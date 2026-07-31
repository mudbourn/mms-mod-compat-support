package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.HeldPoseDelta;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import strm.emfcompat.core.EMFCompatCore;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.parts.EMFModelPartRoot;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.UUID;

/**
 * Adds the held-weapon pose on top of DetailedAnimations' arms instead of replacing
 * them, so DA keeps deciding when the arms swing and this only decides how they
 * hold the weapon.
 *
 * <h2>Why this exists rather than reusing PoseManager</h2>
 *
 * <p>{@code PoseSnapshot.applyRotation} writes absolute {@code xRot/yRot/zRot}.
 * There is no additive path through EMF Compat's store, so storing the hold there
 * necessarily discards DA's animation for that part — including DA's gate on being
 * airborne, which is the entire bug this fixes. {@link HeldPoseMixin} therefore
 * stores a null-armed marker in {@code PoseManager} purely to keep
 * {@link HeldPoseUnpauseMixin}'s gate open, stashes the real contribution in
 * {@link HeldPoseDelta}, and this adds it here.
 *
 * <h2>Ordering</h2>
 *
 * <p>Same seam as {@code EMFModelPartRootMixin} — {@code RETURN} of
 * {@code EMFModelPartRoot#animate} — which is a contested seam, so the order is
 * pinned rather than left to chance. Mixins are applied in ascending priority, and
 * for injections at a shared point the mixin applied <em>first</em> runs
 * <em>last</em>. EMF Compat declares no priority, so it is at the default 1000;
 * this sits below it at 500 and therefore runs after it. That matters: the addon's
 * attack poses are absolute writes, and an attack in progress should still win the
 * arm outright. Adding on top of an attack pose is the correct composition — the
 * attack is the animation, the hold is the offset.
 *
 * <p>First person is skipped for the same reason EMF Compat skips it: the
 * first-person path owns the held-item arm.
 */
@Mixin(value = EMFModelPartRoot.class, priority = 500, remap = false)
public class HeldPoseAdditiveMixin {

    @Inject(method = "animate", at = @At("RETURN"))
    private void mms$addHeldPoseOverAnimation(CallbackInfo ci) {
        EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            return;
        }

        UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null || EMFCompatCore.isLocalPlayerInFirstPerson(uuid)) {
            return;
        }

        HeldPoseDelta.ArmDelta delta = HeldPoseDelta.get(uuid);
        if (delta == null) {
            return;
        }

        EMFModelPartRoot root = (EMFModelPartRoot) (Object) this;
        for (EMFModelPartVanilla part : root.getAllVanillaPartsEMF()) {
            // toStringShort() carries EMF's own decoration around the vanilla name;
            // matching on containment is what the addon does at this same seam.
            String name = part.toStringShort();
            if (name == null) {
                continue;
            }
            if (name.contains("left_arm")) {
                mms$add(part, delta.leftXRot(), delta.leftYRot(), delta.leftZRot());
            } else if (name.contains("right_arm")) {
                mms$add(part, delta.rightXRot(), delta.rightYRot(), delta.rightZRot());
            }
        }
    }

    private static void mms$add(ModelPart part, float xRot, float yRot, float zRot) {
        part.xRot += xRot;
        part.yRot += yRot;
        part.zRot += zRot;
    }
}
