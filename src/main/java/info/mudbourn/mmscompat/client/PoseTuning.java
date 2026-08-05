package info.mudbourn.mmscompat.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Held-pose tuning, written to {@code config/mms_compat_pose.json} on first run.
 *
 * <p>This exists to settle one question that three releases failed to answer by
 * guessing: how a Better Combat held pose should combine with DetailedAnimations'
 * arm swing. Both implementations ship, and {@link #additiveArms} picks between
 * them at runtime — {@code /mmspose arms <absolute|additive>} flips it mid-session
 * so the two can be compared back to back without a rebuild.
 *
 * <pre>
 * {
 *   "additive_arms": false
 * }
 * </pre>
 *
 * <h2>The two modes</h2>
 *
 * <ul>
 *   <li><b>absolute</b> (default, what 0.9.55 onwards shipped) —
 *       {@code HeldPoseMixin} stores real arm snapshots. {@code PoseSnapshot}
 *       writes absolute rotations, so the hold wins the arm outright and DA's
 *       swing is discarded, including DA's gate on being airborne.</li>
 *   <li><b>additive</b> (0.9.52, withdrawn in 0.9.55) — the mixin subtracts
 *       vanilla's walk swing, stashes the remainder in {@link HeldPoseDelta}, and
 *       stores an armless marker so the unpause gate stays open;
 *       {@code HeldPoseAdditiveMixin} adds the remainder back after EMF animates.
 *       DA keeps deciding when the arms swing, the hold only decides how the
 *       weapon sits.</li>
 * </ul>
 *
 * <p>The default is deliberately the shipped behaviour. Additive was measured to
 * make things worse in 0.9.51-0.9.53 — the diagnosis was sound but the apply side
 * was never understood, so it stays opt-in until an actual A/B says otherwise.
 */
public final class PoseTuning {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(), "mms_compat_pose.json");

    /** Add the hold on top of DA's arms instead of replacing them. */
    public static boolean additiveArms = false;

    /**
     * How long, in milliseconds, the arms take to ease across a change of pose
     * authority — into a held pose, out of a swing or a roll, or from one source
     * straight to another. Zero disables the blend and restores the hard cut. See
     * {@code PoseBlend}.
     *
     * <p>Defaults to off. 150 shipped first and read as too fast; 260 was tried and
     * was worse still, because the problem was never the duration. A hard cut looks
     * <em>more</em> correct than any ease tested so far, which is not what a blend
     * between two valid poses can do — an interpolation can look slow or abrupt, but
     * it cannot look wrong unless one of its two endpoints is wrong. Until that is
     * understood the blend stays off, and {@code /mmspose ease <ms>} turns it on for
     * whoever is investigating it.
     */
    public static int transitionEaseMs = 0;

    private PoseTuning() {
    }

    private static final class Data {
        boolean additive_arms = false;
        int transition_ease_ms = 0;
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(FILE)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                additiveArms = data.additive_arms;
                transitionEaseMs = data.transition_ease_ms;
            }
        } catch (Exception e) {
            LOG.error("[mms_compat] failed to read {}; keeping defaults", FILE.getName(), e);
        }
        LOG.info("[mms_compat] held-pose arm mode: {}, handover ease {}ms",
                additiveArms ? "additive" : "absolute", transitionEaseMs);
    }

    public static void save() {
        Data data = new Data();
        data.additive_arms = additiveArms;
        data.transition_ease_ms = transitionEaseMs;
        try {
            File parent = FILE.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            try (Writer writer = new FileWriter(FILE)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            LOG.error("[mms_compat] failed to write {}", FILE.getName(), e);
        }
    }
}
