package info.mudbourn.mmscompat.mixin.jeg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses JEG's creative tab entries as part of the server embargo.
 *
 * <p>Items stay registered, so operators can still obtain them via {@code /give} and
 * killstreak rewards continue to function. This only removes them from the creative
 * and recipe-viewer menus.
 */
@Mixin(targets = "ttv.migami.jeg.fabric.FabricCreativeTabs", remap = false)
public class JegCreativeTabMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$suppressCreativeTabs(CallbackInfo ci) {
        ci.cancel();
    }
}
