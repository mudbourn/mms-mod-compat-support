package info.mudbourn.mmscompat.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.mudbourn.mmscompat.waypoint.SharedWaypoints;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.PublishC2S;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

/**
 * Publish half of the shared-waypoint flow, driven entirely from Xaero's UI:
 *
 *   - Toggle a waypoint to GLOBAL visibility in Xaero  ->  it is published
 *     to the server's shared list (SharedWaypointServer).
 *   - Once the server syncs it back into the shared "MMS" set
 *     (SharedWaypointClient), the private copy is removed: the waypoint has
 *     MIGRATED from the player's personal set to the shared one.
 *
 * Waypoints left LOCAL (Xaero's default) never leave the client — privacy
 * by architecture. The shared set itself is never scanned, so server->client
 * sync can never feed back into an upload.
 *
 * Publish-only by design: unpublishing is an explicit /mmswp remove.
 */
public final class XaeroGlobalWaypointBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_bridge");
    private static final int SCAN_INTERVAL_TICKS = 100;  // 5s
    private static final int RESEND_COOLDOWN_MS = 60_000;

    private static int tickCounter = 0;
    /** name -> last publish send time, so a slow server doesn't get spammed. */
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
        if (!SharedWaypointClient.isSyncActive()) {
            return; // server doesn't run the sync — do nothing
        }

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (session == null) {
            return;
        }
        MinimapWorld world = session.getWorldManager().getCurrentWorld();
        if (world == null) {
            return;
        }

        String dimension = client.player.level().dimension().identifier().toString();
        boolean migrated = false;

        for (WaypointSet set : world.getIterableWaypointSets()) {
            if (SharedWaypoints.SET_NAME.equals(set.getName())) {
                continue; // server-fed set: mirror only, never a publish source
            }
            List<Waypoint> toMigrate = new ArrayList<>();
            for (Waypoint w : set.getWaypoints()) {
                if (w.getPurpose() != WaypointPurpose.NORMAL) continue;   // no deathpoints
                if (w.isTemporary() || w.isDestination()) continue;       // no one-off markers
                if (!w.getVisibility().isGlobal()) continue;              // LOCAL = private, untouched

                if (SharedWaypointClient.isShared(dimension, w.getName())) {
                    // Server list has it (our publish confirmed, or the name
                    // already existed): drop the private copy — migration done.
                    toMigrate.add(w);
                } else {
                    publish(dimension, w);
                }
            }
            for (Waypoint w : toMigrate) {
                removeByName(set, w.getName());
                pendingPublishes.remove(w.getName());
                migrated = true;
                LOGGER.info("waypoint '{}' migrated to shared set '{}'",
                        w.getName(), SharedWaypoints.SET_NAME);
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

    private static void publish(String dimension, Waypoint w) {
        long now = System.currentTimeMillis();
        Long lastSent = pendingPublishes.get(w.getName());
        if (lastSent != null && now - lastSent < RESEND_COOLDOWN_MS) {
            return;
        }
        pendingPublishes.put(w.getName(), now);

        Entry entry = new Entry(w.getName(), w.getInitials(),
                w.getX(), w.getY(), w.getZ(),
                colorIndexOf(w.getWaypointColor()), w.getYaw(),
                SharedWaypoints.NO_OWNER); // server stamps the real owner
        ClientPlayNetworking.send(new PublishC2S(dimension, entry));
        LOGGER.info("publishing global waypoint '{}' to shared set '{}'",
                w.getName(), SharedWaypoints.SET_NAME);
    }

    private static int colorIndexOf(WaypointColor color) {
        for (int i = 0; i < 16; i++) {
            if (WaypointColor.fromIndex(i) == color) {
                return i;
            }
        }
        return 15; // white
    }

    private static void removeByName(WaypointSet set, String name) {
        var iter = set.getWaypoints().iterator();
        while (iter.hasNext()) {
            if (name.equals(iter.next().getName())) {
                iter.remove();
            }
        }
    }
}
