package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.ParryStreak;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// During a parry streak a raised shield or blocking weapon blocks from its first tick, skipping the raise delay.
@Mixin(LivingEntity.class)
public abstract class ParryStreakRaiseMixin {

    @Inject(method = "getItemBlockingWith", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$instantRaiseInStreak(CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isUsingItem()) {
            return;
        }
        ItemStack using = self.getUseItem();
        if (using.has(DataComponents.BLOCKS_ATTACKS) && ParryStreak.active(self)) {
            cir.setReturnValue(using);
        }
    }
}
