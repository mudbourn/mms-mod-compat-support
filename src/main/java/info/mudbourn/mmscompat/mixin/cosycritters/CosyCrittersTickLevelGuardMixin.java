package info.mudbourn.mmscompat.mixin.cosycritters;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;

// Skips Cosy Critters' client tick while the level is unloaded
@Mixin(value = CosyCritters.class, remap = false)
public class CosyCrittersTickLevelGuardMixin {

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void mms$skipWhenLevelNull(Minecraft minecraft, CallbackInfo ci) {
        if (minecraft.level == null) {
            ci.cancel();
        }
    }
}
