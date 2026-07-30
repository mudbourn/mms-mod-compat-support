package info.mudbourn.mmscompat.duck;

/**
 * Per-arrow flight tuning, decided once at spawn and read every tick.
 *
 * <p>Both knobs are multipliers on vanilla behaviour rather than absolute
 * values, so an arrow that no rule matches keeps exactly the flight it always
 * had.
 *
 * @see info.mudbourn.mmscompat.ranged.ArrowFlight
 */
public interface ArrowFlightDuck {

    /** Scales the arrow's gravity. Below 1 means less drop over distance. */
    float mmsCompat$gravityScale();

    /**
     * How far to close the gap between the arrow's per-tick speed retention
     * and a lossless 1.0. Zero is vanilla drag; 0.5 halves the speed lost each
     * tick, which keeps the arrow fast — and so hitting hard — much further
     * out, since vanilla scales impact damage by speed.
     */
    float mmsCompat$inertiaBonus();

    void mmsCompat$setFlight(float gravityScale, float inertiaBonus);
}
