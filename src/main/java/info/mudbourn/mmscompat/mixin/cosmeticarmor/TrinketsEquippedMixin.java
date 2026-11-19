package info.mudbourn.mmscompat.mixin.cosmeticarmor;

import eu.pb4.trinkets.api.SlotType;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Cosmetic Armor 1.8.0 looks up the cosmetic armor slot through TrinketAttachment.getEquipped(Predicate), which Trinkets 4.2 replaced
// with equipped(Predicate, boolean), and reads the result as (slot, stack) pairs. This answers the same lookup through the new call:
// a stack that belongs in the asked equipment slot, held in a trinket slot named "cosmetic" whose group is that equipment slot.
// The flag is false: true keeps only slots that can apply item effects, and the old call had no such filter.
@Mixin(targets = "io.github.apace100.cosmetic_armor.CosmeticArmor")
public abstract class TrinketsEquippedMixin {
    @Inject(method = "getCosmeticArmor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$getCosmeticArmor(LivingEntity entity, EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
        if (attachment != null) {
            for (TrinketSlotAccess access : attachment.equipped(stack -> entity.getEquipmentSlotForItem(stack) == slot, false)) {
                SlotType type = access.inventory().slotType();
                if (type.name().equals("cosmetic") && type.group().equalsIgnoreCase(slot.getName())) {
                    cir.setReturnValue(access.get());
                    return;
                }
            }
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
