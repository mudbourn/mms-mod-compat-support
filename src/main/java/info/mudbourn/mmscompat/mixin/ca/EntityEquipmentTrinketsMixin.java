package info.mudbourn.mmscompat.mixin.ca;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import info.mudbourn.mmscompat.duck.EntityEquipmentDuck;
import net.hollowed.combatamenities.util.entities.ExtraSlots;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Redirects Combat Amenities' EntityEquipment get/put to read/write
 * from Trinkets' chest/back and legs/belt slots instead.
 * Also adds the owner back-reference via EntityEquipmentDuck.
 *
 * Uses @Inject(HEAD, cancellable) on the CA get/put methods directly.
 * When Trinkets slot is available, cancel and return Trinkets value.
 * When not available, fall through to CA's original EnumMap behavior.
 */
@Pseudo
@Mixin(targets = "net.hollowed.combatamenities.util.entities.EntityEquipment", remap = false)
public abstract class EntityEquipmentTrinketsMixin implements EntityEquipmentDuck {

    @Unique
    private LivingEntity mms_owner;

    @Override
    public LivingEntity mms_compat$getOwner() {
        return mms_owner;
    }

    @Override
    public void mms_compat$setOwner(LivingEntity owner) {
        mms_owner = owner;
    }

    /**
     * Resolve the Trinkets slot inventory for a given ExtraSlots enum value.
     * Returns empty if Trinkets component is absent or the slot group/slot doesn't exist.
     */
    @Unique
    private Optional<TrinketInventory> mms_compat$getTrinketInv(ExtraSlots slot) {
        if (mms_owner == null) return Optional.empty();
        return TrinketsApi.getTrinketComponent(mms_owner)
                .flatMap(c -> {
                    String group = slot == ExtraSlots.BACKSLOT ? "chest" : "legs";
                    String slotName = slot == ExtraSlots.BACKSLOT ? "back" : "belt";
                    var groupMap = c.getInventory().get(group);
                    if (groupMap == null) return Optional.empty();
                    TrinketInventory inv = groupMap.get(slotName);
                    return Optional.ofNullable(inv);
                });
    }

    /**
     * Redirect get(ExtraSlots) at HEAD — read from Trinkets if available.
     */
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void mms_compat$redirectGet(ExtraSlots slot, CallbackInfoReturnable<ItemStack> cir) {
        Optional<TrinketInventory> inv = mms_compat$getTrinketInv(slot);
        if (inv.isPresent()) {
            cir.setReturnValue(inv.get().getItem(0));
        }
    }

    /**
     * Redirect put(ExtraSlots, ItemStack) at HEAD — write to Trinkets if available.
     * Returns the previous stack (CA's put contract).
     */
    @Inject(method = "put", at = @At("HEAD"), cancellable = true)
    private void mms_compat$redirectPut(ExtraSlots slot, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        Optional<TrinketInventory> inv = mms_compat$getTrinketInv(slot);
        if (inv.isPresent()) {
            TrinketInventory trinketInv = inv.get();
            ItemStack previous = trinketInv.getItem(0);
            trinketInv.setItem(0, stack);
            cir.setReturnValue(previous);
        }
    }
}
