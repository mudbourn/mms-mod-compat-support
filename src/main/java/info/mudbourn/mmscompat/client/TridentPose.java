package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps the throw wind-up on the arm while a trident or spear is being cocked back.
 *
 * <h2>Why this is not a pack edit</h2>
 *
 * <p>{@code player.jem} contains no trident and no spear anywhere in it —
 * DetailedAnimations authors exactly two use-item poses, blocking and bow, and
 * writes the arm channels unconditionally the rest of the time:
 *
 * <pre>  "right_arm.rx": "if(is_first_person_hand &amp;&amp; !is_in_gui, right_arm.rx, var.rarx)"</pre>
 *
 * <p>Vanilla's wind-up is written by {@code HumanoidModel#poseRightArm} during
 * {@code setupAnim}, i.e. squarely in the branch DA discards in third person. So the
 * arm never rises: the trident is carried at DA's idle while the player is plainly
 * charging a throw. Exactly the spyglass bug, on a different item — see
 * {@link SpyglassPose}.
 *
 * <p>As with the spyglass this is not a modelling disagreement. Vanilla's pose is
 * correct and merely lost, so nothing here is invented; the fix is only about
 * surviving DA, which means going through {@link PoseManager}, whose contents are
 * re-applied over the CEM animation and therefore beat it by construction.
 *
 * <h2>What it matches, and the bug that hid in the old answer</h2>
 *
 * <p>The match is on {@link HumanoidModel.ArmPose} taken straight off the render
 * state, not on the item's {@link net.minecraft.world.item.ItemUseAnimation}. That is
 * a correction, not a refinement: this class previously tested for
 * {@code ItemUseAnimation.SPEAR}, which in 1.21.11 is the <em>new spear</em>
 * animation and not the trident's. A trident declares {@code ItemUseAnimation.TRIDENT},
 * so the test never once passed and no trident has ever been posed by this class.
 * The name collision is old: pre-1.21.11 the trident's use animation was called
 * {@code SPEAR} and its arm pose {@code THROW_SPEAR}, and the rename to
 * {@code TRIDENT}/{@code THROW_TRIDENT} — with {@code SPEAR} handed to a different
 * item family — was never picked up here.
 *
 * <p>Matching the arm pose avoids the whole class of mistake. It is the exact value
 * {@code HumanoidModel} switches on to decide what to write, so "did vanilla pose
 * this arm as a wind-up" is asked in the same terms vanilla answers it, and anything
 * that reaches those poses is covered with nothing to maintain: the vanilla trident,
 * {@code bountiful-fish:elder_trident} (an {@code ElderTridentItem extends TridentItem}),
 * the copper-through-netherite spears, and any modded throwable that routes through
 * either. {@code AvatarRenderer#getArmPose} has five separate paths into
 * {@code ArmPose.SPEAR} alone; enumerating them here would be a standing liability.
 *
 * <h2>Why the pose is snapshotted rather than recomputed</h2>
 *
 * <p>{@link SpyglassPose} reproduces vanilla's numbers because they are absolute.
 * These are not — {@code THROW_TRIDENT} is <em>relative</em> to whatever the arm
 * already held:
 *
 * <pre>  arm.xRot = arm.xRot * 0.5F - (float)Math.PI;
 *  arm.yRot = 0.0F;</pre>
 *
 * <p>and {@code SPEAR} runs {@code SpearAnimations#thirdPersonHandUse}, which reads
 * the head, the swim amount and the item's {@code KINETIC_WEAPON} component to build
 * a multi-stage raise. By {@code RETURN} of {@code setupAnim} vanilla has already
 * applied whichever it chose, so the live part <em>is</em> the pose; re-running either
 * formula here would double it. Capturing the part is therefore both simpler and
 * exact, and it inherits any future change to vanilla's numbers for free.
 *
 * <p>Nothing else has written the arm by that point for the items this covers.
 * {@code bettercombat:trident} — which {@code fallback_compatibility.json} maps
 * {@code trident|javelin|impaled} onto, so the elder trident lands there too —
 * declares {@code "pose": ""}, so Better Combat applies no PAL pose to overwrite it,
 * and an attack animation cannot be playing while a throw is being charged.
 *
 * <h2>Scope: wind-ups only</h2>
 *
 * <p>{@code ArmPose.SPEAR} is not only a wind-up. Vanilla also hands it to any item
 * in {@code #minecraft:spears} that is merely being <em>held</em>, and to a
 * {@code STAB}-swinging item mid-swing. Claiming those frames would put vanilla's
 * carry stance permanently over the Better Combat hold for every spear, which is a
 * regression and not what was asked for. So {@code SPEAR} is additionally gated on
 * {@code ticksUsingItem} for that specific arm — the same value
 * {@code thirdPersonHandUse} itself gates the raise on, so this claims exactly the
 * frames in which vanilla wrote a raise and no others. {@code THROW_TRIDENT} needs no
 * such gate: {@code getArmPose} only selects it when the item is in use on that hand
 * with ticks remaining, so the enum value is already the condition.
 *
 * <p>Only the winding arm is stored; the other slot is left null, which EMF Compat's
 * applier skips, so the free arm keeps DA's idle swing instead of being frozen. Both
 * are stored if both are somehow winding up, which off-hand use makes reachable.
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
    public static boolean apply(AbstractClientPlayer player, PlayerModel model, AvatarRenderState state) {
        boolean right = isWindUp(state.rightArmPose, state, HumanoidArm.RIGHT);
        boolean left = isWindUp(state.leftArmPose, state, HumanoidArm.LEFT);

        if (!right && !left) {
            release(player.getUUID());
            return false;
        }

        PoseManager.savePoses(player.getUUID(), SOURCE,
                left ? new PoseSnapshot(model.leftArm) : null,
                right ? new PoseSnapshot(model.rightArm) : null);
        POSED.add(player.getUUID());
        return true;
    }

    /**
     * Whether vanilla wrote a throw wind-up onto this arm on this frame. See the
     * class doc for why {@code SPEAR} carries an extra condition and
     * {@code THROW_TRIDENT} does not.
     */
    private static boolean isWindUp(HumanoidModel.ArmPose pose, AvatarRenderState state, HumanoidArm arm) {
        if (pose == HumanoidModel.ArmPose.THROW_TRIDENT) {
            return true;
        }
        return pose == HumanoidModel.ArmPose.SPEAR && state.ticksUsingItem(arm) > 0.0F;
    }

    /** Drops the pose in the same frame the throw is released or cancelled. */
    public static void release(UUID id) {
        if (POSED.remove(id)) {
            PoseManager.clearPoses(id, SOURCE);
        }
    }
}
