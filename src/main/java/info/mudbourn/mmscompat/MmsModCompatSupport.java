package info.mudbourn.mmscompat;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsModCompatSupport implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    @Override
    public void onInitialize() {
        // Creative-menu embargo strip
        CreativeEmbargo.register();
        info.mudbourn.mmscompat.waypoint.SharedWaypointServer.register();

        // Xaero's World Map chunk sync (server-side packet handler)

        LOG.info("MMS Mod Compat Support loaded — creative-tab dedup, REI null-filter fix, AR item-drop suppression, creative embargo, Xaero chunk sync.");
    }
}
