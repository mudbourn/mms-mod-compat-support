package info.mudbourn.mmscompat.client;

import java.util.HashMap;
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
 * Publish half of the shared-waypoint flow, driven from Xaero's UI:
 *
 *   - A waypoint set to GLOBAL visibility is published to the server's shared
 *     list (SharedWaypointServer).
 *   - A waypoint left LOCAL is never published.
 *
 * <p><b>Copy, never move.</b> Publishing does not touch the player's own
 * waypoint — it stays in their set, with their colour and placement, exactly as
 * they made it. An earlier version deleted the private copy once the server
 * echoed the waypoint back ("migration"), which silently removed player-made
 * waypoints; that behaviour is gone and must not come back. To avoid the
 * waypoint then appearing twice, {@link SharedWaypointClient} skips mirroring
 * any shared entry whose name the player already has locally.</p>
 *
 * <p><b>Known consequence of the trigger.</b> Xaero's GLOBAL setting means
 * "render at any distance", not "share this" — its tooltip reads "Local: only
 * visible when in the maximum waypoint render distance. Global: always
 * visible." Using it as the publish trigger is a deliberate server convention,
 * not an inference about player intent: marking a waypoint always-visible for
 * personal convenience will also publish its name and coordinates server-wide.
 * {@code isGlobal()} is likewise true for WORLD_MAP_GLOBAL. Players who want a
 * waypoint kept private must leave it LOCAL.</p>
 *
 * <p>The shared set itself is never scanned, so server->client sync can never
 * feed back into an upload. Publish-only by design: unpublishing is an explicit
 * /mmswp remove.</p>
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

        for (WaypointSet set : world.getIterableWaypointSets()) {
            if (SharedWaypoints.SET_NAME.equals(set.getName())) {
                continue; // server-fed set: mirror only, never a publish source
            }
            for (Waypoint w : set.getWaypoints()) {
                if (w.getPurpose() != WaypointPurpose.NORMAL) continue;   // no deathpoints
                if (w.isTemporary() || w.isDestination()) continue;       // no one-off markers
                if (!w.getVisibility().isGlobal()) continue;              // LOCAL = private, untouched

                if (SharedWaypointClient.isShared(dimension, w.getName())) {
                    // Already on the server list — nothing to do. The player's
                    // own copy stays exactly where it is.
                    pendingPublishes.remove(w.getName());
                } else {
                    publish(dimension, w);
                }
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
}
