package info.mudbourn.mmscompat.mixin.mapsync;

import info.mudbourn.mmscompat.mapsync.MapSyncScanScheduler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Dispatches MapSyncer's scheduled incremental scan onto a background thread.
 *
 * <p>{@code performScheduledUpdate} is reached from {@code onServerTick}, so the
 * {@code performIncrementalScan} call it makes runs inside the tick and stalls it for as long as
 * the region walk takes. Redirecting that one call hands the work to
 * {@link MapSyncScanScheduler} and lets the tick return immediately.
 */
@Mixin(targets = "com.mapsyncer.server.IncrementalUpdateHandlerLogic", remap = false)
public class ScheduledScanOffThreadMixin {

    @Redirect(
            method = "performScheduledUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mapsyncer/server/ConversionOrchestrator;"
                            + "performIncrementalScan(Lnet/minecraft/server/MinecraftServer;)V"
            )
    )
    private void mmsCompat$scanOffThread(MinecraftServer server) {
        MapSyncScanScheduler.submit(server, com.mapsyncer.server.ConversionOrchestrator::performIncrementalScan);
    }
}
