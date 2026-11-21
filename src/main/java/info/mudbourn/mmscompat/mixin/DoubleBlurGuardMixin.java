package info.mudbourn.mmscompat.mixin;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Moves the blur up to a second background blur in one frame instead of crashing, as Xaero's waypoint screen asks for one.
@Mixin(GuiRenderState.class)
public abstract class DoubleBlurGuardMixin {

    @Shadow
    private int firstStratumAfterBlur;

    @Inject(method = "blurBeforeThisStratum", at = @At("HEAD"))
    private void mmsCompat$moveBlurUp(CallbackInfo ci) {
        this.firstStratumAfterBlur = Integer.MAX_VALUE;
    }
}
