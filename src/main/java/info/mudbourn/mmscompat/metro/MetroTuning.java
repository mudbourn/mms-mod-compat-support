package info.mudbourn.mmscompat.metro;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metro tuning knobs, written to {@code config/mms_compat_metro.json} on first
 * run. Covers the cruise-zone governor ({@code MetroCruiseZoneMixin}) and the
 * heading-reversal hysteresis ({@code MetroHeadingFlipMixin}).
 *
 * <pre>
 * {
 *   "cruise_enabled": true,
 *   "cruise_speed": 0.8,
 *   "cruise_marker_block": "minecraft:target",
 *   "heading_flip_distance": 3.0,
 *   "heading_flip_debug": false
 * }
 * </pre>
 *
 * Why 0.8 b/t by default: a cart moves {@code speed} blocks per tick, so above
 * 1.0 it can land PAST a one-block detector rail without ever occupying it, and
 * the switch it drives never fires. The MMS line runs at 3.4 b/t — over three
 * blocks a tick — which is why trains dart through junctions unredirected.
 * Anything below 1.0 makes skipping a block impossible; 0.8 leaves margin.
 *
 * The marker block is deliberately NOT ModMetro's {@code metro_model_block}:
 * that one already drives the staged slow-zone ramp, and a cruise zone needs to
 * be able to overlap a ramp without the two triggering each other. Default is
 * {@code minecraft:target} — decorative, essentially never structural, and it
 * sits in the trackbed under the rail where nothing else competes for it.
 */
public final class MetroTuning {

    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(), "mms_compat_metro.json");

    public static boolean cruise_enabled = true;
    public static double cruise_speed = 0.8;
    public static String cruise_marker_block = "minecraft:target";

    /**
     * How far a cart must actually travel AGAINST its cached heading before the
     * reversal is believed, in blocks.
     *
     * This was originally a tick count (5), chosen when the line ran at 3.4 b/t
     * — where 5 ticks meant ~17 blocks of backwards travel and no transient
     * could fake it. Cruise zones then dropped carts to 0.8 b/t, making the
     * same 5 ticks barely 4 blocks, and under braking well under one: a
     * momentary vanilla rail bounce at a junction cleared it, the flip was
     * recorded as genuine, and the train drove back the way it came. Distance
     * is speed-invariant, so the guard means the same thing at every speed.
     */
    public static double heading_flip_distance = 3.0;

    /** Log every accepted heading flip with its position. Diagnostic only. */
    public static boolean heading_flip_debug = false;

    /** Resolved lazily — block registries are not populated at mod-init time. */
    private static Block resolvedMarker;
    private static String resolvedFrom;

    private MetroTuning() {
    }

    private static final class Data {
        boolean cruise_enabled = true;
        double cruise_speed = 0.8;
        String cruise_marker_block = "minecraft:target";
        double heading_flip_distance = 3.0;
        boolean heading_flip_debug = false;
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(FILE)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                cruise_enabled = data.cruise_enabled;
                cruise_speed = data.cruise_speed;
                if (data.cruise_marker_block != null && !data.cruise_marker_block.isBlank()) {
                    cruise_marker_block = data.cruise_marker_block;
                }
                if (data.heading_flip_distance > 0.0) {
                    heading_flip_distance = data.heading_flip_distance;
                }
                heading_flip_debug = data.heading_flip_debug;
            }
        } catch (Exception e) {
            LOG.warn("[mms_compat] could not read {} — using defaults", FILE.getName(), e);
        }
        resolvedMarker = null; // force re-resolve against the (possibly new) id
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            Data data = new Data();
            data.cruise_enabled = cruise_enabled;
            data.cruise_speed = cruise_speed;
            data.cruise_marker_block = cruise_marker_block;
            data.heading_flip_distance = heading_flip_distance;
            data.heading_flip_debug = heading_flip_debug;
            GSON.toJson(data, writer);
        } catch (Exception e) {
            LOG.warn("[mms_compat] could not write {}", FILE.getName(), e);
        }
    }

    /**
     * The configured marker block, or null if the id does not resolve — in
     * which case the governor stays off rather than silently clamping on some
     * fallback block the builder never placed.
     */
    public static org.slf4j.Logger log() {
        return LOG;
    }

    public static Block marker() {
        if (resolvedMarker != null && cruise_marker_block.equals(resolvedFrom)) {
            return resolvedMarker;
        }
        Identifier id = Identifier.tryParse(cruise_marker_block);
        Block block = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        if (block == null || block == Blocks.AIR) {
            LOG.warn("[mms_compat] cruise_marker_block '{}' does not resolve to a block — "
                    + "cruise zones disabled", cruise_marker_block);
            return null;
        }
        resolvedMarker = block;
        resolvedFrom = cruise_marker_block;
        return block;
    }
}
