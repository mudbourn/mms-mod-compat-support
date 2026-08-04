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
 * Keeps the throw wind-up on the arm while a trident is being cocked back.
 *
 * <h2>Why this is not a pack edit</h2>
 *
 * <p>{@code player.jem} contains no trident and no spear anywhere in it —
 * DetailedAnimations authors exactly two use-item poses, blocking and bow, and
 * writes the arm channels unconditionally the rest of the time:
 *
 * <pre>  "right_arm.rx": "if(is_first_person_hand &amp;&amp; !is_in_gui, right_arm.rx, var.rarx)"</pre>
 *
 * <p>Vanilla's wind-up is written by {@code HumanoidModel#poseRightArm} for
 * {@code ArmPose#THROW_SPEAR} during {@code setupAnim}, i.e. squarely in the branch
 * DA discards in third person. So the arm never rises: the trident is carried at
 * DA's idle while the player is plainly charging a throw. Exactly the spyglass
 * bug, on a different item — see {@link SpyglassPose}.
 *
 * <p>As with the spyglass this is not a modelling disagreement. Vanilla's pose is
 * correct and merely lost, so nothing here is invented; the fix is only about
 * surviving DA, which means going through {@link PoseManager}, whose contents are
 * re-applied over the CEM animation and therefore beat it by construction.
 *
 * <h2>Why the pose is snapshotted rather than recomputed</h2>
 *
 * <p>{@link SpyglassPose} reproduces vanilla's numbers because they are absolute.
 * {@code THROW_SPEAR} is not — it is <em>relative</em> to whatever the arm already
 * held:
 *
 * <pre>  arm.xRot = arm.xRot * 0.5F - (float)Math.PI;
 *  arm.yRot = 0.0F;</pre>
 *
 * <p>By {@code RETURN} of {@code setupAnim} vanilla has already applied that once,
 * so the live part <em>is</em> the pose; re-running the formula here would halve
 * the walk swing a second time and subtract a second pi. Capturing the part is
 * therefore both simpler and exact, and it inherits any future change to vanilla's
 * numbers for free.
 *
 * <p>Nothing else has written the arm by that point for the items this covers.
 * {@code bettercombat:trident} — which {@code fallback_compatibility.json} maps
 * {@code trident|javelin|impaled} onto, so {@code bountiful-fish:elder_trident}
 * lands there too — declares {@code "pose": ""}, so Better Combat applies no PAL
 * pose to overwrite it, and an attack animation cannot be playing while the throw
 * is being charged.
 *
 * <h2>Scope</h2>
 *
 * <p>Matched on {@link ItemUseAnimation#SPEAR} rather than on {@code TridentItem}
 * or an item list, so anything that declares the vanilla use animation — and hence
 * gets the vanilla arm pose, and hence loses it to DA the same way — is covered
 * with nothing to maintain. {@code bountiful-fish:elder_trident} is an
 * {@code ElderTridentItem extends TridentItem} and so is already in by inheritance;
 * the match is on the animation so that a future throwable pike or dagger is too.
 *
 * <p>Gated additionally on {@code getUseItemRemainingTicks() > 0}, which is the
 * same condition vanilla uses to select {@code THROW_SPEAR} in the first place —
 * without it this class would claim a frame in which vanilla wrote no pose, and
 * would capture the idle arm as if it were a wind-up.
 *
 * <p>Only the using arm is stored; the other slot is left null, which EMF Compat's
 * applier skips, so the free arm keeps DA's idle swing instead of being frozen.
 * Only while the trident is actually being charged, too — a trident merely held has
 * no vanilla pose either, and DA's idle arms are the right answer for it.
 */
public final class TridentPose {

    /** Distinct from {@code mms_held_pose}, {@code mms_crossbow} and {@code mms_spyglass}; PoseManager merges per part, per source. */
    public static final String SOURCE = "mms_trident";

    /** Who currently has a pose stored, so release costs nothing for everyone else. */
    private static final Set<UUID> POSED = new HashSet<>();

    private TridentPose() {}

    /**
     * @return true if this class posed an arm, meaning no other producer should.
     */
    public static boolean apply(AbstractClientPlayer player, PlayerModel model) {
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0) {
            release(player.getUUID());
            return false;
        }

        ItemStack stack = player.getUseItem();
        if (stack.isEmpty() || stack.getUseAnimation() != ItemUseAnimation.SPEAR) {
            release(player.getUUID());
            return false;
        }

        // The arm actually holding it — off-hand use winds up the other arm.
        HumanoidArm arm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        boolean right = arm == HumanoidArm.RIGHT;

        PoseSnapshot pose = new PoseSnapshot(right ? model.rightArm : model.leftArm);

        PoseManager.savePoses(player.getUUID(), SOURCE,
                right ? null : pose,
                right ? pose : null);
        POSED.add(player.getUUID());
        return true;
    }

    /** Drops the pose in the same frame the throw is released or cancelled. */
    public static void release(UUID id) {
        if (POSED.remove(id)) {
            PoseManager.clearPoses(id, SOURCE);
        }
    }
}
