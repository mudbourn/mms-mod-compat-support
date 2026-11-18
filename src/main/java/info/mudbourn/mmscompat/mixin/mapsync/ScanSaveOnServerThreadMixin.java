package info.mudbourn.mmscompat.mixin.mapsync;

import info.mudbourn.mmscompat.mapsync.MapSyncScanScheduler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Runs the pre-scan world save on the server thread
@Mixin(targets = "com.mapsyncer.server.ConversionOrchestrator", remap = false)
public class ScanSaveOnServerThreadMixin {

    @Redirect(
            method = "performIncrementalScan",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;saveEverything(ZZZ)Z",
                    remap = true
            ),
            remap = true
    )
    private static boolean mmsCompat$saveOnServerThread(MinecraftServer server, boolean suppressLogs,
                                                        boolean flush, boolean forced) {
        return MapSyncScanScheduler.saveOnServerThread(server, suppressLogs, flush, forced);
    }
}
