package info.mudbourn.mmscompat.client;

import java.util.Set;

/**
 * How many arms a Better Combat held pose is allowed to take from
 * DetailedAnimations, keyed on the pose id the weapon declares.
 *
 * <h2>Why this is a list and not a test</h2>
 *
 * <p>The tempting implementation reads the pose animation and asks whether it
 * keyframes {@code leftArm}. Every one of Better Combat's held poses does —
 * {@code heavy}, {@code sword}, {@code polearm}, {@code katana} and {@code scythe}
 * all move both arms — so that test answers "both" for everything and there is no
 * structural signal to read. Which weapons look right carried one-handed is a
 * judgement about the weapon, so it is written down as one.
 *
 * <p>The pose id is the right key rather than the item id because Better Combat
 * resolves {@code parent} chains before {@code WeaponAttributes#pose} is readable,
 * so every material variant of a weapon — and every mod that inherits a stock
 * profile — collapses onto one string. The two entries below cover the anchor and
 * large tuna ({@code bettercombat:anchor} / {@code large_tuna}), Expanded
 * Weaponry's hammers ({@code bettercombat:hammer}) and greatswords
 * ({@code bettercombat:claymore}), and anything else that inherits from them.
 *
 * <p>Everything not listed keeps both arms, which is the behaviour that shipped.
 * That is deliberate: an unrecognised pose is far more likely to be a genuine
 * two-handed hold than a one-handed carry, and a wrong guess in that direction only
 * costs an arm that DA would have swung. Basic Weapons' spears, pikes and glaives
 * all resolve to {@code pose_two_handed_polearm}, and katanas have a pose of their
 * own.
 *
 * <p>Because this is a judgement rather than a derivation, it tracks the mods: a
 * weapon can change hands across an update without its pose id changing, so an
 * entry moving between the list and the default is expected maintenance and not a
 * sign the mechanism is wrong.
 *
 * <h2>Which arm survives</h2>
 *
 * <p>The right one, literally, not the main-hand one. Better Combat's poses are
 * authored against {@code rightArm} in the animation file regardless of the
 * player's handedness setting, so that is where the hold actually lands. Two-handed
 * weapons also lose their attributes entirely in the off hand, so there is no case
 * where the weapon is held in the left.
 */
public final class HeldPoseArms {

    /**
     * Poses whose weapon is carried in one hand, leaving the left arm to DA.
     */
    private static final Set<String> ONE_ARMED = Set.of(
            // anchor, Expanded Weaponry hammers, heavy axes
            "bettercombat:pose_two_handed_heavy",
            // large tuna, Expanded Weaponry greatswords, claymores
            "bettercombat:pose_two_handed_sword");

    /**
     * Poses that should not be preserved at all, so DetailedAnimations keeps both
     * arms and the weapon is carried exactly as if it declared no pose.
     *
     * <p>Only the scythe. It went through both other settings first — both arms, then
     * right arm only — and the right-arm carry was correct for idling, running and
     * jump-running before this. This is not a bug fix on top of that; it is a
     * deliberate look, so the scythe idles like a one-handed weapon rather than
     * being carried.
     *
     * <p>This affects the idle hold and nothing else. Swings are stored by the Better
     * Combat addon under its own {@code better_combat} key, which this mod never
     * writes or clears, so the scythe's attack animations are untouched.
     */
    private static final Set<String> UNPOSED = Set.of(
            "bettercombat:pose_two_handed_scythe");

    private HeldPoseArms() {
    }

    /**
     * @param pose the resolved {@code WeaponAttributes#pose}, or null/blank if the
     *             weapon declares none
     * @return true when the hold should own the left arm as well as the right
     */
    public static boolean usesBothArms(String pose) {
        return pose == null || !ONE_ARMED.contains(pose);
    }

    /**
     * Whether this pose should be discarded rather than preserved, leaving both arms
     * to DetailedAnimations.
     *
     * @param pose the resolved {@code WeaponAttributes#pose}, or null
     */
    public static boolean unposed(String pose) {
        return pose != null && UNPOSED.contains(pose);
    }
}
