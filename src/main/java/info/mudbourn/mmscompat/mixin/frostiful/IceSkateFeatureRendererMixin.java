package info.mudbourn.mmscompat.mixin.frostiful;

import com.github.thedeathlycow.frostiful.client.render.model.IceSkateModel;
import info.mudbourn.mmsrendercommon.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Makes Frostiful's ice skates follow the DetailedAnimations player legs
@Mixin(com.github.thedeathlycow.frostiful.client.render.feature.IceSkateFeatureRenderer.class)
public abstract class IceSkateFeatureRendererMixin {

    @Shadow @Final private IceSkateModel<?> model;

    @Shadow @Final private IceSkateModel<?> babyModel;

    // The wearer's own model, reached by cast rather than @Shadow
    @Unique
    private EntityModel<?> mms$parentModel() {
        return ((RenderLayer<?, ?>) (Object) this).getParentModel();
    }

    @Inject(method = "submit", at = @At("HEAD"))
    private void mms$relaySkatePose(CallbackInfo ci) {
        EntityModel<?> parent = this.mms$parentModel();
        CemLayerPoseRelay.relay(parent, this.model, CemLayerPoseRelay.ICE_SKATES);
        CemLayerPoseRelay.relay(parent, this.babyModel, CemLayerPoseRelay.ICE_SKATES);
    }
}
