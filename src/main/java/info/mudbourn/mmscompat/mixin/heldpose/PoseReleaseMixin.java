package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.PlayerAnimLayerProbe;
import info.mudbourn.mmscompat.client.PoseBlend;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Drives the arm blend, at the end of EMF's animation.
 *
 * <h2>What it feeds in</h2>
 *
 * <p>{@link PoseBlend} needs two things per frame: who owns the arms, and what they
 * are being set to. Both are read here, and both are read in a way that does not
 * depend on whether EMF Compat's restore has already run at this shared injection
 * point — a question this package documents contradictory rules for and therefore
 * refuses to rely on.
 *
 * <ul>
 *   <li><b>Owner</b> is the set of {@code PoseManager} source keys currently holding
 *       an arm, joined into one token. Every authority that goes through the store —
 *       Better Combat swings, this mod's held poses, Inspect Animations — is
 *       identified by construction, and a source added later is picked up with no
 *       change here. Combat Roll drives its animation through Player Animation Lib
 *       and stores nothing, so it is asked for separately; without that it would be
 *       indistinguishable from DetailedAnimations and the end of a roll would not
 *       ease, which is the case this easing was first asked for.</li>
 *   <li><b>Target</b> comes from the store for an arm some source claimed, since the
 *       snapshot is what the restore is going to write whenever it runs, and off the
 *       model for an arm nobody claimed, where DA's animation has just landed and the
 *       restore has nothing to say.</li>
 * </ul>
 *
 * <p>The owner token deliberately ignores <em>which</em> arms each source holds and
 * what it is holding them at. A source that keeps ownership across a change of pose —
 * a posed weapon swapped for another posed weapon — is one continuous authority as
 * far as this is concerned, and its own animation is expected to be continuous. It is
 * the handover between authorities that nobody was smoothing.
 *
 * <p>First person is skipped for the same reason the rest of this package skips it:
 * {@code EMFModelPartRootMixin} does not restore poses there, and the first-person
 * path owns the held-item arm.
 */
@Mixin(value = EMFModelPartRoot.class, priority = 300, remap = false)
public class PoseReleaseMixin {

    @Inject(method = "animate", at = @At("RETURN"))
    private void mms$easeArmHandover(CallbackInfo ci) {
        EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            return;
        }
        UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null || strm.emfcompat.core.EMFCompatCore.isLocalPlayerInFirstPerson(uuid)) {
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

        Map<String, SavedPoses> bySource = PoseManager.entitySavedPosesBySource.get(uuid);
        PoseBlend.frame(uuid, mms$owner(uuid, bySource),
                PoseManager.getSavedPoses(uuid), bySource, left, right);
    }

    /**
     * A stable token for whoever holds the arms this frame.
     *
     * <p>Sorted, because {@code PoseManager} stores sources in a {@code HashMap} and
     * iteration order is not a fact about who owns anything — an unsorted join would
     * spuriously change token, and so start a transition, on a rehash.
     */
    private static String mms$owner(UUID uuid, Map<String, SavedPoses> bySource) {
        List<String> owners = new ArrayList<>(2);
        if (bySource != null) {
            for (Map.Entry<String, SavedPoses> entry : bySource.entrySet()) {
                SavedPoses saved = entry.getValue();
                if (saved != null && (saved.leftArm() != null || saved.rightArm() != null)) {
                    owners.add(entry.getKey());
                }
            }
        }
        if (mms$rolling(uuid)) {
            owners.add(PoseBlend.COMBAT_ROLL);
        }
        if (owners.isEmpty()) {
            return PoseBlend.DETAILED_ANIMATIONS;
        }
        Collections.sort(owners);
        return String.join("+", owners);
    }

    /**
     * Combat Roll drives its animation through Player Animation Lib and never touches
     * {@code PoseManager}, so the only honest question is whether its layer is active.
     * Asking the player's {@code RollManager} instead would be wrong for every player
     * but the local one.
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
