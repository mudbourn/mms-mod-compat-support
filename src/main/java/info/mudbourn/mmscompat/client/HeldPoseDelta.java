package info.mudbourn.mmscompat.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The arm rotation a held weapon's pose contributes, stored as a <em>delta</em>
 * rather than an absolute rotation, and kept outside the mixin package for the
 * same reason as {@link HeldPoseSource} — Mixin claims every class under a
 * config's {@code package}.
 *
 * <h2>Why a delta and not a pose</h2>
 *
 * <p>The obvious implementation stores the posed arms and replays them, and that
 * is what this patch did through 0.9.50. It is wrong, and the symptom is arms that
 * keep running while the player is airborne.
 *
 * <p>The capture happens at {@code RETURN} of {@code PlayerModel#setupAnim}.
 * DetailedAnimations is CEM, so it runs strictly later, inside
 * {@code ModelPart#render}. The captured arms are therefore <em>vanilla's</em>:
 * the Better Combat hold, plus vanilla's walk swing, and none of DA. EMF Compat's
 * {@code EMFModelPartRootMixin} then calls {@code PoseSnapshot.applyRotation},
 * which writes absolute {@code xRot/yRot/zRot} and discards whatever DA animated.
 *
 * <p>So every frame, vanilla's walk swing was being stamped over DA's arms. DA
 * gates its swing on being airborne — which is why an empty hand eases out and
 * freezes correctly, since an empty hand stores nothing and DA survives. A posed
 * weapon replaced that with vanilla's swing, which does not gate.
 *
 * <h2>What is stored</h2>
 *
 * <p>The pose's contribution relative to vanilla's base arm, so it can be
 * <em>added</em> to whatever DA produced instead of replacing it:
 *
 * <ul>
 *   <li>{@code xRot} — vanilla's walk swing is subtracted out. Vanilla computes it
 *       as {@code cos(walkPos * 0.6662 (+PI for the right arm)) * 2 * walkSpeed * 0.5},
 *       and that term is the whole of the running-arms look. What remains is the
 *       hold.</li>
 *   <li>{@code yRot}, {@code zRot} — taken as-is. Vanilla's base contribution on
 *       these axes is only the idle bob (order 0.05 rad), so the absolute value is
 *       already the delta to within a rounding error, and subtracting the bob would
 *       cost more in double-counting risk than it buys.</li>
 * </ul>
 *
 * <p>The result is DA's correctly-gated swing with the weapon hold offset on top:
 * airborne, DA eases to rest and the hold stays put; on the ground the arms swing
 * under the hold as they should.
 */
public final class HeldPoseDelta {

    /** Both arms' pose contribution, in radians, relative to vanilla's base. */
    public record ArmDelta(float leftXRot, float leftYRot, float leftZRot,
                           float rightXRot, float rightYRot, float rightZRot) {
    }

    private static final Map<UUID, ArmDelta> DELTAS = new ConcurrentHashMap<>();

    private HeldPoseDelta() {
    }

    public static void put(UUID uuid, ArmDelta delta) {
        DELTAS.put(uuid, delta);
    }

    public static ArmDelta get(UUID uuid) {
        return DELTAS.get(uuid);
    }

    /**
     * Dropping the weapon has to release the arms in the same frame, or the last
     * delta is added onto DA's idle forever.
     */
    public static void clear(UUID uuid) {
        DELTAS.remove(uuid);
    }

    /**
     * Vanilla's walk-swing term for one arm, which is subtracted at capture and
     * deliberately not re-added — DA supplies its own, already gated on whether the
     * player is actually on the ground.
     *
     * @param rightArm the right arm's swing is a half-cycle out of phase
     */
    public static float vanillaArmSwing(float walkAnimationPos, float walkAnimationSpeed, boolean rightArm) {
        float phase = walkAnimationPos * 0.6662F + (rightArm ? (float) Math.PI : 0.0F);
        return (float) Math.cos(phase) * 2.0F * walkAnimationSpeed * 0.5F;
    }
}
