package info.mudbourn.mmscompat.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.StringArgumentType;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.PublishC2S;
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
 * Removal is deliberately narrow: /mmswp remove <name> works for the
 * waypoint's owner or permission level 2+. Client-side deletions are NOT
 * mirrored (a stale client must never be able to mass-delete the list).
 */
public final class SharedWaypointServer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_server");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** dimension id -> entries; insertion-ordered for stable files. */
    private static Map<String, List<Entry>> store = null;

    private SharedWaypointServer() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PublishC2S.TYPE, PublishC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncS2C.TYPE, SyncS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PublishC2S.TYPE, (payload, context) ->
                context.server().execute(() -> onPublish(context.player(), payload)));

        // full sync on join, every dimension
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Map.Entry<String, List<Entry>> e : store().entrySet()) {
                sender.sendPacket(new SyncS2C(e.getKey(), e.getValue()));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("mmswp")
                        .then(Commands.literal("list").executes(ctx -> {
                            listWaypoints(ctx.getSource());
                            return 1;
                        }))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(ctx -> remove(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name")))))));

        LOGGER.info("[mms_compat] shared waypoint server registered");
    }

    private static void onPublish(ServerPlayer player, PublishC2S payload) {
        Entry entry = new Entry(payload.entry().name(), payload.entry().initials(),
                payload.entry().x(), payload.entry().y(), payload.entry().z(),
                payload.entry().colorIdx(), payload.entry().yaw(),
                player.getUUID()).sanitized();
        if (entry.name().isEmpty()) return;

        List<Entry> list = store().computeIfAbsent(payload.dimension(), k -> new ArrayList<>());
        Entry existing = byName(list, entry.name());
        if (existing != null) {
            // same spot from the same owner is a no-op; anyone else's name is taken
            if (!existing.owner().equals(entry.owner())
                    && !existing.owner().equals(SharedWaypoints.NO_OWNER)) {
                return;
            }
            list.remove(existing);
        }
        list.add(entry);
        save();
        broadcast(player.level().getServer(), payload.dimension());
        LOGGER.info("'{}' published shared waypoint '{}' in {}",
                player.getName().getString(), entry.name(), payload.dimension());
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
        boolean isOp = source.permissions().hasPermission(
                new net.minecraft.server.permissions.Permission.HasCommandLevel(
                        net.minecraft.server.permissions.PermissionLevel.byId(2)));
        if (!isOwner && !isOp) {
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
        for (Entry e : list) {
            if (e.name().equalsIgnoreCase(name)) return e;
        }
        return null;
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
