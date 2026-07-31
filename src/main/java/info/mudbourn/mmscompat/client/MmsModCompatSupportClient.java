package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.client.lean.LeanTuning;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MmsModCompatSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Unconditional: the lean mixin stands itself down when CPA is present,
        // but the config file should still be written so the knobs are visible.
        LeanTuning.load();
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            SharedWaypointClient.register();
            XaeroGlobalWaypointBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("modmetro")) {
            MetroLineSyncClient.register();
        }
    }
}
