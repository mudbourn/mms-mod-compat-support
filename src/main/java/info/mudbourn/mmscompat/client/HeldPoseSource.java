package info.mudbourn.mmscompat.client;

/**
 * The {@code PoseManager} source key the held-pose patch writes under, kept
 * outside the mixin package on purpose.
 *
 * <p>Two mixins need to agree on this string: the producer stores arms under it,
 * and the unpause gate asks whether anything is stored under it. They cannot share
 * it directly — Mixin claims every class under a config's {@code package} and
 * refuses to load one as an ordinary class, so a mixin reading a constant from a
 * sibling mixin blows up with {@code IllegalClassLoadError} the first time the
 * field is touched. A plain class in a normal package is the only safe home.
 *
 * <p>The value is deliberately distinct from the Better Combat addon's
 * {@code "better_combat"} key so the two producers never clear each other.
 */
public final class HeldPoseSource {

    public static final String SOURCE = "mms_held_pose";

    /**
     * Separate slot for Better Combat swing legs. Kept apart from {@link #SOURCE}
     * because the two have different lifetimes — a held pose lasts as long as the
     * weapon is in hand, swing legs only for the swing and only while the player
     * is standing still — and PoseManager clears a whole source at a time, so
     * sharing one key would have each clearing the other mid-frame.
     */
    public static final String LEG_SOURCE = "mms_bc_legs";

    private HeldPoseSource() {
    }
}
