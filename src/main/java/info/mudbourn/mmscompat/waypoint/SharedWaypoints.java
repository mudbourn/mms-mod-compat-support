package info.mudbourn.mmscompat.waypoint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared-waypoint data model and network payloads (common to both sides).
 *
 * Replaces the server_waypoint mod: that mod injected synced waypoints as a
 * transient mod-managed set invisible to Xaero's own waypoint menu (and
 * therefore missing from the world map). We sync into a REAL Xaero set
 * instead — see SharedWaypointClient.
 */
public final class SharedWaypoints {

    /** The Xaero waypoint set name shared waypoints live in, and the wire id namespace. */
    public static final String SET_NAME = "MMS";
    public static final UUID NO_OWNER = new UUID(0L, 0L);
    public static final int MAX_NAME_LEN = 48;

    private SharedWaypoints() {}

    /** One shared waypoint. Color is the Xaero palette index (0-15). */
    public record Entry(String name, String initials, int x, int y, int z,
                        int colorIdx, int yaw, UUID owner) {

        public Entry sanitized() {
            String n = name.strip().replaceAll("[\\p{Cntrl}]", "");
            if (n.length() > MAX_NAME_LEN) n = n.substring(0, MAX_NAME_LEN);
            String i = initials.strip().replaceAll("[\\p{Cntrl}]", "");
            if (i.isEmpty()) i = n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
            if (i.length() > 3) i = i.substring(0, 3);
            return new Entry(n, i, x, y, z,
                    Math.floorMod(colorIdx, 16), yaw, owner);
        }

        static void write(RegistryFriendlyByteBuf buf, Entry e) {
            buf.writeUtf(e.name);
            buf.writeUtf(e.initials);
            buf.writeVarInt(e.x);
            buf.writeVarInt(e.y);
            buf.writeVarInt(e.z);
            buf.writeVarInt(e.colorIdx);
            buf.writeVarInt(e.yaw);
            buf.writeUUID(e.owner);
        }

        static Entry read(RegistryFriendlyByteBuf buf) {
            return new Entry(buf.readUtf(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                    buf.readVarInt(), buf.readVarInt(), buf.readUUID());
        }
    }

    /** Client -> server: publish (or update) one waypoint on the shared list. */
    public record PublishC2S(String dimension, Entry entry) implements CustomPacketPayload {
        public static final Type<PublishC2S> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_publish"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PublishC2S> CODEC =
                StreamCodec.of(
                        (buf, p) -> { buf.writeUtf(p.dimension); Entry.write(buf, p.entry); },
                        buf -> new PublishC2S(buf.readUtf(), Entry.read(buf)));
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Server -> client: the full shared list for one dimension (authoritative). */
    public record SyncS2C(String dimension, List<Entry> entries) implements CustomPacketPayload {
        public static final Type<SyncS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncS2C> CODEC =
                StreamCodec.of(
                        (buf, p) -> {
                            buf.writeUtf(p.dimension);
                            buf.writeVarInt(p.entries.size());
                            for (Entry e : p.entries) Entry.write(buf, e);
                        },
                        buf -> {
                            String dim = buf.readUtf();
                            int n = buf.readVarInt();
                            List<Entry> list = new ArrayList<>(n);
                            for (int i = 0; i < n; i++) list.add(Entry.read(buf));
                            return new SyncS2C(dim, list);
                        });
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
