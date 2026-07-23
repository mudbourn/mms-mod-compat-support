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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client half of the shared-waypoint sync.
 *
 * Caches the server's authoritative per-dimension lists, and reconciles the
 * CURRENT dimension's list into a real Xaero waypoint set ("MMS") — one that
 * shows up in Xaero's own waypoint menu, minimap, and world map like any
 * player-made set. Players control rendering (in-world beacons etc.) with
 * Xaero's normal settings; the set can be hidden per-player from the menu.
 *
 * Reconciliation is idempotent and repairs local edits: the MMS set is a
 * mirror of server state. Toggling a personal waypoint to GLOBAL publishes
 * it (see XaeroGlobalWaypointBridge); deleting from the MMS set locally does
 * NOT unpublish — it just comes back. Unpublish is /mmswp remove.
 */
public final class SharedWaypointClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_client");
    private static final int RECONCILE_INTERVAL_TICKS = 60; // 3s

    /** dimension id -> latest server list. */
    private static final Map<String, List<Entry>> CACHE = new ConcurrentHashMap<>();
    private static int tickCounter = 0;

    private SharedWaypointClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SyncS2C.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    CACHE.put(payload.dimension(), List.copyOf(payload.entries()));
                    tickCounter = RECONCILE_INTERVAL_TICKS; // reconcile on next tick
                }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CACHE.clear());

        ClientTickEvents.END_CLIENT_TICK.register(SharedWaypointClient::tick);
    }

    /** Whether the shared list for a dimension already carries this name. */
    public static boolean isShared(String dimension, String name) {
        List<Entry> list = CACHE.get(dimension);
        if (list == null) return false;
        for (Entry e : list) {
            if (e.name().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static boolean isSyncActive() {
        return ClientPlayNetworking.canSend(SharedWaypoints.PublishC2S.TYPE);
    }

    private static void tick(Minecraft client) {
        if (++tickCounter < RECONCILE_INTERVAL_TICKS) return;
        tickCounter = 0;

        if (client.player == null || client.level == null) return;

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) return;
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) return;

        String dimension = client.player.level().dimension().identifier().toString();
        List<Entry> wanted = CACHE.get(dimension);
        if (wanted == null) return; // no server data (yet) — leave whatever exists alone

        WaypointSet set = world.getWaypointSet(SharedWaypoints.SET_NAME);
        if (set == null) {
            if (wanted.isEmpty()) return;
            set = WaypointSet.Builder.begin().setName(SharedWaypoints.SET_NAME).build();
            world.addWaypointSet(set);
        }

        if (signature(set).equals(signature(wanted))) return; // already mirrored

        // rebuild the set to exactly mirror the server list
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) { iter.next(); iter.remove(); }
        for (Entry e : wanted) {
            Waypoint w = new Waypoint(e.x(), e.y(), e.z(), e.name(), e.initials(),
                    WaypointColor.fromIndex(e.colorIdx()), WaypointPurpose.NORMAL);
            w.setYaw(e.yaw());
            w.setRotation(true);
            set.add(w);
        }

        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (Exception e) {
            LOGGER.error("failed to save Xaero world after shared waypoint sync", e);
        }
        LOGGER.info("mirrored {} shared waypoints into Xaero set '{}' for {}",
                wanted.size(), SharedWaypoints.SET_NAME, dimension);
    }

    private static String signature(WaypointSet set) {
        List<String> parts = new ArrayList<>();
        for (Waypoint w : set.getWaypoints()) {
            parts.add(w.getName() + ":" + w.getX() + ":" + w.getY() + ":" + w.getZ());
        }
        parts.sort(String::compareTo);
        return String.join("|", parts);
    }

    private static String signature(List<Entry> entries) {
        List<String> parts = new ArrayList<>();
        for (Entry e : entries) {
            parts.add(e.name() + ":" + e.x() + ":" + e.y() + ":" + e.z());
        }
        parts.sort(String::compareTo);
        return String.join("|", parts);
    }
}
