package info.mudbourn.mmscompat.mixin.ar;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * AbsolutRevive mixin: shortens the bare-hand revive.
 *
 * tickRescueProgress picks 60 ticks with a defibrillator and 160 ticks (8s)
 * without. Drop the bare-hand time to 70 ticks (3.5s); the defibrillator keeps
 * its 3s and its larger health restore. 160 is unique in the method.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveRescueTimeMixin {

    @ModifyConstant(method = "tickRescueProgress", constant = @Constant(intValue = 160))
    private static int mmsCompat$shortenRescue(int original) {
        return 70;
    }
}
