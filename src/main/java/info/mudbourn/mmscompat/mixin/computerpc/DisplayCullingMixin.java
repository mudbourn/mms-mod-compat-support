package info.mudbourn.mmscompat.mixin.computerpc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Restores frustum culling and a shorter view distance for ComputerPC displays.
@Mixin(targets = "justpc.computerpc.client.render.DisplayBlockEntityRenderer")
public abstract class DisplayCullingMixin {

    private static final int VIEW_DISTANCE = 50;

    @Inject(method = "shouldRenderOffScreen", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$cullOffScreenDisplays(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getViewDistance", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$shortenDisplayViewDistance(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VIEW_DISTANCE);
    }
}
