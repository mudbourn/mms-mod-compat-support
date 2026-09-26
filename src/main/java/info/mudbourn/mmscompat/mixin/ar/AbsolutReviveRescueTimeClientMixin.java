package info.mudbourn.mmscompat.mixin.ar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * AbsolutRevive mixin: matches the client prompt and progress bar to the
 * shortened bare-hand revive (see AbsolutReviveRescueTimeMixin).
 */
@Mixin(targets = "goetic.mods.absolutrevive.client.ClientEventHandler")
public class AbsolutReviveRescueTimeClientMixin {

    @ModifyConstant(method = "updateInteractionPromptState", constant = @Constant(intValue = 160))
    private static int mmsCompat$promptTicks(int original) {
        return 70;
    }

    @ModifyConstant(method = "getInteractionHoldDurationSeconds", constant = @Constant(floatValue = 8.0F))
    private static float mmsCompat$promptSeconds(float original) {
        return 3.5F;
    }

    @ModifyConstant(
            method = {"getInteractionHoldProgress", "getDisplayedInteractionHoldTicks"},
            constant = @Constant(floatValue = 160.0F))
    private static float mmsCompat$progressTicks(float original) {
        return 70.0F;
    }
}
