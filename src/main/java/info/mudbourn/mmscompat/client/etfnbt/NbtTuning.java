package info.mudbourn.mmscompat.client.etfnbt;

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
 * Switches for the ETF NBT fast path, written to {@code config/mms_compat_nbt.json}.
 *
 * <pre>
 * {
 *   "fast_path": true,
 *   "verify": false
 * }
 * </pre>
 *
 * <p>{@link #verify} exists because the fast path's failure mode is silent: if
 * {@link NbtFastPath} synthesises the wrong shape, DetailedAnimations' rules simply
 * stop firing — bows do not draw, shields do not raise — with no error anywhere.
 * With verify on, both paths run and any disagreement is logged once per path, so
 * the game confirms the change rather than the diff. It costs the full serialisation
 * it was written to avoid, so it is for validating a build, not for playing on.
 *
 * <p>{@code /mmsnbt} flips both mid-session. See {@link NbtCommand}.
 */
public final class NbtTuning {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(), "mms_compat_nbt.json");

    /** Serve supported {@code nbt(...)} paths from live entity state. */
    public static boolean fastPath = true;

    /** Run both paths and log disagreements. Costs what the fast path saves. */
    public static boolean verify = false;

    private NbtTuning() {
    }

    private static final class Data {
        boolean fast_path = true;
        boolean verify = false;
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(FILE)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                fastPath = data.fast_path;
                verify = data.verify;
            }
        } catch (Exception e) {
            LOG.error("[mms_compat] failed to read {}; keeping defaults", FILE.getName(), e);
        }
        LOG.info("[mms_compat] nbt fast path: {}{}",
                fastPath ? "on" : "off", verify ? " (verifying)" : "");
    }

    public static void save() {
        Data data = new Data();
        data.fast_path = fastPath;
        data.verify = verify;
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
