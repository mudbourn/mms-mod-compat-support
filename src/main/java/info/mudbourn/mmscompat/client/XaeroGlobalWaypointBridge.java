package info.mudbourn.mmscompat.client;

import java.util.ArrayList;
import java.util.List;

import info.mudbourn.mmscompat.waypoint.SharedWaypoints;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.Entry;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.ScanRequestS2C;
import info.mudbourn.mmscompat.waypoint.SharedWaypoints.ScanResultC2S;
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
 * Publish half of the shared-waypoint flow: answers an admin's
 * {@code /mmswp globalscan} by reporting this client's GLOBAL waypoints.
 *
 * <h2>Why this is request-driven</h2>
 *
 * <p>This used to be a client tick loop that scanned every five seconds and
 * published anything flipped to GLOBAL. That made every player a publisher, and
 * it read intent into a setting that does not carry it: Xaero's GLOBAL means
 * "render at any distance", not "share this" — the tooltip is "Local: only
 * visible when in the maximum waypoint render distance. Global: always visible."
 * Marking a waypoint always-visible for personal convenience therefore published
 * its name and coordinates server-wide.
 *
 * <p>Now nothing is sent unless the server asks, and the server only asks the one
 * operator who ran the command. An admin scanning indexes what they can already
 * see — waypoints they made, or ones shared with them — which is a deliberate act
 * with a person behind it.
 *
 * <h2>Copy, never move</h2>
 *
 * <p>Scanning does not touch the player's own waypoints. They stay in their set,
 * with their colour and placement, exactly as made. An early version deleted the
 * private copy once the server echoed the waypoint back ("migration"), which
 * silently removed player-made waypoints; that behaviour is gone and must not
 * come back. {@link SharedWaypointClient} skips mirroring any shared entry whose
 * name the player already holds, so nothing shows up twice.
 */
public final class XaeroGlobalWaypointBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat_wp_bridge");

    private XaeroGlobalWaypointBridge() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ScanRequestS2C.TYPE, (payload, context) ->
                context.client().execute(() -> onScanRequest(context.client(), payload)));
    }

    private static void onScanRequest(Minecraft client, ScanRequestS2C request) {
        List<Entry> found = new ArrayList<>();

        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        MinimapWorld world = session == null ? null : session.getWorldManager().getCurrentWorld();
        if (world != null) {
            for (WaypointSet set : world.getIterableWaypointSets()) {
                if (SharedWaypoints.SET_NAME.equals(set.getName())) {
                    continue; // server-fed set: mirror only, never a scan source
                }
                for (Waypoint w : set.getWaypoints()) {
                    if (w.getPurpose() != WaypointPurpose.NORMAL) continue;   // no deathpoints
                    if (w.isTemporary() || w.isDestination()) continue;       // no one-off markers
                    if (!w.getVisibility().isGlobal()) continue;              // LOCAL = private
                    if (found.size() >= SharedWaypoints.MAX_SCAN_ENTRIES) break;

                    found.add(new Entry(w.getName(), w.getInitials(),
                            w.getX(), w.getY(), w.getZ(),
                            colorIndexOf(w.getWaypointColor()), w.getYaw(),
                            SharedWaypoints.NO_OWNER)); // server stamps the real owner
                }
            }
        }

        ClientPlayNetworking.send(new ScanResultC2S(request.dimension(), found));
        LOGGER.info("globalscan: reported {} global waypoints for {}",
                found.size(), request.dimension());
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
