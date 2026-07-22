package info.mudbourn.mmscompat.mixin.metrofix;

import com.example.modmetro.MetroMod;
import info.mudbourn.mmscompat.MetroText;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Localizes ModMetro's command feedback.
 *
 * Two strategies, because the messages come in two shapes:
 *
 *  - Three are plain ldc constants → @ModifyConstant.
 *  - Three are assembled by invokedynamic string concatenation and have no
 *    reachable constant. Each lives in a small lambda of the form
 *    {@code concat → Component.literal → areturn}, so we @Inject at HEAD
 *    and return a Component.translatable built from the lambda's own typed
 *    arguments. That is cleaner than rewriting the finished string (which
 *    is what MetroText.rewriteHud has to do on the HUD, where the values
 *    are already flattened into the drawn text) and it keeps the numbers
 *    as real format arguments.
 *
 * Command node names (metro/config/variable/valor/spawn/vagones/tren/
 * quitar/todos) are deliberately NOT touched: they are the command API,
 * and renaming them would break every command block and script on the
 * server the moment this patch is removed or updated.
 *
 * Lambda targets are synthetic names tied to this exact ModMetro build
 * (1.0.0, modmetro-v1.jar md5 28046740b19462b86441dbb44e25fe64). A recompile
 * of ModMetro can renumber them; because the config sets defaultRequire=1,
 * that surfaces as a loud load-time failure rather than silently reverting
 * to Spanish. No version pin is declared in fabric.mod.json — mms_compat
 * patches seven other mods and must still load when ModMetro is absent or
 * updated.
 *
 * Verified lambda map for the md5 above (javap of com.example.modmetro.MetroMod):
 *   must_be_riding   "Debes estar montado…"            -> lambda$onInitialize$10
 *   train_removed    "Tren removido correctamente."    -> lambda$onInitialize$9
 *   unknown_variable "Variable desconocida."           -> lambda$onInitialize$5
 *   train_spawned    (concat, int)                     -> lambda$spawnTrain$15
 *   cars_removed     (concat, int)                     -> lambda$onInitialize$12
 *   config_updated   (concat, String,double)           -> lambda$onInitialize$4
 * When ModMetro changes, re-derive with:
 *   javap -p -c com/example/modmetro/MetroMod.class | grep -B<n> "<spanish string>"
 * and match each class_2561-returning lambda by signature, not just number.
 */
@Mixin(MetroMod.class)
public abstract class MetroCommandTextMixin {

    @ModifyConstant(
        method = "lambda$onInitialize$10",
        constant = @Constant(stringValue = "Debes estar montado en un vagón del metro para usar este comando.")
    )
    private static String mmsCompat$mustBeRiding(String original) {
        return MetroText.tr("mms_compat.metro.cmd.must_be_riding");
    }

    @ModifyConstant(
        method = "lambda$onInitialize$9",
        constant = @Constant(stringValue = "Tren removido correctamente.")
    )
    private static String mmsCompat$trainRemoved(String original) {
        return MetroText.tr("mms_compat.metro.cmd.train_removed");
    }

    @ModifyConstant(
        method = "lambda$onInitialize$5",
        constant = @Constant(stringValue = "Variable desconocida.")
    )
    private static String mmsCompat$unknownVariable(String original) {
        return MetroText.tr("mms_compat.metro.cmd.unknown_variable");
    }

    // ===== Concatenated messages (injected, not constant-modified) =====

    /** "Tren de metro generado con N vagones." */
    @Inject(method = "lambda$spawnTrain$15", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$trainSpawned(int cars, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("mms_compat.metro.cmd.train_spawned", cars));
    }

    /** "Se removieron N vagones en total." */
    @Inject(method = "lambda$onInitialize$12", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$carsRemoved(int cars, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("mms_compat.metro.cmd.cars_removed", cars));
    }

    /** "Configuración actualizada: X = Y" */
    @Inject(method = "lambda$onInitialize$4", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$configUpdated(String variable, double value,
                                                CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.translatable("mms_compat.metro.cmd.config_updated", variable, value));
    }
}
