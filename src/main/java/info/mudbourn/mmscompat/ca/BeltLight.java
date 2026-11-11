package info.mudbourn.mmscompat.ca;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Reads the light level a block item in the Trinkets belt slot (Combat
 * Amenities' belt) would emit if placed.
 */
public final class BeltLight {

    private BeltLight() {
    }

    public static int of(LivingEntity entity) {
        ItemStack stack = TrinketsApi.getTrinketComponent(entity)
            .map(c -> c.getInventory().get("legs"))
            .map(g -> g.get("belt"))
            .filter(inv -> inv.getContainerSize() > 0)
            .map(BeltLight::firstItem)
            .orElse(ItemStack.EMPTY);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return 0;
        }
        return blockItem.getBlock().defaultBlockState().getLightEmission();
    }

    private static ItemStack firstItem(TrinketInventory inv) {
        return inv.getItem(0);
    }
}
