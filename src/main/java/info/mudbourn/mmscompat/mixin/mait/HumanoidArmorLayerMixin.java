package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    // Mob AI Tweaks' baby skeletons are the adult skeleton model drawn at half size, so their armor is the adult
    // armor (the layer's default baby set) with the standard textures, not the baby textures made for the zombie baby body.
    @Redirect(
        method = "renderArmorPiece",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;isBaby:Z"
        )
    )
    private boolean mms_compat$skeletonArmorTexture(HumanoidRenderState state) {
        return state.isBaby && !(state instanceof SkeletonRenderState);
    }
}
