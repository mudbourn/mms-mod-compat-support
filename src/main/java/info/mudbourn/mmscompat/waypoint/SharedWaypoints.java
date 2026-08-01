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

    /**
     * Server -> client: "send me your GLOBAL waypoints for this dimension."
     *
     * <p>Only ever sent to the player who ran {@code /mmswp globalscan}, and only
     * if they passed the operator check. The client cannot start this exchange —
     * that is the whole point of the redesign. Publishing used to run off a client
     * tick loop that watched for any waypoint flipped to GLOBAL, which made every
     * player a publisher and made Xaero's render-distance toggle mean "share this
     * with the server" as a side effect.</p>
     */
    public record ScanRequestS2C(String dimension) implements CustomPacketPayload {
        public static final Type<ScanRequestS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_scan_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanRequestS2C> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeUtf(p.dimension),
                        buf -> new ScanRequestS2C(buf.readUtf()));
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /**
     * Client -> server: the answer to a {@link ScanRequestS2C} — every GLOBAL
     * waypoint the admin holds in this dimension, in one batch.
     *
     * <p>The server accepts this only from an operator with a scan outstanding, so
     * an unsolicited or replayed batch is dropped rather than trusted.</p>
     */
    public record ScanResultC2S(String dimension, List<Entry> entries) implements CustomPacketPayload {
        public static final Type<ScanResultC2S> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_scan_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanResultC2S> CODEC =
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
                            return new ScanResultC2S(dim, list);
                        });
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Hard cap on one scan batch, so a malformed client cannot flood the store. */
    public static final int MAX_SCAN_ENTRIES = 512;

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
