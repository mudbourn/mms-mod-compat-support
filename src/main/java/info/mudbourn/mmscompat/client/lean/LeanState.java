package info.mudbourn.mmscompat.client.lean;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

/**
 * Per-player smoothed lean angles, in degrees.
 *
 * <p>Kept out of the mixin package deliberately: a mixin config owns every class
 * under its package, and a plain helper living there is loaded as a mixin and
 * crashes at runtime.
 *
 * <h2>Why the state lives here and not on the render state</h2>
 *
 * <p>Lean is a smoothed quantity — it has to remember what it was last frame to
 * chase a target. Render states are rebuilt per frame from the entity, so there
 * is nowhere on them to keep it. Keying by player UUID also means a remote player
 * leaving and returning starts from rest rather than snapping from a stale angle.
 *
 * <p>Entries are dropped when a player has not been rendered for a while, so the
 * map cannot grow without bound on a busy server.
 */
public final class LeanState {

    /** Drop state for players not rendered in this long. */
    private static final long EXPIRY_NANOS = 10_000_000_000L; // 10s

    private static final Map<UUID, LeanState> STATES = new ConcurrentHashMap<>();

    private double forwardDegrees;
    private double sideDegrees;
    private long lastRenderNanos;

    private LeanState() {
    }

    public double forwardDegrees() {
        return forwardDegrees;
    }

    public double sideDegrees() {
        return sideDegrees;
    }

    public static LeanState get(UUID id) {
        return STATES.computeIfAbsent(id, unused -> new LeanState());
    }

    /**
     * Recomputes the target lean for this player and advances the smoothed value
     * toward it. Returns the same instance for chaining into the renderer.
     *
     * @param bodyRot body yaw in degrees, from the render state — not the entity,
     *                which is a tick behind and makes the lean stutter when turning
     */
    public LeanState update(AbstractClientPlayer player, float bodyRot) {
        long now = System.nanoTime();
        double deltaSeconds = lastRenderNanos == 0L
                ? 0.0
                : Math.min((now - lastRenderNanos) / 1_000_000_000.0, 0.25);
        lastRenderNanos = now;

        // Movement, as blocks per tick, from the position delta rather than
        // getDeltaMovement(): for remote players delta movement is only refreshed
        // when a movement packet lands, so it reads as a square wave and the lean
        // visibly steps. Position deltas are interpolated and stay smooth.
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;

        double yawRadians = Math.toRadians(bodyRot);
        double sin = Math.sin(yawRadians);
        double cos = Math.cos(yawRadians);

        // Project world movement onto the body's own forward/right axes, so a
        // player strafing while turning leans sideways the whole way round rather
        // than swapping axes as they pass an ordinal direction.
        double forwardSpeed = dx * -sin + dz * cos;
        double sideSpeed = dx * cos + dz * sin;

        double targetForward = clamp(
                forwardSpeed * LeanTuning.forward_degrees_per_speed,
                LeanTuning.max_forward_degrees);
        double targetSide = clamp(
                sideSpeed * LeanTuning.side_degrees_per_speed,
                LeanTuning.max_side_degrees);

        if (LeanTuning.pitch_lean_enabled) {
            targetForward += clamp(
                    player.getXRot() * LeanTuning.pitch_lean_ratio,
                    LeanTuning.max_pitch_lean_degrees);
        }

        if (LeanTuning.yaw_lean_enabled) {
            // Head yaw relative to the body, wrapped so that crossing 180 degrees
            // does not read as a full-speed turn in the opposite direction.
            double headOffset = Mth.wrapDegrees(player.getYHeadRot() - bodyRot);
            targetSide += clamp(
                    headOffset * LeanTuning.yaw_lean_ratio,
                    LeanTuning.max_yaw_lean_degrees);
        }

        // Frame-rate independent approach. A raw per-frame lerp would lean further
        // at high frame rates than low ones, which is what makes this kind of
        // effect feel different on different machines.
        double alpha = deltaSeconds <= 0.0
                ? 1.0
                : 1.0 - Math.exp(-LeanTuning.smoothing * 20.0 * deltaSeconds);

        forwardDegrees += (targetForward - forwardDegrees) * alpha;
        sideDegrees += (targetSide - sideDegrees) * alpha;

        return this;
    }

    private static double clamp(double value, double limit) {
        return Math.max(-limit, Math.min(limit, value));
    }

    /** Called once per frame from the renderer mixin. Cheap when nothing is stale. */
    public static void expire() {
        long cutoff = System.nanoTime() - EXPIRY_NANOS;
        STATES.entrySet().removeIf(entry -> entry.getValue().lastRenderNanos < cutoff);
    }
}
