package info.mudbourn.mmscompat.mixin.frostiful;

import com.github.thedeathlycow.frostiful.survival.PassiveTemperatureEffects;
import com.github.thedeathlycow.thermoo.api.temperature.event.EnvironmentTickContext;
import info.mudbourn.mmscompat.ca.LanternHolster;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Feeds a belt-slot light into Frostiful's block-light warmth, so it warms and
 * melts snow exactly like the same light placed at the wearer's feet.
 */
@Mixin(value = PassiveTemperatureEffects.class, remap = false)
public abstract class HolsterLightWarmthMixin {

    @ModifyVariable(method = "getAndUpdateBlockLightTemperatureChange", at = @At("STORE"), ordinal = 0)
    private static int mms_compat$holsterLight(int warmth, EnvironmentTickContext<? extends LivingEntity> context) {
        if (!FabricLoader.getInstance().isModLoaded("trinkets")
            || !(context.affected() instanceof ServerPlayer player)) {
            return warmth;
        }
        return LanternHolster.withHolster(player, warmth);
    }
}
