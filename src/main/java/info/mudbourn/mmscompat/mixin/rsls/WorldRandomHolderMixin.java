package info.mudbourn.mmscompat.mixin.rsls;

import info.mudbourn.mmscompat.client.RslsWorldRandoms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes RSLS's {@code WorldRandomHolder} through a weak identity set.
 *
 * <p>RSLS keeps every level's random in a static strong set with no removal.
 * C2ME's {@code CheckedThreadLocalRandom} holds its owning level, so each left
 * dimension stays reachable with all of its chunks. The weak set answers the
 * same membership question and lets unloaded levels be collected.</p>
 */
@Mixin(targets = "com.ishland.fabric.rsls.common.WorldRandomHolder", remap = false)
public abstract class WorldRandomHolderMixin {

    @Inject(method = "putWorldRandom", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmscompat$putWeak(Object random, CallbackInfo ci) {
        RslsWorldRandoms.put(random);
        ci.cancel();
    }

    @Inject(method = "isWorldRandom", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmscompat$isWeak(Object random, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(RslsWorldRandoms.contains(random));
    }
}
