package info.mudbourn.mmscompat.waypoint;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Shared-waypoint data model and network payloads, common to both sides.
 * Each entry names the Xaero waypoint set it is mirrored into.
 */
public final class SharedWaypoints {

    /** Set used for entries stored before sets were tracked. */
    public static final String DEFAULT_SET = "MMS";
    public static final UUID NO_OWNER = new UUID(0L, 0L);
    public static final int MAX_NAME_LEN = 48;
    public static final int MAX_SET_LEN = 32;

    /** Hard cap on one scan batch. */
    public static final int MAX_SCAN_ENTRIES = 512;

    private SharedWaypoints() {}

    /** Case-insensitive identity of an entry within one dimension. */
    public static String key(String set, String name) {
        return set.toLowerCase(Locale.ROOT) + "\0" + name.toLowerCase(Locale.ROOT);
    }

    /** One shared waypoint. Color is the Xaero palette index (0-15). */
    public record Entry(
            String set,
            String name,
            String initials,
            int x,
            int y,
            int z,
            int colorIdx,
            int yaw,
            UUID owner
    ) {

        public String key() {
            return SharedWaypoints.key(set, name);
        }

        public Entry sanitized() {
            String s = clean(set, MAX_SET_LEN);
            if (s.isEmpty()) s = DEFAULT_SET;
            String n = clean(name, MAX_NAME_LEN);
            String i = clean(initials, 3);
            if (i.isEmpty()) i = n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase(Locale.ROOT);
            return new Entry(s, n, i, x, y, z, Math.floorMod(colorIdx, 16), yaw, owner == null ? NO_OWNER : owner);
        }

        private static String clean(String raw, int max) {
            String s = raw == null ? "" : raw.strip().replaceAll("[\\p{Cntrl}]", "");
            return s.length() > max ? s.substring(0, max) : s;
        }

        static void write(RegistryFriendlyByteBuf buf, Entry e) {
            buf.writeUtf(e.set);
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
            return new Entry(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUUID()
            );
        }
    }

    private static void writeEntries(RegistryFriendlyByteBuf buf, List<Entry> entries) {
        buf.writeVarInt(entries.size());
        for (Entry e : entries) Entry.write(buf, e);
    }

    private static List<Entry> readEntries(RegistryFriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<Entry> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(Entry.read(buf));
        return list;
    }

    /**
     * Server to client: report GLOBAL waypoints for this dimension, from the
     * named Xaero set only, or from every set when {@code set} is empty.
     */
    public record ScanRequestS2C(String dimension, String set) implements CustomPacketPayload {
        public static final Type<ScanRequestS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_scan_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanRequestS2C> CODEC =
                StreamCodec.of(
                        (buf, p) -> {
                            buf.writeUtf(p.dimension);
                            buf.writeUtf(p.set);
                        },
                        buf -> new ScanRequestS2C(buf.readUtf(), buf.readUtf()));
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Client to server: the answer to a {@link ScanRequestS2C}. */
    public record ScanResultC2S(String dimension, List<Entry> entries) implements CustomPacketPayload {
        public static final Type<ScanResultC2S> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_scan_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ScanResultC2S> CODEC =
                StreamCodec.of(
                        (buf, p) -> {
                            buf.writeUtf(p.dimension);
                            writeEntries(buf, p.entries);
                        },
                        buf -> new ScanResultC2S(buf.readUtf(), readEntries(buf)));
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** Server to client: the full shared list for one dimension, across all sets. */
    public record SyncS2C(String dimension, List<Entry> entries) implements CustomPacketPayload {
        public static final Type<SyncS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "wp_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncS2C> CODEC =
                StreamCodec.of(
                        (buf, p) -> {
                            buf.writeUtf(p.dimension);
                            writeEntries(buf, p.entries);
                        },
                        buf -> new SyncS2C(buf.readUtf(), readEntries(buf)));
        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
