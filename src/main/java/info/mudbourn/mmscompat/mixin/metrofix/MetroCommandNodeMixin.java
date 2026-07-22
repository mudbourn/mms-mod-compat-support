package info.mudbourn.mmscompat.mixin.metrofix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.example.modmetro.MetroMod;

/**
 * Translates ModMetro's Spanish command NODE names to English:
 *
 *   /metro tren quitar [todos]      -> /metro train remove [all]
 *   /metro spawn [vagones]          -> /metro spawn [cars]
 *   /metro config <variable> <valor> -> /metro config <variable> <value>
 *
 * The earlier localization pass (MetroCommandTextMixin) deliberately left
 * node names alone; Eli has since decided the API break is acceptable —
 * any command block still using the Spanish forms must be updated.
 *
 * The literals live in the command-registration lambda ($14), and the
 * argument names are ALSO looked up by string in the executes handlers
 * ($5: valor/variable, $7: vagones) — every occurrence must rename in
 * lockstep or Brigadier throws IllegalArgumentException at execution.
 * Lambda numbering pinned to modmetro-v1.jar (1.0.0,
 * md5 28046740b19462b86441dbb44e25fe64), verified via javap;
 * defaultRequire=1 fails loudly on a mismatch.
 */
@Mixin(MetroMod.class)
public abstract class MetroCommandNodeMixin {

    @ModifyConstant(method = "lambda$onInitialize$14", constant = @Constant(stringValue = "tren"))
    private static String mmsCompat$train(String s) {
        return "train";
    }

    @ModifyConstant(method = "lambda$onInitialize$14", constant = @Constant(stringValue = "quitar"))
    private static String mmsCompat$remove(String s) {
        return "remove";
    }

    @ModifyConstant(method = "lambda$onInitialize$14", constant = @Constant(stringValue = "todos"))
    private static String mmsCompat$all(String s) {
        return "all";
    }

    @ModifyConstant(
        method = {"lambda$onInitialize$14", "lambda$onInitialize$7"},
        constant = @Constant(stringValue = "vagones")
    )
    private static String mmsCompat$cars(String s) {
        return "cars";
    }

    @ModifyConstant(
        method = {"lambda$onInitialize$14", "lambda$onInitialize$5"},
        constant = @Constant(stringValue = "valor")
    )
    private static String mmsCompat$value(String s) {
        return "value";
    }
}
