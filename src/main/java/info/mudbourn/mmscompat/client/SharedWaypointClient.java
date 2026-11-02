package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.waypoint.SharedWaypoints;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.SyncS2C;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client half of the shared-waypoint sync. Mirrors the current dimension's
 * shared list into real Xaero waypoint sets, one per set named on the entries.
 * Only waypoints this client mirrored are ever removed.
 */
public final class SharedWaypointClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_client");
    private static final int RECONCILE_INTERVAL_TICKS = 60;

    private static final Map<String, List<Entry>> CACHE = new ConcurrentHashMap<>();

    // Dimension id to set-and-name keys this client mirrored this session.
    private static final Map<String, Set<String>> MIRRORED = new ConcurrentHashMap<>();

    private static int tickCounter = 0;

    private SharedWaypointClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SyncS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    CACHE.put(payload.dimension(), List.copyOf(payload.entries()));
                    tickCounter = RECONCILE_INTERVAL_TICKS;
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CACHE.clear();
            MIRRORED.clear();
        });

        ClientTickEvents.END_CLIENT_TICK.register(SharedWaypointClient::tick);
    }

    private static void tick(Minecraft client) {
        if (++tickCounter < RECONCILE_INTERVAL_TICKS) return;
        tickCounter = 0;

        if (client.player == null || client.level == null) return;

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null || world.getDimId() == null) return;

        String dimension = world.getDimId().identifier().toString();
        List<Entry> serverList = CACHE.get(dimension);
        if (serverList == null) return;

        Set<String> mirrored = MIRRORED.computeIfAbsent(dimension, k -> new HashSet<>());
        Map<String, Map<String, Entry>> wantedBySet = new LinkedHashMap<>();
        for (Entry e : serverList) {
            wantedBySet.computeIfAbsent(e.set(), k -> new HashMap<>())
                    .put(e.name().toLowerCase(Locale.ROOT), e);
        }

        int[] counts = new int[3];
        for (WaypointSet set : world.getIterableWaypointSets()) {
            Map<String, Entry> wanted = wantedBySet.remove(set.getName());
            reconcileExisting(set, wanted == null ? Map.of() : wanted, mirrored, counts);
            if (wanted != null) addMissing(world, set, wanted, mirrored, counts);
        }
        for (Map.Entry<String, Map<String, Entry>> e : wantedBySet.entrySet()) {
            WaypointSet set = WaypointSet.Builder.begin().setName(e.getKey()).build();
            world.addWaypointSet(set);
            addMissing(world, set, e.getValue(), mirrored, counts);
        }

        if (counts[0] == 0 && counts[1] == 0 && counts[2] == 0) return;

        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (Exception e) {
            LOGGER.error("failed to save Xaero world after shared waypoint sync", e);
        }
        LOGGER.info("shared waypoint sync for {}: {} added, {} corrected, {} removed",
                dimension, counts[0], counts[1], counts[2]);
    }

    // Corrects shared names already in the set and drops mirrors the server no longer lists.
    private static void reconcileExisting(WaypointSet set, Map<String, Entry> wanted, Set<String> mirrored, int[] counts) {
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) {
            Waypoint w = iter.next();
            String key = SharedWaypoints.key(set.getName(), w.getName());
            Entry want = wanted.remove(w.getName().toLowerCase(Locale.ROOT));

            if (want == null) {
                if (mirrored.remove(key)) {
                    iter.remove();
                    counts[2]++;
                }
                continue;
            }

            if (!w.getVisibility().isGlobal()) mirrored.add(key);
            if (applyTo(w, want)) counts[1]++;
        }
    }

    // Adds shared entries the set lacks, unless the player holds that name in another set.
    private static void addMissing(MinimapWorld world, WaypointSet set, Map<String, Entry> wanted, Set<String> mirrored, int[] counts) {
        if (wanted.isEmpty()) return;
        Set<String> otherNames = namesOutside(world, set.getName());
        for (Entry e : wanted.values()) {
            if (otherNames.contains(e.name().toLowerCase(Locale.ROOT))) continue;
            Waypoint w = new Waypoint(
                    e.x(),
                    e.y(),
                    e.z(),
                    e.name(),
                    e.initials(),
                    WaypointColor.fromIndex(e.colorIdx()),
                    WaypointPurpose.NORMAL
            );
            w.setYaw(e.yaw());
            w.setRotation(true);
            set.add(w);
            mirrored.add(e.key());
            counts[0]++;
        }
    }

    // Copies the server's position, color, initials and yaw onto a waypoint, leaving visibility alone.
    private static boolean applyTo(Waypoint w, Entry e) {
        boolean changed = false;
        if (w.getX() != e.x()) { w.setX(e.x()); changed = true; }
        if (w.getY() != e.y()) { w.setY(e.y()); changed = true; }
        if (w.getZ() != e.z()) { w.setZ(e.z()); changed = true; }
        if (w.getColor() != e.colorIdx()) { w.setColor(e.colorIdx()); changed = true; }
        if (!e.initials().equals(w.getSymbol())) { w.setSymbol(e.initials()); changed = true; }
        if (w.getYaw() != e.yaw()) { w.setYaw(e.yaw()); changed = true; }
        return changed;
    }

    private static Set<String> namesOutside(MinimapWorld world, String setName) {
        Set<String> names = new HashSet<>();
        for (WaypointSet set : world.getIterableWaypointSets()) {
            if (setName.equals(set.getName())) continue;
            for (Waypoint w : set.getWaypoints()) {
                names.add(w.getName().toLowerCase(Locale.ROOT));
            }
        }
        return names;
    }
}
