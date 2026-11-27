package info.mudbourn.mmscompat.mixin.frostiful;

import com.github.thedeathlycow.frostiful.survival.PassiveTemperatureEffects;
import com.github.thedeathlycow.thermoo.api.core.v2.event.EnvironmentTickContext;
import info.mudbourn.mmscompat.ca.LanternHolster;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Feeds a belt-slot light into Frostiful's block-light warmth
@Mixin(value = PassiveTemperatureEffects.class, remap = false)
public abstract class HolsterLightWarmthMixin {

    @ModifyVariable(method = "getAndUpdateBlockLightTemperatureChange", at = @At("STORE"), ordinal = 0)
    private static int mms_compat$holsterLight(int warmth, EnvironmentTickContext<? extends LivingEntity> context) {
        return LanternHolster.withHolster(context.affected(), warmth);
    }
}
