package info.mudbourn.mmscompat.mixin.scorchful;

import com.github.thedeathlycow.scorchful.config.section.TemperatureConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Being on fire heats mobs but not players, so fire weapons such as the Phoenix kit cannot give players heat stroke
// in PvP. Lava, fire charges and hot environments still heat players as usual.
@Mixin(targets = "com.github.thedeathlycow.scorchful.temperature.tick.ActiveTemperatureTickListeners")
public abstract class PlayerBurnHeatMixin {

    @Inject(method = "getOnFireTemperatureChange", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$noBurnHeatForPlayers(LivingEntity entity, TemperatureConfig config, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof Player) {
            cir.setReturnValue(0);
        }
    }
}
