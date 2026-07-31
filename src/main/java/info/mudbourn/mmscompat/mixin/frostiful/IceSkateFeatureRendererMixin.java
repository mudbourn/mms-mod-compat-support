package info.mudbourn.mmscompat.mixin.frostiful;

import com.github.thedeathlycow.frostiful.client.render.model.IceSkateModel;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Frostiful's ice skates follow the DetailedAnimations player legs.
 *
 * <p>{@code IceSkateFeatureRenderer} never animates its model at all — there is
 * no {@code setupAnim} call anywhere in {@code submit}. It takes the parent
 * model's root, applies that one transform to the pose stack, and submits an
 * otherwise untouched {@code IceSkateModel}. Under vanilla animation that is
 * invisible, because the layer is drawn inside the same pose the legs were drawn
 * in. Under CEM it is not: EMF animates the player's parts from inside
 * {@code ModelPart#render}, so the leg the skate belongs to has moved and the
 * skate has not.
 *
 * <p>This is the same shape of bug as the spider spikes, but the easy case: EMF's
 * own {@code EMFBipedPose} path does not reach here (it is wired only into
 * {@code HumanoidArmorLayer}), while the model itself is a
 * {@code HumanoidModel} built from the vanilla layer definition — so names and
 * pivots already agree and {@link CemLayerPoseRelay#ICE_SKATES} is the identity.
 * The relay's pivot-absorbing algebra costs nothing in that case and keeps the
 * mapping honest if Frostiful reshapes the model later.
 *
 * <p>Injected at {@code HEAD} rather than after an animation call because there
 * is none, and nothing between here and the submit touches the part fields the
 * relay writes. Both models are relayed since the baby/adult choice is made
 * further down; posing the one that will not be submitted costs a part-tree walk
 * and no draw.
 *
 * <p>Note the renderer applies the parent root to the pose stack itself. The
 * relay accumulates from the children of the root down and never folds the root
 * in, so the two do not compound.
 */
@Mixin(com.github.thedeathlycow.frostiful.client.render.feature.IceSkateFeatureRenderer.class)
public abstract class IceSkateFeatureRendererMixin {

    @Shadow @Final private IceSkateModel<?> model;

    @Shadow @Final private IceSkateModel<?> babyModel;

    @Shadow protected abstract EntityModel<?> getParentModel();

    @Inject(method = "submit", at = @At("HEAD"))
    private void mms$relaySkatePose(CallbackInfo ci) {
        EntityModel<?> parent = this.getParentModel();
        CemLayerPoseRelay.relay(parent, this.model, CemLayerPoseRelay.ICE_SKATES);
        CemLayerPoseRelay.relay(parent, this.babyModel, CemLayerPoseRelay.ICE_SKATES);
    }
}
