package info.mudbourn.mmscompat.aerialhell;

import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Aerial Hell totems only work held: their passive abilities run from the item's
 * inventoryTick and require the stack to be in the main or off hand. Worn in a trinket
 * slot they are never ticked. This ticks worn Aerial Hell totems every server tick,
 * and {@code TotemHandConditionMixin} lets the hand condition pass for the stack
 * being ticked here.
 */
public final class TrinketTotems {
    private static ItemStack ticking = ItemStack.EMPTY;

    private TrinketTotems() {}

    public static boolean isTicking(ItemStack stack) {
        return !ticking.isEmpty() && stack == ticking;
    }

    public static void register() {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("trinkets")) return;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                var attachment = TrinketsApi.getAttachment(player);
                if (attachment == null) continue;
                attachment.forEach((slot, stack) -> {
                    if (!isAerialTotem(stack)) return;
                    ticking = stack;
                    try {
                        stack.getItem().inventoryTick(stack, player.level(), player, EquipmentSlot.OFFHAND);
                    } finally {
                        ticking = ItemStack.EMPTY;
                    }
                });
            }
        });
    }

    private static boolean isAerialTotem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("aerialhell") && id.getPath().endsWith("_totem");
    }
}
