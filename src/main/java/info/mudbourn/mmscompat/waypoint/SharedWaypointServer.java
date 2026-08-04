package info.mudbourn.mmscompat.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.ScanRequestS2C;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.ScanResultC2S;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.SyncS2C;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server half of the shared-waypoint sync.
 *
 * Keeps the authoritative per-dimension shared list in
 * config/mms_compat/shared_waypoints.json, accepts publishes from any player
 * (dedupe/replace by name), and broadcasts the affected dimension's full
 * list to everyone. Full-list sync means clients can idempotently mirror it
 * into their Xaero "MMS" set — no delta bookkeeping to corrupt.
 *
 * Publishing is admin-driven: an operator runs /mmswp globalscan, the server
 * asks that one client for its GLOBAL waypoints, and the batch is applied by
 * name — new names added, known names rewritten from the scanned copy.
 * Players cannot publish, and no client tick loop watches for waypoints being
 * flipped to GLOBAL — Xaero's GLOBAL toggle is a render-distance setting, and
 * treating it as "share this" published waypoints nobody meant to share.
 *
 * Removal is deliberately narrow: /mmswp remove <name> works for the
 * waypoint's owner or permission level 2+. Client-side deletions are NOT
 * mirrored (a stale client must never be able to mass-delete the list).
 */
