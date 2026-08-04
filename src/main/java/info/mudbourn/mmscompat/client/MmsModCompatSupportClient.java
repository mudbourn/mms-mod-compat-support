package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.client.lean.LeanTuning;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorSets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

public class MmsModCompatSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Unconditional: the lean mixin stands itself down when CPA is present,
        // but the config file should still be written so the knobs are visible.
        LeanTuning.load();
        // Held-pose arm mode. Loaded unconditionally so the file exists even when
        // the mixins are gated off, and so /mmspose can report the current mode.
        PoseTuning.load();
        PoseCommand.register();
        // Clothing of the Lowlands vanity sets carry custom geometry, so the model
        // layers have to exist before anything wearing one is rendered. Registered
        // unconditionally: the sets are keyed by equipment asset id, and a player
        // who never sees one just pays for a few unused baked models.
        LowlandsArmorSets.registerModelLayers();
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            SharedWaypointClient.register();
            XaeroGlobalWaypointBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("modmetro")) {
            MetroLineSyncClient.register();
        }
        if (FabricLoader.getInstance().isModLoaded("particlerain")) {
            ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> ParticleRainBiomeMemo.reset());
        }
    }
}
