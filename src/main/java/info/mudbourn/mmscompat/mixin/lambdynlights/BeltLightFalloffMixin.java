package info.mudbourn.mmscompat.mixin.lambdynlights;

import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupEntityEntry;
import dev.lambdaurora.lambdynlights.engine.source.EntityDynamicLightSource;
import info.mudbourn.mmscompat.ca.BeltLight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives a light carried in the belt slot the one-level-per-block falloff of the
 * same light placed down, instead of LambDynamicLights' steeper entity falloff.
 */
@Mixin(value = SpatialLookupEntityEntry.class, remap = false)
public abstract class BeltLightFalloffMixin {

    @Shadow
    public abstract EntityDynamicLightSource source();

    @Inject(method = "getDynamicLightLevel", at = @At("RETURN"), cancellable = true)
    private void mms_compat$placedFalloff(BlockPos pos, CallbackInfoReturnable<Double> cir) {
        if (!(source() instanceof LivingEntity entity)) {
            return;
        }
        int carried = BeltLight.of(entity);
        if (carried <= 0) {
            return;
        }
        double dx = pos.getX() + 0.5 - source().getDynamicLightX();
        double dy = pos.getY() + 0.5 - source().getDynamicLightY();
        double dz = pos.getZ() + 0.5 - source().getDynamicLightZ();
        double placed = carried - Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (placed > cir.getReturnValueD()) {
            cir.setReturnValue(placed);
        }
    }
}
