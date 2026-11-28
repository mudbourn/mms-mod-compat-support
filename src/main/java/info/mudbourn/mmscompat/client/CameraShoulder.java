package info.mudbourn.mmscompat.client;

import java.lang.reflect.Method;

// Reads which shoulder Leawind's third person camera sits on, through its scheduler runtime.
final class CameraShoulder {

    private static final String PACKAGE = "io.github.leawind.thirdperson.internal.logic.";

    private static final Method GET_INSTANCE;
    private static final Method SESSION;
    private static final Method MODE;
    private static final Method CAMERA_SETTINGS;
    private static final Method PROFILE;
    private static final Method CENTERED;
    private static final Method OFFSET_X;
    private static final Object MODE_AIMING;
    private static final Object SLOT_NORMAL;
    private static final Object SLOT_AIMING;

    static {
        Method getInstance = null;
        Method session = null;
        Method mode = null;
        Method cameraSettings = null;
        Method profile = null;
        Method centered = null;
        Method offsetX = null;
        Object modeAiming = null;
        Object slotNormal = null;
        Object slotAiming = null;
        try {
            Class<?> runtime = Class.forName(PACKAGE + "scheduler.SchedulerRuntime");
            Class<?> settings = Class.forName(PACKAGE + "scheduler.camera.CameraSettings");
            Class<?> slot = Class.forName(PACKAGE + "scheduler.camera.CameraProfileSlot");
            Class<?> cameraProfile = Class.forName(PACKAGE + "base.camera.CameraProfile");
            getInstance = runtime.getMethod("getInstance");
            session = runtime.getMethod("session");
            mode = Class.forName(PACKAGE + "scheduler.SchedulerSession").getMethod("mode");
            cameraSettings = runtime.getMethod("cameraSettings");
            profile = settings.getMethod("profile", slot);
            centered = cameraProfile.getMethod("centered");
            offsetX = cameraProfile.getMethod("offsetX");
            modeAiming = Class.forName(PACKAGE + "scheduler.aiming.CameraMode").getField("AIMING").get(null);
            slotNormal = slot.getField("NORMAL").get(null);
            slotAiming = slot.getField("AIMING").get(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            getInstance = null;
        }
        GET_INSTANCE = getInstance;
        SESSION = session;
        MODE = mode;
        CAMERA_SETTINGS = cameraSettings;
        PROFILE = profile;
        CENTERED = centered;
        OFFSET_X = offsetX;
        MODE_AIMING = modeAiming;
        SLOT_NORMAL = slotNormal;
        SLOT_AIMING = slotAiming;
    }

    private CameraShoulder() {
    }

    // The sign of the active profile's sideways offset: 1 or -1, or 0 while centered or unreadable.
    static double side() {
        Object profile = profile();
        if (profile == null) return 0.0;
        try {
            if ((boolean) CENTERED.invoke(profile)) return 0.0;
            return Math.signum((double) OFFSET_X.invoke(profile));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 0.0;
        }
    }


    private static Object profile() {
        if (GET_INSTANCE == null) return null;
        try {
            Object runtime = GET_INSTANCE.invoke(null);
            return PROFILE.invoke(CAMERA_SETTINGS.invoke(runtime), slot(runtime));
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Object slot(Object runtime) throws ReflectiveOperationException {
        return MODE.invoke(SESSION.invoke(runtime)) == MODE_AIMING ? SLOT_AIMING : SLOT_NORMAL;
    }
}
