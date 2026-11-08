package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Logs the missing data fixer lookup for a modded block entity or entity type at
 * debug instead of error.
 *
 * <p>Every mod that registers its own block entity or entity type triggers
 * {@code No data fixer registered for <id>} at startup, because vanilla schemas
 * only know vanilla ids. The lookup is expected to miss and the type works
 * without a fixer, so the error level only makes startup logs look broken.
 */
@Mixin(Util.class)
public class DataFixerLookupQuietMixin {

    @WrapOperation(
        method = "doFetchChoiceType",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
            remap = false
        )
    )
    private static void mms$debugMissingFixer(Logger logger, String message, Object id, Operation<Void> original) {
        logger.debug(message, id);
    }
}
