package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.geom.ModelPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A per-frame trace of who actually writes the player's arms, and in what order.
 *
 * <h2>Why this exists</h2>
 *
 * <p>Four rounds of held-pose work have now been reasoned out from the source of
 * Better Combat, DetailedAnimations and EMF Compat, shipped, and produced no visible
 * change. Each diagnosis was defensible and each was unverifiable in game, because
 * the only observable is the final pixel: an arm that looks wrong looks identical
 * whether the pose was never stored, stored and overwritten, or stored and restored
 * onto a model nobody animated.
 *
 * <p>Those three cases have completely different fixes, so the next step is to stop
 * inferring which one is happening and read it off. This samples one number —
 * {@code left_arm.xRot}, plus its siblings — at each point in the chain where
 * authorship can change hands, tags each sample, and prints them in the order they
 * actually occurred.
 *
 * <h2>Why the order is part of the output and not an assumption</h2>
 *
 * <p>Two mixins in this package document opposite rules for how priority orders
 * injections at a shared point: {@code HeldPoseLegMixin} says the higher number runs
 * second, {@code HeldPoseAdditiveMixin} says the mixin applied first runs last.
 * Both cannot be right, and every ordering argument made in this package rests on
 * one of them. So nothing here is sampled at an assumed position — the samples
 * self-report, and the sequence in the log is the ground truth. Whichever rule is
 * correct becomes readable rather than argued.
 *
 * <h2>What a line means</h2>
 *
 * <pre>
 * [pose] paused=false src(L)=null src(R)=mms_held_pose
 *   setupAnim        L=(-0.31, 0.00, 0.09) R=(-1.42, 0.21, -0.10)
 *   animate:early    L=(-0.31, 0.00, 0.09) R=(-1.42, 0.21, -0.10)
 *   animate:late     L=( 0.44, 0.00, 0.00) R=(-1.42, 0.21, -0.10)
 * </pre>
 *
 * <p>Read it as: if {@code setupAnim} and {@code animate:early} agree, the CEM
 * animation did not touch the arm — DetailedAnimations never ran, and the value is
 * whatever Better Combat's PAL pose or NEA left there. If they differ, DA ran and
 * the question moves to whether the restore between {@code early} and {@code late}
 * put the old value back. A left arm that is identical across all three while
 * {@code src(L)=null} is the specific case the one-armed weapon change was supposed
 * to fix and evidently does not: nothing restored it, so something upstream is
 * simply never being overwritten.
 *
 * <p>The same trace answers the NEA question without any new instrumentation. An
 * NEA held-item pose lands during {@code setupAnim} exactly like a Better Combat
 * one, so "does NEA's arm survive movement" is the same three-way comparison on a
 * frame where the player is walking.
 *
 * <h2>Cost</h2>
 *
 * <p>Off by default, local player only, and throttled to one report a second. When
 * off, every entry point is a static boolean read.
 */
public final class PoseDebug {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    /** Interval between reports. Per-frame output is unreadable and unloggable. */
    private static final long PERIOD_MS = 1000L;

    public static volatile boolean enabled;

    private static final List<String> SAMPLES = new ArrayList<>();
    private static long lastReport;
    private static boolean reportingThisFrame;

    private PoseDebug() {
    }

    /**
     * Opens a frame. Flushes whatever the previous frame collected, then decides
     * whether this frame is a reporting one.
     *
     * <p>Flushing at the <em>start</em> of the next frame rather than the end of
     * this one avoids having to know which sample point runs last — which is
     * exactly the thing under investigation.
     */
    public static void beginFrame(String header) {
        if (!enabled) {
            return;
        }
        flush();
        long now = System.currentTimeMillis();
        reportingThisFrame = now - lastReport >= PERIOD_MS;
        if (reportingThisFrame) {
            lastReport = now;
            SAMPLES.add(header);
        }
    }

    /** Records both arms under a tag, in the order the call actually happens. */
    public static void sample(String tag, ModelPart leftArm, ModelPart rightArm) {
        if (!enabled || !reportingThisFrame) {
            return;
        }
        SAMPLES.add(String.format("  %-16s L=(%+.3f, %+.3f, %+.3f) R=(%+.3f, %+.3f, %+.3f)",
                tag,
                leftArm.xRot, leftArm.yRot, leftArm.zRot,
                rightArm.xRot, rightArm.yRot, rightArm.zRot));
    }

    /** Whether this frame is being reported, for callers that must not do work otherwise. */
    public static boolean recording() {
        return enabled && reportingThisFrame;
    }

    public static UUID localPlayerFilter(UUID candidate, UUID local) {
        return candidate != null && candidate.equals(local) ? candidate : null;
    }

    private static void flush() {
        if (SAMPLES.isEmpty()) {
            return;
        }
        LOG.info("[mms_compat] {}", String.join("\n", SAMPLES));
        SAMPLES.clear();
        reportingThisFrame = false;
    }
}
