package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The baby hand offset is made for the baby zombie body. Mob AI Tweaks' baby skeletons are the adult model at half
// size, so the offset pulled their bows in toward the armpit; they hold items the way an adult skeleton does.
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Inject(method = "useBabyOffset", at = @At("HEAD"), cancellable = true)
    private void mms_compat$noSkeletonBabyOffset(ArmedEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof SkeletonRenderState) {
            cir.setReturnValue(false);
        }
    }
}
