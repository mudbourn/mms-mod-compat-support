package info.mudbourn.mmscompat.mixin.frostiful;

import com.github.thedeathlycow.frostiful.survival.system.IceSkateSystem;
import info.mudbourn.mmsvanity.VanityMarker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Stops vanity ice skates from skating, since every skating check goes through isWearingSkates.
@Mixin(value = IceSkateSystem.class, remap = false)
public abstract class IceSkateVanityMixin {

    @Inject(method = "isWearingSkates", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$vanitySkatesDoNotSkate(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (VanityMarker.isVanity(entity.getItemBySlot(EquipmentSlot.FEET))) {
            cir.setReturnValue(false);
        }
    }
}
