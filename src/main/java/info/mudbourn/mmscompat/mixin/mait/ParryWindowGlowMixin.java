package info.mudbourn.mmscompat.mixin.mait;

import com.llamalad7.mixinextras.sugar.Local;
import info.mudbourn.mmscompat.client.mait.ParryWindowGlow;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Draws the first person shield at full brightness while its perfect parry window is open.
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ParryWindowGlowMixin {

    private static final int FULL_BRIGHT = 0xF000F0;

    @ModifyVariable(method = "submitArmWithItem", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int mmsCompat$glowInParryWindow(int light, @Local(argsOnly = true) ItemStack stack) {
        return ParryWindowGlow.glowing(stack) ? FULL_BRIGHT : light;
    }
}
