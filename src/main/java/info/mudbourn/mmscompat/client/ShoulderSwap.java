package info.mudbourn.mmscompat.client;

import java.lang.reflect.Method;

// Reads and sets which shoulder Leawind's third person camera sits on, through its scheduler runtime.
final class ShoulderSwap {

    private static final String PACKAGE = "io.github.leawind.thirdperson.internal.logic.";

    private static final Method GET_INSTANCE;
    private static final Method SESSION;
    private static final Method MODE;
    private static final Method CAMERA_SETTINGS;
    private static final Method PROFILE;
    private static final Method UPDATE_PROFILE;
    private static final Method CENTERED;
    private static final Method OFFSET_X;
    private static final Method WITH_OFFSET_X;
    private static final Object MODE_AIMING;
    private static final Object SLOT_NORMAL;
    private static final Object SLOT_AIMING;

    static {
        Method getInstance = null;
        Method session = null;
        Method mode = null;
        Method cameraSettings = null;
        Method profile = null;
        Method updateProfile = null;
        Method centered = null;
        Method offsetX = null;
        Method withOffsetX = null;
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
            updateProfile = runtime.getMethod("updateCameraProfile", slot, cameraProfile);
            centered = cameraProfile.getMethod("centered");
            offsetX = cameraProfile.getMethod("offsetX");
            withOffsetX = cameraProfile.getMethod("withOffsetX", double.class);
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
        UPDATE_PROFILE = updateProfile;
        CENTERED = centered;
        OFFSET_X = offsetX;
        WITH_OFFSET_X = withOffsetX;
        MODE_AIMING = modeAiming;
        SLOT_NORMAL = slotNormal;
        SLOT_AIMING = slotAiming;
    }

    private ShoulderSwap() {
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

    // Puts the active profile's camera on the shoulder with this offset sign.
    static void setSide(double side) {
        Object profile = profile();
        if (profile == null) return;
        try {
            double offset = Math.abs((double) OFFSET_X.invoke(profile)) * side;
            Object runtime = GET_INSTANCE.invoke(null);
            UPDATE_PROFILE.invoke(runtime, slot(runtime), WITH_OFFSET_X.invoke(profile, offset));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
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
