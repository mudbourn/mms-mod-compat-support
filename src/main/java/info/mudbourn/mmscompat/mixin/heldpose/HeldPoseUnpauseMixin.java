package info.mudbourn.mmscompat.mixin.heldpose;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import info.mudbourn.mmscompat.client.HeldPoseSource;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import strm.emfcompat.core.PoseManager;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;

import java.util.UUID;

/**
 * Keeps EMF's entity animation running while a Better Combat held pose is stored
 * under this mod's source key.
 *
 * <h2>Why the held pose alone was not enough</h2>
 *
 * <p>{@link HeldPoseMixin} stashes both arms under {@code mms_held_pose} so they
 * survive DetailedAnimations. That fixes who writes last, but not whether the
 * animation runs at all. EMF Compat's Better Combat addon also decides, every
 * frame, whether the CEM animation is paused — in
 * {@code EMFAnimationEntityContextMixin}:
 *
 * <pre>{@code
 * return AttackPauseOverride.isUnpaused(uuid)
 *         ? false
 *         : PoseManager.getSavedPoses(uuid, "better_combat") == null;
 * }</pre>
 *
 * <p>On an idle hold no attack is playing, so {@code isUnpaused} is false, and the
 * addon's own producer cleared {@code "better_combat"} on that same frame — so the
 * expression is {@code null == null}, and the method reports <em>paused</em>.
 *
 * <p>The gate consults exactly one source. {@code mms_held_pose} — chosen so the
 * two producers would never contend for a slot — is invisible to it. So the pose is
 * stored and the animation that would have shown it is switched off in the same
 * frame, the jem collapses, and the arms fall back to a free swing. That is the
 * whole reason two-handed weapons swing while held: weapons with no pose have
 * nothing to lose, which is why one-handed swords and maces always looked right.
 *
 * <p>This mirrors the addon's own reasoning for our key. It only ever turns a
 * {@code true} into {@code false} — it can pause nothing that was not already
 * running, and it defers to {@code entitiesPaused}, which is EMF's explicit
 * user-facing pause and not ours to override.
 */
@Mixin(value = EMFAnimationEntityContext.class, remap = false)
public class HeldPoseUnpauseMixin {

    @ModifyReturnValue(method = "isEntityAnimPaused", at = @At("RETURN"))
    private static boolean mms$unpauseWhileHoldingPosedWeapon(boolean original) {
        // Only ever un-pause. If EMF already intends to animate, leave it alone.
        if (!original) {
            return false;
        }
        if (Minecraft.getInstance().level == null) {
            return original;
        }

        EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            return original;
        }

        UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null) {
            return original;
        }

        // EMF's own explicit pause set outranks this. Matching the addon here
        // matters: overriding it would make a deliberately frozen entity animate.
        if (EMFAnimationEntityContext.entitiesPaused.contains(uuid)) {
            return original;
        }

        return PoseManager.getSavedPoses(uuid, HeldPoseSource.SOURCE) != null ? false : original;
    }
}
