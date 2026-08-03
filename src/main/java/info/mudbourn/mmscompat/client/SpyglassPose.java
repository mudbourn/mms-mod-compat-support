package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Raises the using arm to the eye while a spyglass is up.
 *
 * <h2>Why this is not a pack edit</h2>
 *
 * <p>{@code player.jem} has no spyglass anywhere in it. DetailedAnimations authors
 * exactly two use-item poses — blocking and bow — and writes the arm channels
 * unconditionally the rest of the time:
 *
 * <pre>  "right_arm.rx": "if(is_first_person_hand &amp;&amp; !is_in_gui, right_arm.rx, var.rarx)"</pre>
 *
 * <p>In the first-person hand pass that preserves whatever vanilla wrote; in the
 * third-person pass it discards it. Vanilla's own spyglass pose is written by
 * {@code HumanoidModel#poseRightArm} during {@code setupAnim}, i.e. squarely in
 * the branch DA throws away — so the arm falls to the idle while the item layer
 * still draws the spyglass at the head, and the spyglass floats in front of the
 * face unheld.
 *
 * <p>Unlike the crossbow this is not a modelling disagreement — vanilla's pose is
 * correct and merely lost — so the numbers below are vanilla's, reproduced rather
 * than reinvented. The fix is only about surviving DA, which means going through
 * {@link PoseManager}: it re-applies over the CEM animation and so beats it by
 * construction. See {@link CrossbowPose} for the same seam used for a case where
 * the pose genuinely had to be authored fresh.
 *
 * <h2>Scope</h2>
 *
 * <p>Only while the spyglass is actually in use. A spyglass merely held has no
 * vanilla pose either, and DA's idle arms are the right answer for it — the bug
 * is the raise, not the carry, so there is nothing to keep continuous here and no
 * per-player timer to run.
 *
 * <p>Only the using arm is stored; the other slot is left null, which EMF Compat's
 * applier skips, so the free arm keeps DA's idle swing instead of being frozen.
 *
 * <p>Matched on {@link ItemUseAnimation#SPYGLASS} rather than on {@code Items.SPYGLASS}
 * so a modded scope that declares the vanilla use animation — and therefore gets the
 * vanilla arm pose, and therefore loses it to DA the same way — is covered without
 * an item list to maintain.
 */
public final class SpyglassPose {

    /** Distinct from {@code mms_held_pose} and {@code mms_crossbow}; PoseManager merges per part, per source. */
    public static final String SOURCE = "mms_spyglass";

    /** Vanilla's raise, from {@code HumanoidModel#poseRightArm}. */
    private static final float RAISE = 1.9198622f;

    /** Vanilla's inward yaw, and its extra crouch pitch — the same magnitude, coincidentally. */
    private static final float YAW = 0.2617994f;

    private static final float CROUCH_PITCH = 0.2617994f;

    private static final float MIN_PITCH = -2.4f;

    private static final float MAX_PITCH = 3.3f;

    /** Who currently has a pose stored, so release costs nothing for everyone else. */
    private static final Set<UUID> POSED = new HashSet<>();

    private SpyglassPose() {}

    /**
     * @return true if this class posed an arm, meaning no other producer should.
     */
    public static boolean apply(AbstractClientPlayer player, PlayerModel model) {
        if (!player.isUsingItem()) {
            release(player.getUUID());
            return false;
        }

        ItemStack stack = player.getUseItem();
        if (stack.isEmpty() || stack.getUseAnimation() != ItemUseAnimation.SPYGLASS) {
            release(player.getUUID());
            return false;
        }

        // The arm actually holding it — off-hand use raises the other arm.
        HumanoidArm arm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        boolean right = arm == HumanoidArm.RIGHT;

        // Injected at RETURN of setupAnim, so head is already set from the render
        // state — the same point in the frame vanilla's own poseRightArm reads it.
        float pitch = Math.clamp(
                model.head.xRot - RAISE - (player.isCrouching() ? CROUCH_PITCH : 0.0f),
                MIN_PITCH, MAX_PITCH);
        // Opposite yaw signs per arm: a raised arm's yRot swings it toward the
        // player's left when positive, so the right arm needs negative yaw to come
        // inward to the eye. Matching signs would splay it outward.
        float yaw = model.head.yRot + (right ? -YAW : YAW);

        var posed = right ? model.rightArm : model.leftArm;

        // Author onto the live part, snapshot, then put it back: the snapshot is
        // re-applied after the CEM animation anyway, and leaving the model mutated
        // here would be a side effect on anything that reads it in between.
        PoseSnapshot before = new PoseSnapshot(posed);

        posed.xRot = pitch;
        posed.yRot = yaw;
        posed.zRot = 0.0f;

        PoseSnapshot pose = new PoseSnapshot(posed);

        before.apply(posed);

        PoseManager.savePoses(player.getUUID(), SOURCE,
                right ? null : pose,
                right ? pose : null);
        POSED.add(player.getUUID());
        return true;
    }

    /** Drops the pose in the same frame the spyglass comes down. */
    public static void release(UUID id) {
        if (POSED.remove(id)) {
            PoseManager.clearPoses(id, SOURCE);
        }
    }
}
