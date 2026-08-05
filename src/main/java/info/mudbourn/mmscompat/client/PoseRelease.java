package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Eases the arms back into DetailedAnimations when a pose source lets go of them,
 * instead of cutting on a single frame.
 *
 * <h2>The problem</h2>
 *
 * <p>Every arm authority in this stack is binary. Better Combat's addon stores arms
 * for exactly the duration of a swing and calls {@code clearPoses} the instant it
 * ends; Combat Roll's animation layer stops reporting active the frame the roll
 * finishes; {@code HeldPoseMixin} releases on the frame the weapon leaves the hand.
 * In every case the arm is at the end of a posed animation on one frame and at
 * whatever DA's cycle happens to be on the next, with nothing in between. The eye
 * reads that discontinuity as the animation "ending" rather than finishing, which is
 * why a roll's end is visible even when the roll itself looks right.
 *
 * <p>Nothing upstream can fix it, because no single mod owns both sides of the
 * handover: the source that is releasing has already decided it is done, and DA
 * never knew a pose was there to blend from.
 *
 * <h2>How the blend is anchored</h2>
 *
 * <p>The obvious implementation samples the arms on the last owned frame and eases
 * from that. It has a subtle failure: whether the sampled value is the posed one or
 * DA's depends on whether this code runs before or after EMF Compat's restore, at a
 * shared injection point whose ordering this package documents two contradictory
 * rules for. Anchoring to the wrong side would ease from DA to DA — a no-op that
 * looks exactly like the bug.
 *
 * <p>So the anchor is taken from the <em>store</em> rather than from the model.
 * {@code PoseSnapshot} carries the rotations the restore is going to write, so
 * reading them is correct whether or not the restore has happened yet. Only a
 * PAL-driven source with no {@code PoseManager} entry — a Combat Roll roll — falls
 * back to sampling the model, and during those the CEM animation is paused, so both
 * sides of the restore read the same numbers anyway.
 *
 * <h2>The curve</h2>
 *
 * <p>Weight runs 1 to 0 over {@link PoseTuning#releaseEaseMs} and is shaped by
 * smoothstep, so the handover leaves the pose and arrives at DA with zero velocity
 * at both ends. A linear ramp removes the jump but leaves a corner, which on a fast
 * release reads as a second, smaller snap.
 *
 * <p>Time is measured in wall clock rather than ticks. The blend is a render-side
 * cosmetic and should last the same wall time whether the client is at 30 or 240
 * frames a second, and tick counting would quantise a 150 ms ease to three steps.
 */
public final class PoseRelease {

    /** One arm's rotation, as either a store snapshot or a model sample. */
    private record Arm(float xRot, float yRot, float zRot) {
    }

    private static final class State {
        Arm left;
        Arm right;
        long releasedAtNanos;
        boolean owned;
    }

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private PoseRelease() {
    }

    /**
     * Records that a source currently owns the arms, anchoring the blend that will
     * run when it lets go.
     */
    public static void owned(UUID uuid, float leftX, float leftY, float leftZ,
                             float rightX, float rightY, float rightZ) {
        State state = STATES.computeIfAbsent(uuid, k -> new State());
        state.left = new Arm(leftX, leftY, leftZ);
        state.right = new Arm(rightX, rightY, rightZ);
        state.owned = true;
        state.releasedAtNanos = 0L;
    }

    /**
     * Records that no source owns the arms this frame, and blends the tail of the
     * last pose into whatever DA has produced.
     *
     * <p>Called with the live model parts, which at this point hold DA's animation.
     * They are rewritten in place toward the stored anchor by the current weight.
     */
    public static void releasing(UUID uuid, ModelPart leftArm, ModelPart rightArm) {
        State state = STATES.get(uuid);
        if (state == null || state.left == null) {
            return;
        }

        long now = System.nanoTime();
        if (state.owned) {
            // First unowned frame: start the clock. The anchor is already stored.
            state.owned = false;
            state.releasedAtNanos = now;
        }

        long easeNanos = Math.max(0L, PoseTuning.releaseEaseMs) * 1_000_000L;
        if (easeNanos == 0L) {
            forget(uuid);
            return;
        }

        float linear = 1.0F - (float) (now - state.releasedAtNanos) / easeNanos;
        if (linear <= 0.0F) {
            // Blend finished. Dropping the anchor matters: a stale one would be
            // re-blended the next time this player is rendered after a gap.
            forget(uuid);
            return;
        }

        float weight = smoothstep(Math.min(1.0F, linear));
        apply(leftArm, state.left, weight);
        apply(rightArm, state.right, weight);
    }

    /**
     * Drops any anchor for this player. Used when a source releases the arms for a
     * reason that should not ease — the player going out of view, or the weapon
     * being swapped for one with a different pose entirely.
     */
    public static void forget(UUID uuid) {
        STATES.remove(uuid);
    }

    private static void apply(ModelPart part, Arm anchor, float weight) {
        part.xRot = lerp(part.xRot, anchor.xRot(), weight);
        part.yRot = lerp(part.yRot, anchor.yRot(), weight);
        part.zRot = lerp(part.zRot, anchor.zRot(), weight);
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    /** Zero derivative at both ends, so neither leaving nor arriving is a corner. */
    private static float smoothstep(float t) {
        return t * t * (3.0F - 2.0F * t);
    }
}
