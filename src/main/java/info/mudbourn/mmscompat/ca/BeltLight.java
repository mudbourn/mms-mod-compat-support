package info.mudbourn.mmscompat.ca;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Reads the Trinkets belt slot (Combat Amenities' belt) and the light level its
 * block item would emit if placed.
 */
public final class BeltLight {

    private BeltLight() {
    }

    public static int of(LivingEntity entity) {
        TrinketInventory slot = slot(entity);
        return slot == null ? 0 : lightOf(slot.getItem(0));
    }

    public static int lightOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return 0;
        }
        return blockItem.getBlock().defaultBlockState().getLightEmission();
    }

    public static TrinketInventory slot(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity)
            .map(c -> c.getInventory().get("legs"))
            .map(g -> g.get("belt"))
            .filter(inv -> inv.getContainerSize() > 0)
            .orElse(null);
    }
}
