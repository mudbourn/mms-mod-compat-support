package info.mudbourn.mmscompat.client.lean;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Body-lean tuning, written to {@code config/mms_compat_lean.json} on first run.
 *
 * <p>This is a clean-room reimplementation of the lean effect that Custom Player
 * Animations provided. CPA is CC BY-NC-SA 4.0 and this mod is MIT, so no CPA code
 * was read or translated — the behaviour was specified from its published config
 * surface (twelve options: enable, invert, smoothing, forward/side intensity,
 * forward/side maxima, and look/pitch/yaw lean with their intensities) and
 * rebuilt independently. The knobs below cover the same ground but are NOT the
 * same numbers.
 *
 * <h2>Units are degrees here, and were not in CPA</h2>
 *
 * <p>CPA's values ({@code leanForwardIntensity 0.3}, {@code maxLeanSide 2.0})
 * are internal scalars applied at its own injection point, and they do not
 * transfer to a different one — a maximum of "2.0" is neither 2 degrees nor 2
 * radians in any way that survives being moved. Everything here is stated in
 * degrees of rotation instead, so the config says what it does. Expect to tune
 * {@link #max_side_degrees} by eye on first run; it is the one that reads
 * strongest in motion.
 *
 * <pre>
 * {
 *   "enabled": true,
 *   "smoothing": 0.2,
 *   "forward_degrees_per_speed": 25.0,
 *   "side_degrees_per_speed": 25.0,
 *   "max_forward_degrees": 8.0,
 *   "max_side_degrees": 10.0,
 *   "pitch_lean_enabled": true,
 *   "pitch_lean_ratio": 0.2,
 *   "max_pitch_lean_degrees": 6.0,
 *   "yaw_lean_enabled": false,
 *   "yaw_lean_ratio": 0.2,
 *   "max_yaw_lean_degrees": 6.0,
 *   "invert_forward": false,
 *   "invert_side": false,
 *   "pivot_height": 0.0
 * }
 * </pre>
 */
public final class LeanTuning {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(), "mms_compat_lean.json");

    public static boolean enabled = true;

    /**
     * How fast the lean chases its target, per second, as the rate of an
     * exponential approach. Lower is smoother and laggier; higher snaps.
     * Carried across from CPA's {@code leanSmoothingFactor} of 0.2, but applied
     * frame-rate independently here rather than as a raw per-frame lerp, so it
     * means the same thing at 30fps and 240fps.
     */
    public static double smoothing = 0.2;

    /**
     * Degrees of forward lean per block-per-tick of forward movement. A sprint
     * is roughly 0.28 b/t, so the default lands near 7 degrees before clamping.
     */
    public static double forward_degrees_per_speed = 25.0;

    /** As above, for strafing. */
    public static double side_degrees_per_speed = 25.0;

    public static double max_forward_degrees = 8.0;
    public static double max_side_degrees = 10.0;

    /** Looking up or down tilts the body with it. CPA's {@code enablePitchLean}. */
    public static boolean pitch_lean_enabled = true;

    /** Degrees of body tilt per degree of look pitch. */
    public static double pitch_lean_ratio = 0.2;

    public static double max_pitch_lean_degrees = 6.0;

    /**
     * Turning the head relative to the body rolls the body after it. Off by
     * default, matching the CPA config in the pack ({@code enableYawLean: false})
     * — with head-turn animations already in the stack it double-reads.
     */
    public static boolean yaw_lean_enabled = false;

    public static double yaw_lean_ratio = 0.2;
    public static double max_yaw_lean_degrees = 6.0;

    /**
     * Sign flips. The rotation axes at this injection point depend on the
     * handedness of the pose stack partway through {@code setupRotations}, which
     * is easier to confirm in game than to argue about — if the lean reads
     * backwards, flip the matching one of these rather than negating intensities.
     */
    public static boolean invert_forward = false;
    public static boolean invert_side = false;

    /**
     * Height of the lean pivot above the feet, in blocks. 0.0 pivots at the
     * ground, which reads as a whole-body lean. Raising it toward ~0.9 pivots
     * nearer the hips, which reads as leaning from the waist.
     */
    public static double pivot_height = 0.0;

    private LeanTuning() {
    }

    private static final class Data {
        boolean enabled = true;
        double smoothing = 0.2;
        double forward_degrees_per_speed = 25.0;
        double side_degrees_per_speed = 25.0;
        double max_forward_degrees = 8.0;
        double max_side_degrees = 10.0;
        boolean pitch_lean_enabled = true;
        double pitch_lean_ratio = 0.2;
        double max_pitch_lean_degrees = 6.0;
        boolean yaw_lean_enabled = false;
        double yaw_lean_ratio = 0.2;
        double max_yaw_lean_degrees = 6.0;
        boolean invert_forward = false;
        boolean invert_side = false;
        double pivot_height = 0.0;
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(FILE)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                enabled = data.enabled;
                if (data.smoothing > 0.0) {
                    smoothing = data.smoothing;
                }
                forward_degrees_per_speed = data.forward_degrees_per_speed;
                side_degrees_per_speed = data.side_degrees_per_speed;
                if (data.max_forward_degrees >= 0.0) {
                    max_forward_degrees = data.max_forward_degrees;
                }
                if (data.max_side_degrees >= 0.0) {
                    max_side_degrees = data.max_side_degrees;
                }
                pitch_lean_enabled = data.pitch_lean_enabled;
                pitch_lean_ratio = data.pitch_lean_ratio;
                if (data.max_pitch_lean_degrees >= 0.0) {
                    max_pitch_lean_degrees = data.max_pitch_lean_degrees;
                }
                yaw_lean_enabled = data.yaw_lean_enabled;
                yaw_lean_ratio = data.yaw_lean_ratio;
                if (data.max_yaw_lean_degrees >= 0.0) {
                    max_yaw_lean_degrees = data.max_yaw_lean_degrees;
                }
                invert_forward = data.invert_forward;
                invert_side = data.invert_side;
                pivot_height = data.pivot_height;
            }
        } catch (Exception e) {
            LOG.warn("[mms_compat] could not read {} — using defaults", FILE.getName(), e);
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            Data data = new Data();
            data.enabled = enabled;
            data.smoothing = smoothing;
            data.forward_degrees_per_speed = forward_degrees_per_speed;
            data.side_degrees_per_speed = side_degrees_per_speed;
            data.max_forward_degrees = max_forward_degrees;
            data.max_side_degrees = max_side_degrees;
            data.pitch_lean_enabled = pitch_lean_enabled;
            data.pitch_lean_ratio = pitch_lean_ratio;
            data.max_pitch_lean_degrees = max_pitch_lean_degrees;
            data.yaw_lean_enabled = yaw_lean_enabled;
            data.yaw_lean_ratio = yaw_lean_ratio;
            data.max_yaw_lean_degrees = max_yaw_lean_degrees;
            data.invert_forward = invert_forward;
            data.invert_side = invert_side;
            data.pivot_height = pivot_height;
            GSON.toJson(data, writer);
        } catch (Exception e) {
            LOG.warn("[mms_compat] could not write {}", FILE.getName(), e);
        }
    }
}
