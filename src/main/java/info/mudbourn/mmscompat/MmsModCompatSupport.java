package info.mudbourn.mmscompat;

import info.mudbourn.mmscompat.bubble.BubbleContent;
import info.mudbourn.mmscompat.ca.LanternHolster;
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

        // PvP arrow limit
        info.mudbourn.mmscompat.ranged.PvpArrowLimit.register();

        // Carpet hello payload registration
        if (FabricLoader.getInstance().isModLoaded("carpet")) {
            CarpetPayloadRegistration.register();
        }

        // Belt-slot lantern warmth and heat penalty
        if (FabricLoader.getInstance().isModLoaded("combatamenities")
            && FabricLoader.getInstance().isModLoaded("frostiful")) {
            LanternHolster.register();
        }

        // Perfect blocks skip Combat Amenities' shield cooldown
        if (FabricLoader.getInstance().isModLoaded("combatamenities")) {
            info.mudbourn.mmscompat.ca.PerfectBlockCooldown.register();
        }

        // A Totem of Possession worn in a trinket slot, used with the possess key
        if (FabricLoader.getInstance().isModLoaded("alexscaves") && FabricLoader.getInstance().isModLoaded("trinkets")) {
            info.mudbourn.mmscompat.alexscaves.WornPossession.register();
        }

        // Bubblellaneous Java port
        if (FabricLoader.getInstance().isModLoaded("aerialhell")) {
            info.mudbourn.mmscompat.aerialhell.AerialHellMobStats.register();
            info.mudbourn.mmscompat.aerialhell.TrinketTotems.register();
        }

        if (FabricLoader.getInstance().isModLoaded("ferrite")) {
            info.mudbourn.mmscompat.ferrite.FerriteOreOff.register();
        }

        if (FabricLoader.getInstance().isModLoaded("mr_bubblellaneous")) {
            BubbleContent.register();
        }

        // Xaero's World Map chunk sync (server-side packet handler)

        LOG.info("MMS Mod Compat Support loaded - creative-tab dedup, REI null-filter fix, creative embargo, Xaero chunk sync.");
    }
}
