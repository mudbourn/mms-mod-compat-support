package info.mudbourn.mmscompat.client.spellengine;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Tells whether a player's spells come only from the offhand, by Spell Engine's
 * spell_engine:spell_container component.
 */
public final class SpellContainers {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("spell_engine", "spell_container");

    private SpellContainers() {}

    public static boolean offhandOnly(Player player) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(ID);
        if (type == null) {
            return false;
        }
        ItemStack main = player.getMainHandItem();
        return !main.isEmpty() && !main.has(type) && player.getOffhandItem().has(type);
    }
}
