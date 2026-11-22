package info.mudbourn.mmscompat.mixin.combatamenities;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.impl.slots.TrinketSlotState;
import org.spongepowered.asm.mixin.Mixin;

// TrinketsAnchorOrderMixin anchors the spell and misc groups to Combat Amenities' back and belt
// slots (46 and 47). The creative inventory has no such slots, so Trinkets finds no position for
// those groups there and crashes opening the creative inventory. Those groups are left out of the
// creative inventory instead; they still show in the survival inventory.
@Mixin(targets = "eu.pb4.trinkets.impl.client.slot.legacy.LegacyTrinketSlotStateImpl$Creative")
public abstract class TrinketsCreativeAnchorMixin {

    @WrapMethod(method = "getSlotConfig")
    private TrinketSlotState.SlotInfo mmsCompat$skipUnplacedGroup(int slotIndex, TrinketInventory inventory, int index,
                                                                  Operation<TrinketSlotState.SlotInfo> original) {
        try {
            return original.call(slotIndex, inventory, index);
        } catch (NullPointerException e) {
            return TrinketSlotState.SlotInfo.FALLBACK;
        }
    }
}
