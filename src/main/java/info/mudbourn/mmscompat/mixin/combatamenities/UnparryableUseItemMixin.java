package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.UnparryableDamage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Shows no raised item to Combat Amenities' parry check for damage with no attacker.
@Mixin(LivingEntity.class)
public abstract class UnparryableUseItemMixin {

    @Inject(method = "getUseItem", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$hideUseItem(CallbackInfoReturnable<ItemStack> cir) {
        if (UnparryableDamage.marked((LivingEntity) (Object) this)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
