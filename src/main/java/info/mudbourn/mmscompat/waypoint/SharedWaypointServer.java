package info.mudbourn.mmscompat.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server half of the shared-waypoint sync. Keeps the authoritative
 * per-dimension list in config/mms_compat/shared_waypoints.json, each entry
 * tagged with its Xaero waypoint set, and broadcasts full lists to clients.
 */
public final class SharedWaypointServer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_server");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private record PendingScan(String dimension, String set) {}

    private static Map<String, List<Entry>> store = null;

    private static final Map<UUID, PendingScan> pendingScans = new ConcurrentHashMap<>();

    private SharedWaypointServer() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ScanResultC2S.TYPE, ScanResultC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncS2C.TYPE, SyncS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(ScanRequestS2C.TYPE, ScanRequestS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ScanResultC2S.TYPE, (payload, context) ->
                context.server().execute(() -> onScanResult(context.player(), payload)));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Map.Entry<String, List<Entry>> e : store().entrySet()) {
                sender.sendPacket(new SyncS2C(e.getKey(), e.getValue()));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                pendingScans.remove(handler.getPlayer().getUUID()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("mmswp")
                        .then(Commands.literal("list")
                                .executes(ctx -> list(ctx.getSource(), null))
                                .then(setArgument()
                                        .executes(ctx -> list(ctx.getSource(), set(ctx)))))
                        .then(Commands.literal("globalscan")
                                .requires(SharedWaypointServer::isOperator)
                                .executes(ctx -> globalScan(ctx.getSource(), ""))
                                .then(Commands.argument("set", StringArgumentType.string())
                                        .executes(ctx -> globalScan(ctx.getSource(), set(ctx)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> remove(ctx.getSource(), null, name(ctx)))))
                        .then(Commands.literal("removefrom")
                                .then(setArgument()
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(ctx -> remove(ctx.getSource(), set(ctx), name(ctx))))))));

        LOGGER.info("[mms_compat] shared waypoint server registered");
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> setArgument() {
        return Commands.argument("set", StringArgumentType.string())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        setsIn(dimensionOf(ctx.getSource())).stream().map(SharedWaypointServer::quoted),
                        builder));
    }

    private static String quoted(String set) {
        return StringArgumentType.escapeIfRequired(set);
    }

    private static String set(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, "set");
    }

    private static String name(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, "name");
    }

    private static String dimensionOf(CommandSourceStack source) {
        return source.getLevel().dimension().identifier().toString();
    }

    private static boolean isOperator(CommandSourceStack source) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2)));
    }

    private static int globalScan(CommandSourceStack source, String set) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("/mmswp globalscan must be run by a player."));
            return 0;
        }
        if (!ServerPlayNetworking.canSend(player, ScanRequestS2C.TYPE)) {
            source.sendFailure(Component.literal("Your client does not have the MMS waypoint sync, nothing to scan."));
            return 0;
        }
        String dimension = player.level().dimension().identifier().toString();
        pendingScans.put(player.getUUID(), new PendingScan(dimension, set));
        ServerPlayNetworking.send(player, new ScanRequestS2C(dimension, set));
        String scope = set.isEmpty() ? "all sets" : "set \"" + set + "\"";
        source.sendSuccess(() -> Component.literal("Scanning your global waypoints in " + dimension + " (" + scope + ")..."), false);
        return 1;
    }

    // Applies a scan by set and name, rewriting known entries but keeping their owner.
    private static void onScanResult(ServerPlayer player, ScanResultC2S payload) {
        PendingScan expected = pendingScans.remove(player.getUUID());
        if (expected == null || !expected.dimension().equals(payload.dimension())) {
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
            Entry entry = new Entry(
                    raw.set(),
                    raw.name(),
                    raw.initials(),
                    raw.x(),
                    raw.y(),
                    raw.z(),
                    raw.colorIdx(),
                    raw.yaw(),
                    player.getUUID()
            ).sanitized();
            if (entry.name().isEmpty()) continue;
            if (!expected.set().isEmpty() && !entry.set().equalsIgnoreCase(expected.set())) continue;

            int at = indexOf(list, entry.key());
            if (at < 0) {
                list.add(entry);
                added++;
                continue;
            }

            Entry existing = list.get(at);
            Entry merged = new Entry(
                    existing.set(),
                    existing.name(),
                    entry.initials(),
                    entry.x(),
                    entry.y(),
                    entry.z(),
                    entry.colorIdx(),
                    entry.yaw(),
                    existing.owner()
            );
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
        LOGGER.info("'{}' globalscan in {} ({}): {} scanned, {} added, {} updated, {} unchanged",
                player.getName().getString(), payload.dimension(),
                expected.set().isEmpty() ? "all sets" : expected.set(),
                payload.entries().size(), added, updated, unchanged);
    }

    private static int remove(CommandSourceStack source, String set, String name) {
        String dimension = dimensionOf(source);
        List<Entry> list = store().getOrDefault(dimension, List.of());
        List<Entry> matches = list.stream()
                .filter(e -> e.name().equalsIgnoreCase(name))
                .filter(e -> set == null || e.set().equalsIgnoreCase(set))
                .toList();
        if (matches.isEmpty()) {
            String where = set == null ? "here" : "in set \"" + set + "\"";
            source.sendFailure(Component.literal("No shared waypoint named \"" + name + "\" " + where + "."));
            return 0;
        }
        if (matches.size() > 1) {
            String sets = String.join(", ", matches.stream().map(Entry::set).toList());
            source.sendFailure(Component.literal(
                    "\"" + name + "\" exists in several sets (" + sets + "). Use /mmswp removefrom <set> <name>."));
            return 0;
        }

        Entry entry = matches.get(0);
        UUID caller = source.getPlayer() != null ? source.getPlayer().getUUID() : null;
        boolean isOwner = caller != null && caller.equals(entry.owner());
        if (!isOwner && !isOperator(source)) {
            source.sendFailure(Component.literal("Only the waypoint's owner or an operator can remove it."));
            return 0;
        }
        list.remove(entry);
        save();
        broadcast(source.getServer(), dimension);
        source.sendSuccess(() -> Component.literal(
                "Removed shared waypoint \"" + entry.name() + "\" from set \"" + entry.set() + "\"."), true);
        return 1;
    }

    private static int list(CommandSourceStack source, String set) {
        String dimension = dimensionOf(source);
        List<Entry> all = store().getOrDefault(dimension, List.of());
        Set<String> sets = setsIn(dimension);
        if (set != null) {
            sets.removeIf(s -> !s.equalsIgnoreCase(set));
            if (sets.isEmpty()) {
                source.sendFailure(Component.literal("No shared waypoint set named \"" + set + "\" here."));
                return 0;
            }
        }
        if (sets.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No shared waypoints here."), false);
            return 1;
        }
        for (String s : sets) {
            List<String> names = all.stream()
                    .filter(e -> e.set().equals(s))
                    .map(Entry::name)
                    .toList();
            source.sendSuccess(() -> Component.literal(
                    s + " (" + names.size() + "): " + String.join(", ", names)), false);
        }
        return 1;
    }

    private static Set<String> setsIn(String dimension) {
        Set<String> sets = new LinkedHashSet<>();
        for (Entry e : store().getOrDefault(dimension, List.of())) sets.add(e.set());
        return sets;
    }

    private static void broadcast(MinecraftServer server, String dimension) {
        if (server == null) return;
        SyncS2C payload = new SyncS2C(dimension, store().getOrDefault(dimension, List.of()));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    private static int indexOf(List<Entry> list, String key) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).key().equals(key)) return i;
        }
        return -1;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("mms_compat").resolve("shared_waypoints.json");
    }

    // Loads the store, filling in the default set for entries saved without one.
    private static synchronized Map<String, List<Entry>> store() {
        if (store == null) {
            store = new LinkedHashMap<>();
            Path f = file();
            if (Files.exists(f)) {
                try (Reader r = Files.newBufferedReader(f)) {
                    Map<String, List<Entry>> loaded = GSON.fromJson(r,
                            new TypeToken<Map<String, List<Entry>>>() {}.getType());
                    if (loaded != null) {
                        loaded.forEach((dim, entries) -> store.put(dim,
                                new ArrayList<>(entries.stream().map(Entry::sanitized).toList())));
                    }
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
