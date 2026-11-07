package info.mudbourn.mmscompat;

import info.mudbourn.mmscompat.bubble.BubbleContent;
import info.mudbourn.mmscompat.ca.CaCreativeSlotLock;
import info.mudbourn.mmscompat.carpet.CarpetPayloadRegistration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsModCompatSupport implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    @Override
    public void onInitialize() {
        // Creative-menu embargo strip
        CreativeEmbargo.register();
        info.mudbourn.mmscompat.waypoint.SharedWaypointServer.register();

        // Carpet hello payload registration
        if (FabricLoader.getInstance().isModLoaded("carpet")) {
            CarpetPayloadRegistration.register();
        }

        // Combat Amenities creative slot packet lock
        if (FabricLoader.getInstance().isModLoaded("combatamenities")
            && FabricLoader.getInstance().isModLoaded("trinkets")) {
            CaCreativeSlotLock.register();
        }

        // Bubblellaneous Java port
        if (FabricLoader.getInstance().isModLoaded("mr_bubblellaneous")) {
            BubbleContent.register();
        }

        // Xaero's World Map chunk sync (server-side packet handler)

        LOG.info("MMS Mod Compat Support loaded — creative-tab dedup, REI null-filter fix, AR item-drop suppression, creative embargo, Xaero chunk sync.");
    }
}
