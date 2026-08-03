package info.mudbourn.mmscompat.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Clears Leawind's stuck {@code isPerspectiveInverted} flag while the camera is
 * genuinely in first person.
 *
 * <h2>The bug</h2>
 *
 * <p>Leawind's {@code CameraTypeMixin} overwrites {@code CameraType#isFirstPerson}
 * for every caller in the game:
 *
 * <pre>  return this.firstPerson ^ ThirdPersonStatus.isPerspectiveInverted;</pre>
 *
 * <p>and {@code ThirdPersonEvents} only ever updates that flag inside a branch
 * guarded by {@code options.getCameraType() != CameraType.FIRST_PERSON}:
 *
 * <pre>
 * if (options.getCameraType() != CameraType.FIRST_PERSON) {
 *     Entity e = ENTITY_AGENT.getRawCameraEntity();
 *     isPerspectiveInverted = !e.isSpectator() &amp;&amp; e.isVisuallySwimming();
 *     if (e instanceof LivingEntity le &amp;&amp; le.isUsingItem())
 *         isPerspectiveInverted |= &lt;use_to_first_person predicates&gt;;
 * }
 * </pre>
 *
 * <p>So the flag is written only while in third person and is never cleared on the
 * way into first person. Swim in third person — which sets it — then press F5, and
 * it stays {@code true} with nothing left to ever reset it. {@code FIRST_PERSON}
 * then reports {@code true ^ true == false}.
 *
 * <p>The visible symptom is the spyglass. {@code Player#getFieldOfViewModifier}
 * returns its hard {@code 0.1F} scope multiplier only under
 * {@code isFirstPerson && isScoping()}, and its {@code isFirstPerson} argument is
 * exactly this poisoned call. The scope multiplier is skipped, leaving only the
 * ordinary movement-speed FOV drift, and the spyglass appears to barely zoom. Any
 * other consumer of {@code isFirstPerson} is wrong for the same duration.
 *
 * <h2>Why a tick handler and not a mixin</h2>
 *
 * <p>The obvious seam — {@code CameraType#isFirstPerson} — is the one Leawind
 * already occupies, and winning it would be a priority fight whose outcome flips
 * with load order. Resetting the field instead is uncontested: Leawind writes it
 * only in the third-person branch, so in first person there is no second writer to
 * race, and once out of first person the next tick of Leawind's own handler
 * recomputes it from scratch. Ordering between the two handlers is therefore
 * irrelevant in both directions.
 *
 * <p>Per-tick is frequent enough despite the flag being read per frame: nothing can
 * set it while first person holds, so one clear on entry is sufficient and the
 * repeats are idempotent.
 *
 * <p>Reached reflectively, as {@link CemLayerPoseRelay} reaches EMF — the compat mod
 * takes no compile-time dependency on Leawind, and an upstream fix or rename
 * degrades to doing nothing rather than crashing the client tick.
 */
public final class PerspectiveFlagReset {

    private static final String STATUS = "com.github.leawind.thirdperson.ThirdPersonStatus";

    private static final String FIELD = "isPerspectiveInverted";

    private static volatile boolean unavailable;

    private static MethodHandle setter;

    private PerspectiveFlagReset() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (unavailable || minecraft.options == null) {
                return;
            }
            if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
                // Third person: Leawind's own handler owns the flag this tick.
                return;
            }
            clear();
        });
    }

    private static void clear() {
        if (!resolve()) {
            return;
        }
        try {
            setter.invokeExact(false);
        } catch (Throwable t) {
            unavailable = true;
        }
    }

    private static boolean resolve() {
        if (setter != null) {
            return true;
        }
        synchronized (PerspectiveFlagReset.class) {
            if (setter != null) {
                return true;
            }
            try {
                Class<?> status = Class.forName(STATUS, false,
                        PerspectiveFlagReset.class.getClassLoader());
                setter = MethodHandles.publicLookup()
                        .findStaticSetter(status, FIELD, boolean.class);
                return true;
            } catch (Throwable t) {
                unavailable = true;
                return false;
            }
        }
    }
}
