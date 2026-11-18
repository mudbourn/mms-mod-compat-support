package info.mudbourn.mmscompat.ca;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

// Reads the Combat Amenities belt slot and the light level its block item would emit if placed
public final class BeltLight {

    private static EquipmentSlot beltSlot;

    private BeltLight() {
    }

    public static int of(LivingEntity entity) {
        EquipmentSlot belt = slot();
        return belt == null ? 0 : lightOf(entity.getItemBySlot(belt));
    }

    public static int lightOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return 0;
        }
        return blockItem.getBlock().defaultBlockState().getLightEmission();
    }

    // Combat Amenities adds the belt slot to the vanilla enum at load, so it is found by name.
    public static EquipmentSlot slot() {
        if (beltSlot == null) {
            for (EquipmentSlot candidate : EquipmentSlot.values()) {
                if (candidate.name().equals("COMBATAMENITIES_BELTSLOT")) {
                    beltSlot = candidate;
                }
            }
        }
        return beltSlot;
    }
}
