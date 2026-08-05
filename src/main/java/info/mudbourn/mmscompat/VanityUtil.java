package info.mudbourn.mmscompat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;

/**
 * Utility for applying vanity (cosmetic-only) components to armor.
 * Zeroes armor, armor_toughness, knockback_resistance; makes unbreakable;
 * clears enchantments; adds lore.
 */
public class VanityUtil {

    public static void applyVanity(ItemStack stack) {
        // The machine-readable marker. Everything else here removes an effect; this
        // one exists for the few that cannot be removed and have to be gated instead.
        // See VanityMarker for when that is legitimate.
        stack.set(VanityMarker.COMPONENT, Unit.INSTANCE);

        // Unbreakable (Unit marker component)
        stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);

        // Lore marker
        stack.set(DataComponents.LORE, new ItemLore(
            List.of(Component.literal("(Vanity)").withStyle(style -> style.withColor(0xAAAAAA).withItalic(true)))
        ));

        // Zero out armor attributes \u2014 replaces the item's default modifiers wholesale
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
            .add(
                Attributes.ARMOR,
                new AttributeModifier(mmsCompatId("vanity_no_armor"), 0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR
            )
            .add(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(mmsCompatId("vanity_no_toughness"), 0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR
            )
            .add(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(mmsCompatId("vanity_no_kb"), 0, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.ARMOR
            )
            .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);

        // Clear enchantments \u2014 Protection/Thorns/etc. apply independently of the
        // armor attribute, so a vanity'd item with enchants is misleadingly powerful.
        stack.remove(DataComponents.ENCHANTMENTS);
        stack.remove(DataComponents.STORED_ENCHANTMENTS);

        // Behaviour that is a component rather than a hardcoded item check, and so
        // can simply be taken off the stack.
        //
        // GLIDER is the one that matters: since 1.21.5 elytra flight is driven by this
        // marker, not by the item being an elytra. Removing it leaves a stack that
        // still renders wings \u2014 those come from the equipment asset's "wings" layer,
        // which the item keeps \u2014 but no longer flies. That is the whole reason the
        // elytra does not need to be skinned onto another item to be made inert.
        //
        // The other two cannot appear on armour today. They are removed anyway because
        // the cost is nil and a kit row is one edit away from handing out a shield or a
        // totem, at which point their absence would be a silent gameplay effect.
        stack.remove(DataComponents.GLIDER);
        stack.remove(DataComponents.DEATH_PROTECTION);
        stack.remove(DataComponents.BLOCKS_ATTACKS);
    }

    private static Identifier mmsCompatId(String path) {
        return Identifier.fromNamespaceAndPath("mms_compat", path);
    }
}