public final class SharedWaypointServer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_server");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** dimension id -> entries; insertion-ordered for stable files. */
    private static Map<String, List<Entry>> store = null;

    /**
     * Players with a {@link ScanRequestS2C} outstanding, and the dimension it was
     * issued for. A scan result is only honoured while the sender appears here, so
     * a client cannot publish by sending an unsolicited batch. Cleared on receipt
     * and on disconnect.
     */
    private static final Map<UUID, String> pendingScans = new java.util.concurrent.ConcurrentHashMap<>();

    private SharedWaypointServer() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ScanResultC2S.TYPE, ScanResultC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncS2C.TYPE, SyncS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(ScanRequestS2C.TYPE, ScanRequestS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ScanResultC2S.TYPE, (payload, context) ->
                context.server().execute(() -> onScanResult(context.player(), payload)));

        // full sync on join, every dimension
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Map.Entry<String, List<Entry>> e : store().entrySet()) {
                sender.sendPacket(new SyncS2C(e.getKey(), e.getValue()));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                pendingScans.remove(handler.getPlayer().getUUID()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("mmswp")
                        .then(Commands.literal("list").executes(ctx -> {
                            listWaypoints(ctx.getSource());
                            return 1;
                        }))
                        .then(Commands.literal("globalscan")
                                .requires(SharedWaypointServer::isOperator)
                                .executes(ctx -> globalScan(ctx.getSource())))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> remove(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name")))))));

        LOGGER.info("[mms_compat] shared waypoint server registered");
    }

    private static boolean isOperator(CommandSourceStack source) {
        return source.permissions().hasPermission(
                new net.minecraft.server.permissions.Permission.HasCommandLevel(
                        net.minecraft.server.permissions.PermissionLevel.byId(2)));
    }

    /**
     * Ask the calling operator's client for its GLOBAL waypoints in this
     * dimension. The scan indexes whatever that admin can already see — waypoints
     * they made themselves, or ones shared with them — which is why it is scoped
     * to one player rather than crawling every client.
     */
    private static int globalScan(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("/mmswp globalscan must be run by a player."));
            return 0;
        }
        if (!ServerPlayNetworking.canSend(player, ScanRequestS2C.TYPE)) {
            source.sendFailure(Component.literal(
                    "Your client does not have the MMS waypoint sync — nothing to scan."));
            return 0;
        }
        String dimension = player.level().dimension().identifier().toString();
        pendingScans.put(player.getUUID(), dimension);
        ServerPlayNetworking.send(player, new ScanRequestS2C(dimension));
        source.sendSuccess(() -> Component.literal(
                "Scanning your global waypoints in " + dimension + "…"), false);
        return 1;
    }

    /**
     * Index an admin's scan batch. The scan is authoritative: a name already on
     * the list is rewritten in place from the scanned copy — coordinates, colour,
     * initials, yaw — rather than skipped.
     *
     * <p>Skipping was the old behaviour and it made the list unmaintainable. A
     * waypoint that was indexed at the wrong spot, or in the wrong colour, could
     * only be corrected by removing it and scanning again, and the same admin who
     * had just moved their own copy would be told "0 new, 1 already indexed" with
     * nothing changed. The scan is an operator-only command run by someone looking
     * at the waypoints they mean to publish, so their copy is the newer truth.
     *
     * <p>{@code owner} is the one field a rewrite preserves. Ownership decides who
     * may {@code /mmswp remove} an entry, so a rescan must not quietly transfer it
     * to whoever scanned last.
     */
    private static void onScanResult(ServerPlayer player, ScanResultC2S payload) {
        String expected = pendingScans.remove(player.getUUID());
        if (expected == null || !expected.equals(payload.dimension())) {
            LOGGER.warn("dropping unsolicited waypoint scan from '{}'", player.getName().getString());
            return;
        }
        if (payload.entries().size() > SharedWaypoints.MAX_SCAN_ENTRIES) {
            player.sendSystemMessage(Component.literal(
                    "Scan aborted: more than " + SharedWaypoints.MAX_SCAN_ENTRIES + " global waypoints."));
            return;
        }

        List<Entry> list = store().computeIfAbsent(payload.dimension(), k -> new ArrayList<>());
        int added = 0;
        int updated = 0;
        int unchanged = 0;
        for (Entry raw : payload.entries()) {
            Entry entry = new Entry(raw.name(), raw.initials(), raw.x(), raw.y(), raw.z(),
                    raw.colorIdx(), raw.yaw(), player.getUUID()).sanitized();
            if (entry.name().isEmpty()) continue;

            int at = indexOfName(list, entry.name());
            if (at < 0) {
                list.add(entry);
                added++;
                continue;
            }

            Entry existing = list.get(at);
            // Keep the stored name and owner: the name so the file keeps whatever
            // casing it was indexed under, the owner so a rescan never reassigns
            // who is allowed to remove the entry.
            Entry merged = new Entry(existing.name(), entry.initials(),
                    entry.x(), entry.y(), entry.z(), entry.colorIdx(), entry.yaw(),
                    existing.owner());
            if (merged.equals(existing)) {
                unchanged++;
            } else {
                list.set(at, merged);
                updated++;
            }
        }

        if (added > 0 || updated > 0) {
            save();
            broadcast(player.level().getServer(), payload.dimension());
        }
        int newCount = added;
        int changedCount = updated;
        int sameCount = unchanged;
        player.sendSystemMessage(Component.literal(
                "Indexed " + newCount + " new waypoint" + (newCount == 1 ? "" : "s")
                        + ", updated " + changedCount
                        + (sameCount > 0 ? " (" + sameCount + " already up to date)" : "") + "."));
        LOGGER.info("'{}' globalscan in {}: {} scanned, {} added, {} updated, {} unchanged",
                player.getName().getString(), payload.dimension(),
                payload.entries().size(), added, updated, unchanged);
    }

    private static int remove(CommandSourceStack source, String name) {
        String dimension = source.getLevel().dimension().identifier().toString();
        List<Entry> list = store().get(dimension);
        Entry entry = list == null ? null : byName(list, name);
        if (entry == null) {
            source.sendFailure(Component.literal("No shared waypoint named \"" + name + "\" here."));
            return 0;
        }
        UUID caller = source.getPlayer() != null ? source.getPlayer().getUUID() : null;
        boolean isOwner = caller != null && caller.equals(entry.owner());
        if (!isOwner && !isOperator(source)) {
            source.sendFailure(Component.literal("Only the waypoint's owner or an operator can remove it."));
            return 0;
        }
        list.remove(entry);
        save();
        broadcast(source.getServer(), dimension);
        source.sendSuccess(() -> Component.literal("Removed shared waypoint \"" + name + "\"."), true);
        return 1;
    }

    private static void listWaypoints(CommandSourceStack source) {
        String dimension = source.getLevel().dimension().identifier().toString();
        List<Entry> list = store().getOrDefault(dimension, List.of());
        source.sendSuccess(() -> Component.literal(
                "Shared waypoints here (" + list.size() + "): "
                        + String.join(", ", list.stream().map(Entry::name).toList())), false);
    }

    private static void broadcast(MinecraftServer server, String dimension) {
        if (server == null) return;
        SyncS2C payload = new SyncS2C(dimension, store().getOrDefault(dimension, List.of()));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    private static Entry byName(List<Entry> list, String name) {
        int at = indexOfName(list, name);
        return at < 0 ? null : list.get(at);
    }

    private static int indexOfName(List<Entry> list, String name) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    // ── persistence ──

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("mms_compat").resolve("shared_waypoints.json");
    }

    private static synchronized Map<String, List<Entry>> store() {
        if (store == null) {
            store = new LinkedHashMap<>();
            Path f = file();
            if (Files.exists(f)) {
                try (Reader r = Files.newBufferedReader(f)) {
                    Map<String, List<Entry>> loaded = GSON.fromJson(r,
                            new TypeToken<Map<String, List<Entry>>>() {}.getType());
                    if (loaded != null) store.putAll(loaded);
                } catch (Exception e) {
                    LOGGER.error("failed to load shared waypoints; starting empty", e);
                }
            }
        }
        return store;
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(file().getParent());
            try (Writer w = Files.newBufferedWriter(file())) {
                GSON.toJson(store(), w);
            }
        } catch (IOException e) {
            LOGGER.error("failed to save shared waypoints", e);
        }
    }
}
