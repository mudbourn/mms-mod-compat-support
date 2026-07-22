package info.mudbourn.mmscompat.map;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkSyncManager {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat_map");
    private static final int EXTRA_CHUNK_RADIUS = 6;
    private static final int TICKET_LIFETIME_TICKS = 600;

    private static final Map<UUID, Map<String, Set<Long>>> PLAYER_EXPLORED = new ConcurrentHashMap<>();
    private static final Map<String, Long> FORCE_LOADED = new ConcurrentHashMap<>();
    private static final TicketType CHUNK_SYNC_TICKET = TicketType.UNKNOWN;

    /** Client→server payload: player reports an explored chunk. */
    public record ExploredChunkPayload(int chunkX, int chunkZ, String dimension) implements CustomPacketPayload {
        public static final Type<ExploredChunkPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "explored_chunk"));

        public static final StreamCodec<RegistryFriendlyByteBuf, ExploredChunkPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> { buf.writeVarInt(p.chunkX); buf.writeVarInt(p.chunkZ); buf.writeUtf(p.dimension); },
                        buf -> new ExploredChunkPayload(buf.readVarInt(), buf.readVarInt(), buf.readUtf())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private ChunkSyncManager() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ExploredChunkPayload.TYPE, ExploredChunkPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ExploredChunkPayload.TYPE, (payload, context) -> {
            context.server().execute(() ->
                    onPlayerExploredChunk(context.player(), payload.chunkX(), payload.chunkZ(), payload.dimension()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PLAYER_EXPLORED.remove(handler.getPlayer().getUUID()));

        LOG.info("[mms_compat] Xaero chunk sync registered");
    }

    private static void onPlayerExploredChunk(ServerPlayer reporter, int chunkX, int chunkZ, String dimension) {
        MinecraftServer server = reporter.level().getServer();
        if (server == null) return;

        UUID reporterId = reporter.getUUID();
        long packed = ChunkPos.asLong(chunkX, chunkZ);

        PLAYER_EXPLORED
                .computeIfAbsent(reporterId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet())
                .add(packed);

        ServerLevel level = findLevel(server, dimension);
        if (level == null) return;

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other.getUUID().equals(reporterId)) continue;
            if (!dimensionOf(other.level()).equals(dimension)) continue;

            Set<Long> otherExplored = PLAYER_EXPLORED
                    .computeIfAbsent(other.getUUID(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());

            if (otherExplored.contains(packed)) continue;

            int viewDistance = server.getPlayerList().getViewDistance();
            ChunkPos otherPos = other.chunkPosition();
            int dist = Math.max(Math.abs(chunkX - otherPos.x), Math.abs(chunkZ - otherPos.z));
            if (dist <= viewDistance) continue;
            if (dist > viewDistance + EXTRA_CHUNK_RADIUS) continue;

            forceLoadForPlayer(level, chunkPos);
        }
    }

    private static void forceLoadForPlayer(ServerLevel level, ChunkPos pos) {
        String key = dimensionOf(level) + ":" + pos.x + ":" + pos.z;

        Long expiry = FORCE_LOADED.get(key);
        if (expiry != null && expiry > level.getGameTime()) return;

        level.getChunkSource().addTicketWithRadius(CHUNK_SYNC_TICKET, pos, 2);
        FORCE_LOADED.put(key, level.getGameTime() + TICKET_LIFETIME_TICKS);
    }

    public static void cleanupExpiredTickets(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        FORCE_LOADED.entrySet().removeIf(entry -> {
            if (entry.getValue() <= gameTime) {
                String[] parts = entry.getKey().split(":");
                if (parts.length == 3) {
                    ServerLevel level = findLevel(server, parts[0]);
                    if (level != null) {
                        int cx = Integer.parseInt(parts[1]);
                        int cz = Integer.parseInt(parts[2]);
                        level.getChunkSource().removeTicketWithRadius(
                                CHUNK_SYNC_TICKET, new ChunkPos(cx, cz), 2);
                    }
                }
                return true;
            }
            return false;
        });
    }

    private static ServerLevel findLevel(MinecraftServer server, String dimension) {
        for (ServerLevel sl : server.getAllLevels()) {
            if (dimensionOf(sl).equals(dimension)) return sl;
        }
        return null;
    }

    private static String dimensionOf(net.minecraft.world.level.Level level) {
        return level.dimension().toString();
    }

    public static void clear() {
        PLAYER_EXPLORED.clear();
        FORCE_LOADED.clear();
    }
}
