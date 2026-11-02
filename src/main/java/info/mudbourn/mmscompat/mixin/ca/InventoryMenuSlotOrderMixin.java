package info.mudbourn.mmscompat.mixin.ca;

import dev.emi.trinkets.TrinketPlayerScreenHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rebuilds Trinkets' slots after Combat Amenities has added its back and belt
 * slots, so client and server menus share one slot order with CA at 46 and 47.
 */
@Mixin(value = InventoryMenu.class, priority = 1100)
public abstract class InventoryMenuSlotOrderMixin {

    @Inject(
        method = "<init>(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V",
        at = @At("RETURN")
    )
    private void mms_compat$trinketsAfterCa(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        ((TrinketPlayerScreenHandler) this).trinkets$updateTrinketSlots(true);

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        for (int i = 0; i < menu.slots.size(); i++) {
            menu.slots.get(i).index = i;
        }
    }
}
