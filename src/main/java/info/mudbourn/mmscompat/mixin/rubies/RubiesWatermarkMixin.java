package info.mudbourn.mmscompat.mixin.rubies;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the Rubies mod's watermark HUD overlay
 * ("MrInvisibleYT" / "youtube.com/@MrInvisiblexYT") rendered on the top-left of the screen.
 * The watermark is the only thing registered in onInitializeClient, so cancelling it is safe.
 */
@Mixin(targets = "com.example.rubies.RubiesClient", remap = false)
public class RubiesWatermarkMixin {

    @Inject(method = "onInitializeClient", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$suppressWatermark(CallbackInfo ci) {
        ci.cancel();
    }
}
