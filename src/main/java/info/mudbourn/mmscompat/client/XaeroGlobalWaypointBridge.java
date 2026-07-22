package info.mudbourn.mmscompat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;

/**
 * Bridges Xaero's waypoint visibility to the server_waypoint shared list,
 * so players never touch server_waypoint directly:
 *
 *   - Toggle a waypoint to GLOBAL in Xaero's own UI  ->  it is published to
 *     the server's shared "MMS" list (via the /wp command tree, so the
 *     server's own permission config still applies).
 *   - Once the server syncs it back into the shared set, the player's
 *     private copy is removed: the waypoint has MIGRATED from their
 *     personal list to the shared one. One waypoint, no doubles.
 *
 * Waypoints left LOCAL (Xaero's default) never leave the client — privacy
 * by architecture. Only the player's own sets are scanned; the server-fed
 * shared set is treated as read-only truth, so server->client sync can
 * never feed back into an upload.
 *
 * Publish-only by design: unpublishing is an explicit /wp remove (or GUI)
 * action. Automatically mirroring deletions was rejected as too risky — a
 * client with a stale view could mass-delete shared waypoints.
 *
 * No dependency on server_waypoint: if the server drops the mod, /wp
 * commands fail with a chat error and nothing else happens.
 */
public final class XaeroGlobalWaypointBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_bridge");
    /** server_waypoint list every publish goes to; also the Xaero set name it syncs back as. */
    private static final String SHARED_LIST = "MMS";
    private static final int SCAN_INTERVAL_TICKS = 100;  // 5s
    private static final int RESEND_COOLDOWN_MS = 60_000;

    private static int tickCounter = 0;
    /** name -> last time we sent /wp add, so a slow server doesn't get spammed. */
    private static final Map<String, Long> pendingPublishes = new HashMap<>();

    private XaeroGlobalWaypointBridge() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(XaeroGlobalWaypointBridge::tick);
    }

    private static void tick(Minecraft client) {
        if (++tickCounter < SCAN_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        if (client.player == null || client.level == null || client.getConnection() == null) {
            pendingPublishes.clear();
            return;
        }
        if (client.getSingleplayerServer() != null) {
            return; // multiplayer only
        }

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) {
            return;
        }
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) {
            return;
        }

        WaypointSet shared = world.getWaypointSet(SHARED_LIST);
        String dimension = client.player.level().dimension().identifier().toString();
        boolean migrated = false;

        for (WaypointSet set : world.getIterableWaypointSets()) {
            if (SHARED_LIST.equals(set.getName())) {
                continue; // server-fed set: read-only truth, never scanned for upload
            }
            List<Waypoint> toMigrate = new ArrayList<>();
            for (Waypoint w : set.getWaypoints()) {
                if (w.getPurpose() != WaypointPurpose.NORMAL) continue;   // no deathpoints
                if (w.isTemporary() || w.isDestination()) continue;       // no one-off markers
                if (!w.getVisibility().isGlobal()) continue;              // LOCAL = private, untouched

                if (shared != null && containsName(shared, w.getName())) {
                    // Server has it (either our publish confirmed, or the name
                    // already existed): drop the private copy to finish migration.
                    toMigrate.add(w);
                } else {
                    publish(client, dimension, w);
                }
            }
            for (Waypoint w : toMigrate) {
                removeByName(set, w.getName());
                pendingPublishes.remove(w.getName());
                migrated = true;
                LOGGER.info("waypoint '{}' migrated to shared list '{}'", w.getName(), SHARED_LIST);
            }
        }

        if (migrated) {
            try {
                session.getWorldManagerIO().saveWorld(world);
            } catch (Exception e) {
                LOGGER.error("failed to save Xaero waypoints after migration", e);
            }
        }
    }

    private static void publish(Minecraft client, String dimension, Waypoint w) {
        long now = System.currentTimeMillis();
        Long lastSent = pendingPublishes.get(w.getName());
        if (lastSent != null && now - lastSent < RESEND_COOLDOWN_MS) {
            return;
        }
        pendingPublishes.put(w.getName(), now);

        String cmd = String.format(
            "wp add %s %s %d %d %d %s %s %s %d true",
            dimension,
            quote(SHARED_LIST),
            w.getX(), w.getY(), w.getZ(),
            quote(w.getName()),
            quote(w.getInitials()),
            quote(String.format("#%06X", w.getWaypointColor().getHex() & 0xFFFFFF)),
            w.getYaw()
        );
        client.player.connection.sendCommand(cmd);
        LOGGER.info("publishing global waypoint '{}' to shared list '{}'", w.getName(), SHARED_LIST);
    }

    private static boolean containsName(WaypointSet set, String name) {
        for (Waypoint w : set.getWaypoints()) {
            if (name.equals(w.getName())) {
                return true;
            }
        }
        return false;
    }

    private static void removeByName(WaypointSet set, String name) {
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                iter.remove();
            }
        }
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
