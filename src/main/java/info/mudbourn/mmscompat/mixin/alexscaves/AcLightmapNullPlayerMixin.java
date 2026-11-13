package info.mudbourn.mmscompat.mixin.alexscaves;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Returns no Alex's Caves ambient light bonus while no player is loaded.
@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.render.ACLightmapAdditions", remap = false)
public class AcLightmapNullPlayerMixin {

    @Inject(method = "ambientBonus", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mms$noPlayerNoBonus(CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getInstance().player == null) {
            cir.setReturnValue(0.0F);
        }
    }
}
