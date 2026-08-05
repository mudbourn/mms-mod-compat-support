package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.geom.ModelPart;
import strm.emfcompat.core.PoseManager;
import strm.emfcompat.core.PoseSnapshot;
import strm.emfcompat.core.SavedPoses;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Eases the arms across a change of pose authority instead of cutting on one frame.
 *
 * <h2>The problem</h2>
 *
 * <p>Every arm authority in this stack is binary and none of them knows about the
 * others. Better Combat's addon stores arms for exactly the duration of a swing and
 * calls {@code clearPoses} the instant it ends. Combat Roll's animation layer stops
 * reporting active the frame the roll finishes. {@code HeldPoseMixin} runs a ladder
 * of mutually exclusive branches — crossbow, spyglass, trident, inspect, in-use, idle
 * hold, nothing — and hands the arms to exactly one of them per frame, releasing the
 * rest. {@code PoseManager} then merges whatever is stored and stamps absolute
 * rotations over EMF's animation.
 *
 * <p>So a handover is a hard cut in every direction. The arm is at the end of one
 * animation on one frame and at an unrelated absolute rotation on the next, with
 * nothing in between: raise a spyglass mid-walk and the arm teleports up; finish a
 * swing and it teleports back into DA's cycle; put the spyglass away with a posed
 * weapon in hand and it teleports twice, once out of the spyglass and once into the
 * hold. The eye reads each of those as a glitch rather than as a movement.
 *
 * <p>Nothing upstream can fix it, because no single mod owns both sides of a
 * handover. The releasing source has already decided it is done, and the acquiring
 * one never knew there was a pose to come from.
 *
 * <h2>What this does</h2>
 *
 * <p>Each frame it works out <em>who</em> owns the arms and <em>what</em> they are
 * being set to. When the owner changes, it starts a transition anchored to the arm
 * rotations that were actually displayed on the previous frame, and for
 * {@link PoseTuning#transitionEaseMs} it writes a blend of the new target and that
 * anchor. The identity of the two sides does not matter — pose to pose, pose to
 * DetailedAnimations, and DA to pose all go through the same path — so a source
 * added later inherits the easing without knowing this class exists.
 *
 * <p>Only the two arms are handled. They are the contested slot: they are what every
 * producer here writes, what {@code PoseManager} stores, and where DA and the held
 * poses actually collide. Nothing else in the stack has two authorities fighting for
 * it frame to frame.
 *
 * <h2>Why the anchor is the previous displayed frame</h2>
 *
 * <p>Not the previous source's snapshot: by the time the owner changes, that source
 * may have been mid-animation, and its stored value is where the animation was, not
 * where the arm was drawn — the two differ whenever a previous transition was still
 * running. Anchoring to what was drawn makes the arm continuous by construction, and
 * makes an interrupted transition chain into the next one instead of restarting from
 * a stale pose.
 *
 * <h2>Why both the model and the store are written</h2>
 *
 * <p>This runs at {@code EMFModelPartRoot#animate} RETURN, the same injection point
 * EMF Compat restores its poses at, and the relative order of the two is a priority
 * question this package deliberately does not depend on. Writing only the model would
 * lose every blend that eases <em>into</em> a stored pose if the restore runs after
 * us; writing only the store would lose every blend out to DA, which the store knows
 * nothing about. Writing both is correct either way round: whichever runs last, the
 * value it writes is the blended one.
 *
 * <p>The write-back targets the owning source's own entry, so it replaces that
 * source's value for this frame rather than adding a competing one — the producer
 * rewrites it from scratch next frame, during {@code setupAnim}, well before this
 * runs again.
 *
 * <h2>The curve</h2>
 *
 * <p>Weight runs 1 to 0 shaped by smoothstep, so the arm leaves the old pose and
 * arrives at the new one with zero velocity at both ends. A linear ramp removes the
 * jump but leaves a corner, which on a fast handover reads as a second, smaller snap.
 *
 * <p>Time is wall clock, not ticks: this is a render-side cosmetic and should last
 * the same wall time at 30 frames a second as at 240, where tick counting would
 * quantise a 150 ms ease into three steps.
 */
public final class PoseBlend {

    /** One arm's rotation, from either a store snapshot or a model sample. */
    private record Arm(float xRot, float yRot, float zRot) {

        static Arm of(ModelPart part) {
            return new Arm(part.xRot, part.yRot, part.zRot);
        }

        static Arm of(PoseSnapshot snapshot) {
            return new Arm(snapshot.xRot, snapshot.yRot, snapshot.zRot);
        }

        Arm lerp(Arm to, float t) {
            return new Arm(
                    this.xRot + (to.xRot - this.xRot) * t,
                    this.yRot + (to.yRot - this.yRot) * t,
                    this.zRot + (to.zRot - this.zRot) * t);
        }
    }

    private static final class State {
        /** The owner token seen on the previous frame. */
        String owner = "";
        /** What was actually drawn on the previous frame, and so what to ease from. */
        Arm left;
        Arm right;
        /** Anchor for the transition in progress, or null when none is running. */
        Arm fromLeft;
        Arm fromRight;
        long startedAtNanos;
    }

    /** Owner token for "nobody stored anything" — i.e. DetailedAnimations has them. */
    public static final String DETAILED_ANIMATIONS = "";

    /**
     * Owner token for Combat Roll. Its animation goes through Player Animation Lib
     * and never touches {@code PoseManager}, so it has no source key of its own and
     * would otherwise be indistinguishable from DA — which would mean no ease at the
     * end of a roll, the case this easing was first asked for.
     */
    public static final String COMBAT_ROLL = "$roll";

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    /**
     * A throwaway part used only to turn blended rotations back into a
     * {@link PoseSnapshot}, whose only constructor reads a {@code ModelPart}. Seeded
     * from the stored snapshot first so translation, scale and visibility survive the
     * round trip untouched; only the three rotations are ours to change.
     *
     * <p>Render is single-threaded, so one instance is enough.
     */
    private static final ModelPart SCRATCH = new ModelPart(List.of(), Map.of());

    private PoseBlend() {
    }

    /**
     * Runs one frame of blending for a player, rewriting {@code leftArm} and
     * {@code rightArm} in place.
     *
     * @param uuid    the player being rendered
     * @param owner   a token identifying who owns the arms this frame; any stable
     *                string will do, and a change of value is what starts a
     *                transition. {@link #DETAILED_ANIMATIONS} for nobody.
     * @param stored  the merged store entry, or null when nothing is stored. Each arm
     *                it carries is the target for that arm, because it is what the
     *                restore is going to write regardless of injection order; an arm
     *                it leaves null is one no source claimed, and is sampled off the
     *                model, where DetailedAnimations has already written it.
     * @param bySource the store's per-source entries, for the write-back, or null to
     *                write the model only
     */
    public static void frame(UUID uuid, String owner, SavedPoses stored,
                             Map<String, SavedPoses> bySource,
                             ModelPart leftArm, ModelPart rightArm) {
        State state = STATES.computeIfAbsent(uuid, k -> new State());

        PoseSnapshot storedLeft = stored == null ? null : stored.leftArm();
        PoseSnapshot storedRight = stored == null ? null : stored.rightArm();
        Arm targetLeft = storedLeft != null ? Arm.of(storedLeft) : Arm.of(leftArm);
        Arm targetRight = storedRight != null ? Arm.of(storedRight) : Arm.of(rightArm);

        long now = System.nanoTime();
        long easeNanos = Math.max(0L, PoseTuning.transitionEaseMs) * 1_000_000L;

        if (!owner.equals(state.owner)) {
            state.owner = owner;
            // First frame under new management. Ease from what the eye last saw, not
            // from what the outgoing source had stored; see the class javadoc.
            if (state.left != null && easeNanos > 0L) {
                state.fromLeft = state.left;
                state.fromRight = state.right;
                state.startedAtNanos = now;
            } else {
                state.fromLeft = null;
            }
        }

        Arm left = targetLeft;
        Arm right = targetRight;

        if (state.fromLeft != null) {
            float linear = 1.0F - (float) (now - state.startedAtNanos) / easeNanos;
            if (linear <= 0.0F) {
                // Done. Dropping the anchor matters: a stale one would be re-blended
                // the next time this player is rendered after a gap.
                state.fromLeft = null;
            } else {
                float weight = smoothstep(Math.min(1.0F, linear));
                left = targetLeft.lerp(state.fromLeft, weight);
                right = targetRight.lerp(state.fromRight, weight);
            }
        }

        state.left = left;
        state.right = right;

        // Settled and unstored means the arms already hold the target — DA wrote it,
        // and there is no store entry that could overwrite it. Writing anything here
        // would be a no-op at best and churn at worst.
        if (state.fromLeft == null) {
            return;
        }

        write(leftArm, left);
        write(rightArm, right);

        // And the same values into every source that claimed an arm, so the restore
        // writes the blend whichever side of it this injection lands on. Every source
        // is given the same value, so the merge settles on it however it orders them.
        if (bySource == null || bySource.isEmpty()) {
            return;
        }
        for (Map.Entry<String, SavedPoses> entry : List.copyOf(bySource.entrySet())) {
            SavedPoses saved = entry.getValue();
            if (saved == null || (saved.leftArm() == null && saved.rightArm() == null)) {
                continue;
            }
            PoseManager.savePoses(uuid, entry.getKey(), new SavedPoses(
                    saved.leftArm() == null ? null : reseal(saved.leftArm(), left),
                    saved.rightArm() == null ? null : reseal(saved.rightArm(), right),
                    saved.parts()));
        }
    }

    /** Drops any anchor for this player, so the next frame starts clean. */
    public static void forget(UUID uuid) {
        STATES.remove(uuid);
    }

    /** A copy of {@code snapshot} with its rotations replaced by {@code arm}. */
    private static PoseSnapshot reseal(PoseSnapshot snapshot, Arm arm) {
        snapshot.apply(SCRATCH);
        write(SCRATCH, arm);
        return new PoseSnapshot(SCRATCH);
    }

    private static void write(ModelPart part, Arm arm) {
        part.xRot = arm.xRot();
        part.yRot = arm.yRot();
        part.zRot = arm.zRot();
    }

    /** Zero derivative at both ends, so neither leaving nor arriving is a corner. */
    private static float smoothstep(float t) {
        return t * t * (3.0F - 2.0F * t);
    }
}
