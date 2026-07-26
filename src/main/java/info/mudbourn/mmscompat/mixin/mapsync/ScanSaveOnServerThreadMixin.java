package info.mudbourn.mmscompat.mixin.mapsync;

import info.mudbourn.mmscompat.mapsync.MapSyncScanScheduler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Keeps the pre-scan world save on the server thread once the scan itself moves off it.
 *
 * <p>{@code performIncrementalScan} flushes the world before reading region files, so the data on
 * disk is current. Everything else it touches is either file I/O or an immutable read
 * ({@code getLevel}, {@code dimensionType}, {@code location}), but {@code saveEverything} writes
 * chunk storage and must not run from
 * {@link MapSyncScanScheduler}'s pool thread. This bounces just that call back to the tick and
 * waits for it.
 *
 * <p>{@code remap = true} on the redirect: the enclosing target is MapSyncer's own class and is
 * not remapped, but {@code saveEverything} is a Minecraft method and needs mapping to the
 * intermediary name at build time.
 */
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
