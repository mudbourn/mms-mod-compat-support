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
 * Answers an operator's {@code /mmswp globalscan} by reporting this client's
 * GLOBAL waypoints, tagged with their Xaero set, from one set or all of them.
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
                if (!request.set().isEmpty() && !request.set().equalsIgnoreCase(set.getName())) continue;
                for (Waypoint w : set.getWaypoints()) {
                    if (w.getPurpose() != WaypointPurpose.NORMAL) continue;
                    if (w.isTemporary() || w.isDestination()) continue;
                    if (!w.getVisibility().isGlobal()) continue;
                    if (found.size() >= SharedWaypoints.MAX_SCAN_ENTRIES) break;

                    found.add(new Entry(
                            set.getName(),
                            w.getName(),
                            w.getInitials(),
                            w.getX(),
                            w.getY(),
                            w.getZ(),
                            colorIndexOf(w.getWaypointColor()),
                            w.getYaw(),
                            SharedWaypoints.NO_OWNER
                    ));
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
        return 15;
    }
}
