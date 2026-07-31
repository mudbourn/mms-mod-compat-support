package info.mudbourn.mmscompat.mixin.lean;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmscompat.client.lean.LeanState;
import info.mudbourn.mmscompat.client.lean.LeanTuning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Leans the player's body into their movement and their look direction — the one
 * feature worth keeping from Custom Player Animations, rebuilt clean-room so the
 * mod itself can stay out of the pack.
 *
 * <h2>Why this seam, and not the model</h2>
 *
 * <p>Every animation layer in the pack fights over {@code ModelPart} rotations.
 * DetailedAnimations writes them in {@code setupAnim}; Player Animation Library
 * writes them for Better Combat; EMF re-applies CEM animation later still, from
 * inside {@code ModelPart#render}; and {@code PoseManager} exists precisely
 * because the last writer wins and priority cannot fix it (see
 * {@code HeldPoseMixin}). Anything that leaned the body by rotating bones would
 * join that fight, lose it intermittently, and have to be re-litigated every time
 * one of those mods updates.
 *
 * <p>{@code setupRotations} does not touch the model at all — it transforms the
 * {@link PoseStack} before the model is rendered into it. Applying the lean here
 * puts it strictly above the entire animation stack, where nothing can contend
 * with it and it cannot contend with anything:
 *
 * <ul>
 *   <li><b>No contention.</b> Bone rotations and a pose-stack transform compose;
 *       they do not overwrite each other. No priority, no ordering, no gate.</li>
 *   <li><b>Layers inherit it for free.</b> Armor, elytra, capes, held items and
 *       CEM feature layers all render inside this transform, so they lean with
 *       the body automatically. This is the one class of problem
 *       {@code CemLayerPoseRelay} exists to solve for bone animation, and it
 *       simply does not arise here.</li>
 *   <li><b>EMF-proof.</b> EMF replaces the model wholesale with a jem rig. The
 *       transform is outside the model, so it applies to whatever renders.</li>
 * </ul>
 *
 * <h2>What it deliberately skips</h2>
 *
 * <p>Poses where vanilla already rotates the body hard — swimming and crawling,
 * elytra flight, sleeping, riptide spin, and the death topple. Leaning on top of
 * those compounds two rotations that were never meant to combine, and the result
 * reads as the model detaching rather than as a lean.
 */
@Mixin(AvatarRenderer.class)
public class AvatarRendererLeanMixin {

    @Inject(
            method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("RETURN"))
    private void mms$applyLean(AvatarRenderState state, PoseStack poseStack,
                               float bodyRot, float partialTick, CallbackInfo ci) {
        if (!LeanTuning.enabled) {
            return;
        }
        if (state.isAutoSpinAttack
                || state.deathTime > 0.0F
                || state.hasPose(Pose.SLEEPING)
                || state.hasPose(Pose.SWIMMING)
                || state.hasPose(Pose.FALL_FLYING)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }

        LeanState lean = LeanState.get(player.getUUID()).update(player, state.bodyRot);

        double forward = LeanTuning.invert_forward ? -lean.forwardDegrees() : lean.forwardDegrees();
        double side = LeanTuning.invert_side ? -lean.sideDegrees() : lean.sideDegrees();

        if (forward == 0.0 && side == 0.0) {
            return;
        }

        // Pivot: the stack is at the entity's feet here, which reads as leaning
        // from the ground. Lifting the pivot first moves it toward the hips.
        double pivot = LeanTuning.pivot_height;
        if (pivot != 0.0) {
            poseStack.translate(0.0, pivot, 0.0);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees((float) forward));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) side));
        if (pivot != 0.0) {
            poseStack.translate(0.0, -pivot, 0.0);
        }

        LeanState.expire();
    }
}
