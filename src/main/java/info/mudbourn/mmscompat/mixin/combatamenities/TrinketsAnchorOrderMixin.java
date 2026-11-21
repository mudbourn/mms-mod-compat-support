package info.mudbourn.mmscompat.mixin.combatamenities;

import eu.pb4.trinkets.impl.TrinketInventoryMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Trinkets builds its slots and group anchors at the end of the InventoryMenu constructor
// (priority 500), before Combat Amenities appends its back and belt slots. So the spell and
// misc groups anchored at slots 46 and 47 found Trinkets' own slots there and fell back to
// the sidebar, and hovering the back or belt slot opened nothing.
//
// Once Combat Amenities is done, rebuilding the Trinkets slots moves them after the back and
// belt slots, which leaves those at 46 and 47; a second rebuild then records the anchors
// against them. Runs on both sides so slot numbers match between client and server.
@Mixin(value = InventoryMenu.class, priority = 1500)
public abstract class TrinketsAnchorOrderMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mmsCompat$anchorAfterExtraSlots(CallbackInfo ci) {
        TrinketInventoryMenu trinkets = (TrinketInventoryMenu) this;
        trinkets.trinkets$updateTrinketSlots(false);
        mmsCompat$renumber();
        trinkets.trinkets$updateTrinketSlots(false);
        mmsCompat$renumber();
    }

    // Trinkets removes its slots by list position without renumbering what shifts down.
    private void mmsCompat$renumber() {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        for (int i = 0; i < menu.slots.size(); i++) {
            menu.slots.get(i).index = i;
        }
    }
}
