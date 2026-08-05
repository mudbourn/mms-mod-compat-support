package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.PlayerAnimLayerProbe;
import info.mudbourn.mmscompat.client.PoseRelease;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.SavedPoses;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.parts.EMFModelPartRoot;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.UUID;

/**
 * Runs the arm release blend, at the end of EMF's animation.
 *
 * <h2>Where the two cases are read</h2>
 *
 * <p>"Owned" is the union of every authority that can hold the arms, because the
 * blend should look the same however the pose was being driven:
 *
 * <ul>
 *   <li>A {@code PoseManager} entry with either arm set — Better Combat swings, this
 *       mod's held poses, Inspect Animations, and anything else that registers a
 *       source. The anchor comes straight off the {@code PoseSnapshot}, which is
 *       what makes it independent of whether EMF Compat's restore has already run;
 *       see {@link PoseRelease}.</li>
 *   <li>Combat Roll's PAL layer, which stores nothing and has to be sampled off the
 *       model. This is the case the request was actually about — the end of a roll
 *       being visible because it cuts rather than settles.</li>
 * </ul>
 *
 * <p>A Better Combat swing satisfies both, and the store is preferred; that is the
 * ordering-proof path, so it should win whenever it is available.
 *
 * <h2>Priority</h2>
 *
 * <p>Low, to run late at this shared injection point — but the correctness of the
 * blend does not depend on that, which is the point of anchoring to the store. If
 * this runs before EMF Compat's restore, the restore then overwrites the blended
 * arms on an owned frame, which is fine because an owned frame is not being blended;
 * on an unowned frame the restore has nothing to write and the blend survives
 * either way.
 *
 * <p>First person is skipped for the same reason the rest of this package skips it.
 */
@Mixin(value = EMFModelPartRoot.class, priority = 300, remap = false)
public class PoseReleaseMixin {

    @Inject(method = "animate", at = @At("RETURN"))
    private void mms$easeArmRelease(CallbackInfo ci) {
        EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            return;
        }
        UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null || strm.emfcompat.core.EMFCompatCore.isLocalPlayerInFirstPerson(uuid)) {
            return;
        }

        SavedPoses saved = PoseManager.getSavedPoses(uuid);
        if (saved != null && saved.leftArm() != null && saved.rightArm() != null) {
            PoseRelease.owned(uuid,
                    saved.leftArm().xRot, saved.leftArm().yRot, saved.leftArm().zRot,
                    saved.rightArm().xRot, saved.rightArm().yRot, saved.rightArm().zRot);
            return;
        }

        ModelPart left = null;
        ModelPart right = null;
        EMFModelPartRoot root = (EMFModelPartRoot) (Object) this;
        for (EMFModelPartVanilla part : root.getAllVanillaPartsEMF()) {
            String name = part.toStringShort();
            if (name == null) {
                continue;
            }
            if (left == null && name.contains("left_arm")) {
                left = part;
            } else if (right == null && name.contains("right_arm")) {
                right = part;
            }
        }
        if (left == null || right == null) {
            return;
        }

        if (mms$rolling(uuid)) {
            PoseRelease.owned(uuid,
                    left.xRot, left.yRot, left.zRot,
                    right.xRot, right.yRot, right.zRot);
            return;
        }

        PoseRelease.releasing(uuid, left, right);
    }

    /**
     * Combat Roll drives its animation through Player Animation Lib and never
     * touches {@code PoseManager}, so the only honest question is whether its layer
     * is active. Asking the player's {@code RollManager} instead would be wrong for
     * every player but the local one.
     */
    private static boolean mms$rolling(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        return mc.level.getEntity(uuid) instanceof Avatar avatar
                && PlayerAnimLayerProbe.isPlaying(avatar, PlayerAnimLayerProbe.COMBAT_ROLL);
    }
}
