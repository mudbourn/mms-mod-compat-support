package info.mudbourn.mmscompat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

public class MmsModCompatSupportClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // ETF NBT fast path. Loaded unconditionally so the file and /mmsnbt exist
        // even without ETF installed; the mixin itself is gated on the mod.
        info.mudbourn.mmscompat.client.etfnbt.NbtTuning.load();
        info.mudbourn.mmscompat.client.etfnbt.NbtCommand.register();
        if (FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            SharedWaypointClient.register();
            XaeroGlobalWaypointBridge.register();
        }
        if (FabricLoader.getInstance().isModLoaded("particlerain")) {
            ClientPlayConnectionEvents.DISCONNECT.register(
                (handler, client) -> ParticleRainBiomeMemo.reset());
        }
        // Camera Glue (was the standalone camera-glue mod): force first person
        // during Cinematic Respawn death sequences so Third Person's camera does
        // not fight the cinematic. Only wire it up when all three mods it bridges
        // are present — the guard keeps CameraGlue and its third-party imports
        // from being linked otherwise.
        if (FabricLoader.getInstance().isModLoaded("leawind_third_person")
                && FabricLoader.getInstance().isModLoaded("perspective_api")
                && FabricLoader.getInstance().isModLoaded("cinematic_respawn")) {
            info.mudbourn.mmscompat.client.camera.CameraGlue.register();
        }
    }
}
