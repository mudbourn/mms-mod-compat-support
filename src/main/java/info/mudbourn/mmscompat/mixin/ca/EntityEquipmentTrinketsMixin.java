package info.mudbourn.mmscompat.mixin.ca;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import info.mudbourn.mmscompat.duck.EntityEquipmentDuck;
import net.hollowed.combatamenities.util.entities.ExtraSlots;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Makes Trinkets' chest/back and legs/belt slots the only store for Combat
 * Amenities' back and belt equipment. CA's own map only holds stacks read from
 * old saves, which move into Trinkets on the first server-side read.
 */
@Pseudo
@Mixin(targets = "net.hollowed.combatamenities.util.entities.EntityEquipment", remap = false)
public abstract class EntityEquipmentTrinketsMixin implements EntityEquipmentDuck {

    @Shadow
    @Final
    private EnumMap<ExtraSlots, ItemStack> map;

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

    @Unique
    private TrinketInventory mms_compat$trinketInv(ExtraSlots slot) {
        if (mms_owner == null) return null;

        String group = slot == ExtraSlots.BACKSLOT ? "chest" : "legs";
        String name = slot == ExtraSlots.BACKSLOT ? "back" : "belt";
        return TrinketsApi.getTrinketComponent(mms_owner)
            .map(c -> c.getInventory().get(group))
            .map(g -> g.get(name))
            .filter(inv -> inv.getContainerSize() > 0)
            .orElse(null);
    }

    @Unique
    private boolean mms_compat$owned() {
        return mms_compat$trinketInv(ExtraSlots.BACKSLOT) != null
            && mms_compat$trinketInv(ExtraSlots.BELTSLOT) != null;
    }

    // Moves stacks loaded from CA's own save data into Trinkets.
    @Unique
    private void mms_compat$migrateLegacy() {
        if (map.isEmpty() || mms_owner.level().isClientSide()) return;

        for (Map.Entry<ExtraSlots, ItemStack> entry : map.entrySet()) {
            ItemStack legacy = entry.getValue();
            if (legacy.isEmpty()) continue;

            TrinketInventory inv = mms_compat$trinketInv(entry.getKey());
            if (inv.getItem(0).isEmpty()) {
                inv.setItem(0, legacy);
            } else if (mms_owner instanceof Player player) {
                player.getInventory().placeItemBackInInventory(legacy);
            } else {
                mms_owner.spawnAtLocation((ServerLevel) mms_owner.level(), legacy);
            }
        }
        map.clear();
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void mms_compat$get(ExtraSlots slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!mms_compat$owned()) return;

        mms_compat$migrateLegacy();
        cir.setReturnValue(mms_compat$trinketInv(slot).getItem(0));
    }

    @Inject(method = "put", at = @At("HEAD"), cancellable = true)
    private void mms_compat$put(ExtraSlots slot, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!mms_compat$owned()) return;

        mms_compat$migrateLegacy();
        TrinketInventory inv = mms_compat$trinketInv(slot);
        ItemStack previous = inv.getItem(0);
        inv.setItem(0, stack);
        cir.setReturnValue(previous);
    }

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void mms_compat$isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!mms_compat$owned()) return;

        boolean legacyEmpty = map.values().stream().allMatch(ItemStack::isEmpty);
        cir.setReturnValue(
            legacyEmpty
                && mms_compat$trinketInv(ExtraSlots.BACKSLOT).getItem(0).isEmpty()
                && mms_compat$trinketInv(ExtraSlots.BELTSLOT).getItem(0).isEmpty()
        );
    }

    @Inject(method = "clear", at = @At("HEAD"), cancellable = true)
    private void mms_compat$clear(CallbackInfo ci) {
        if (!mms_compat$owned()) return;

        map.clear();
        mms_compat$trinketInv(ExtraSlots.BACKSLOT).setItem(0, ItemStack.EMPTY);
        mms_compat$trinketInv(ExtraSlots.BELTSLOT).setItem(0, ItemStack.EMPTY);
        ci.cancel();
    }

    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
    private void mms_compat$dropAll(LivingEntity entity, CallbackInfo ci) {
        if (!mms_compat$owned()) return;

        mms_compat$migrateLegacy();
        for (ExtraSlots slot : ExtraSlots.values()) {
            TrinketInventory inv = mms_compat$trinketInv(slot);
            entity.drop(inv.getItem(0), true, false);
            inv.setItem(0, ItemStack.EMPTY);
        }
        ci.cancel();
    }
}
