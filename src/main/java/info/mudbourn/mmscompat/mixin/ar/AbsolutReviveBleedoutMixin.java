package info.mudbourn.mmscompat.mixin.ar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

/**
 * AbsolutRevive mixin — shortens the bleedout countdown.
 *
 * handleFatalPlayerDamage calls setUnconsciousState(1800, true, true,
 * "critical_condition") — 1800 ticks = 90 seconds before the downed player
 * dies out. That is too long to wait for a rescue that isn't coming; drop it
 * to 600 ticks (30 seconds). The countdown ticks down in PlayerDamageModel.tick
 * and death fires when it reaches 0, so this is the only value that governs it.
 *
 * 1800 is unique within handleFatalPlayerDamage (verified against the compiled
 * method), so the constant match is unambiguous.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveBleedoutMixin {

    @ModifyConstant(method = "handleFatalPlayerDamage", constant = @Constant(intValue = 1800))
    private static int mmsCompat$shortenBleedout(int original) {
        return 600;
    }
}
