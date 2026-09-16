package info.mudbourn.mmscompat.client.camera;

import io.github.leawind.perspectiveapi.api.PerspectiveAPI;
import net.gamev.cinematic_respawn.client.RespawnCinematicController;

/**
 * Glue between Leawind's Third Person and Cinematic Respawn.
 *
 * While CR is running a death cinematic, force first person — Third Person's
 * camera otherwise fights CR's for control of the shot.
 *
 * Embedded into mms-mod-compat-support from the former standalone camera-glue
 * mod (2026-09-16). It was its own client entrypoint there; here it is a plain
 * helper that {@link MmsModCompatSupportClient} calls only when all three of
 * leawind_third_person, perspective_api and cinematic_respawn are loaded, so
 * this compat mod never hard-depends on them and the classes below are only
 * linked when they are actually present.
 *
 * v2 (2026-08-04) — rewritten for Leawind 3.0.0.
 *
 * 3.0.0 deleted the surface v1 was built on (ThirdPerson.getConfig(),
 * ENTITY_AGENT, RotateTargetEnum, ThirdPersonEvents) and moved camera
 * arbitration into Perspective API, which Third Person now registers itself
 * with as a perspective. So instead of mutating another mod's config, we
 * register a single override on Perspective API's override chain.
 *
 * The override chain polls its suppliers continuously and skips any that
 * return null, so this is entirely stateless: return first person while the
 * cinematic runs, null otherwise. That removes every failure mode v1 had to
 * defend against by hand —
 *
 * - No enter/exit edge tracking, so a missed edge can't strand the camera.
 * - No saved previous state to restore, so nothing to leak if a tick is
 *   skipped or the player entity is swapped mid-sequence.
 * - No dependence on mc.player being non-null. The death -> respawn entity
 *   swap transiently nulls it, which is exactly when v1's restore had to fire;
 *   the supplier doesn't care.
 *
 * Whatever the player had selected before the cinematic is simply what the
 * chain falls back to once we stop overriding.
 */
public final class CameraGlue {

    private CameraGlue() {}

    /** Perspective API's built-in first-person perspective. */
    private static final String FIRST_PERSON = "perspective_api.first_person";

    /**
     * Override priority. The chain sorts descending and takes the first
     * non-null, so this only needs to outrank ordinary perspective switching.
     * Deliberately not Integer.MAX_VALUE — leave headroom for anything that
     * legitimately needs to outrank a death cinematic.
     */
    private static final int PRIORITY = 1000;

    /**
     * Register the override. Call only when leawind_third_person, perspective_api
     * and cinematic_respawn are all loaded — the guard lives at the call site so
     * this class (and the third-party types it imports) is never linked without
     * them.
     */
    public static void register() {
        // Defer until Perspective API's runtime is installed; the override
        // chain is not available at mod-init time.
        PerspectiveAPI.runWhenReady("camera_glue", () ->
            PerspectiveAPI.getOverrideChain().register(PRIORITY, CameraGlue::override)
        );
    }

    /**
     * @return the first-person perspective id while a death cinematic is in
     *         progress, or null to defer to whatever else the chain decides.
     */
    private static String override() {
        // Cover the WHOLE sequence including the respawn wake-up pan, not just
        // the death shot — isActive() alone drops the override too early and
        // Third Person snaps back mid-transition.
        boolean cinematic = RespawnCinematicController.isActive()
                         || RespawnCinematicController.isCameraTransitionActive();

        return cinematic ? FIRST_PERSON : null;
    }
}
