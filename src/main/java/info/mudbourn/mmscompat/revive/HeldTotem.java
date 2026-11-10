package info.mudbourn.mmscompat.revive;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Finds a death-protection item a player could still use, in either hand or
 * any Trinkets slot (Friends and Foes lets totems pop from its charm slot).
 */
public final class HeldTotem {

    private static final boolean TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

    private HeldTotem() {}

    // True when the vanilla totem check would save this player.
    public static boolean has(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (protects(player.getItemInHand(hand))) {
                return true;
            }
        }
        return TRINKETS && inTrinkets(player);
    }

    private static boolean inTrinkets(Player player) {
        return TrinketsApi.getTrinketComponent(player)
            .map(component -> component.isEquipped(HeldTotem::protects))
            .orElse(false);
    }

    private static boolean protects(ItemStack stack) {
        return stack.has(DataComponents.DEATH_PROTECTION);
    }
}
