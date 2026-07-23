package info.mudbourn.mmscompat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MmsModCompatSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            SharedWaypointClient.register();
            XaeroGlobalWaypointBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("modmetro")) {
            MetroLineSyncClient.register();
        }
    }
}
