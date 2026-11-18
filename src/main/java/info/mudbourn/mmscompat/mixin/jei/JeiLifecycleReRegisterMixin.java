package info.mudbourn.mmscompat.mixin.jei;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Skips JEI's second reload listener registration when the client restarts after a crash. */
@Pseudo
@Mixin(targets = "mezz.jei.fabric.JustEnoughItemsClient", remap = false)
public class JeiLifecycleReRegisterMixin {

    private static boolean mmsCompat$lifecycleRegistered;

    @Inject(
        method = "lambda$onInitializeClient$4",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void mmsCompat$registerOnce(@Coerce Object lifecycleHandler, Minecraft client, CallbackInfo ci) {
        if (mmsCompat$lifecycleRegistered) {
            ci.cancel();
            return;
        }
        mmsCompat$lifecycleRegistered = true;
    }
}
