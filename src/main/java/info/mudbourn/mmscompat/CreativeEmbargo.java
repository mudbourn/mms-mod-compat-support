package info.mudbourn.mmscompat;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * Creative-menu embargo strip.
 * Removes weapon-duplicate items from creative tabs and creative search.
 * Uses a static ID list (not a datapack tag) because item tags aren't guaranteed
 * loaded when creative tabs first build. Keep in sync with the
 * #c:hidden_from_recipe_viewers tag (same ids).
 */
public class CreativeEmbargo {

    // Canonical source: the weapon-dup ids from the Item Embargo spec.
    // Keep in sync with data/c/tags/item/hidden_from_recipe_viewers.json.
    private static final Set<Identifier> EMBARGO = Set.of(
        // basicweapons hammers (7)
        Identifier.parse("basicweapons:wooden_hammer"),
        Identifier.parse("basicweapons:stone_hammer"),
        Identifier.parse("basicweapons:copper_hammer"),
        Identifier.parse("basicweapons:iron_hammer"),
        Identifier.parse("basicweapons:golden_hammer"),
        Identifier.parse("basicweapons:diamond_hammer"),
        Identifier.parse("basicweapons:netherite_hammer"),
        // basicweapons spears (7)
        Identifier.parse("basicweapons:wooden_spear"),
        Identifier.parse("basicweapons:stone_spear"),
        Identifier.parse("basicweapons:copper_spear"),
        Identifier.parse("basicweapons:iron_spear"),
        Identifier.parse("basicweapons:golden_spear"),
        Identifier.parse("basicweapons:diamond_spear"),
        Identifier.parse("basicweapons:netherite_spear"),
        // expanded_weaponry daggers (7)
        Identifier.parse("expanded_weaponry:wooden_dagger"),
        Identifier.parse("expanded_weaponry:stone_dagger"),
        Identifier.parse("expanded_weaponry:copper_dagger"),
        Identifier.parse("expanded_weaponry:iron_dagger"),
        Identifier.parse("expanded_weaponry:golden_dagger"),
        Identifier.parse("expanded_weaponry:diamond_dagger"),
        Identifier.parse("expanded_weaponry:netherite_dagger"),
        // weaponsexpanded hatchets (7)
        Identifier.parse("weaponsexpanded:wooden_hatchet"),
        Identifier.parse("weaponsexpanded:stone_hatchet"),
        Identifier.parse("weaponsexpanded:copper_hatchet"),
        Identifier.parse("weaponsexpanded:iron_hatchet"),
        Identifier.parse("weaponsexpanded:golden_hatchet"),
        Identifier.parse("weaponsexpanded:diamond_hatchet"),
        Identifier.parse("weaponsexpanded:netherite_hatchet"),
        // weaponsexpanded greatswords (7)
        Identifier.parse("weaponsexpanded:wooden_greatsword"),
        Identifier.parse("weaponsexpanded:stone_greatsword"),
        Identifier.parse("weaponsexpanded:copper_greatsword"),
        Identifier.parse("weaponsexpanded:iron_greatsword"),
        Identifier.parse("weaponsexpanded:golden_greatsword"),
        Identifier.parse("weaponsexpanded:diamond_greatsword"),
        Identifier.parse("weaponsexpanded:netherite_greatsword"),
        // weaponsexpanded hammers (7)
        Identifier.parse("weaponsexpanded:wooden_hammer"),
        Identifier.parse("weaponsexpanded:stone_hammer"),
        Identifier.parse("weaponsexpanded:copper_hammer"),
        Identifier.parse("weaponsexpanded:iron_hammer"),
        Identifier.parse("weaponsexpanded:golden_hammer"),
        Identifier.parse("weaponsexpanded:diamond_hammer"),
        Identifier.parse("weaponsexpanded:netherite_hammer"),
        // weaponsexpanded longbow (1)
        Identifier.parse("weaponsexpanded:longbow")
    );

    public static void register() {
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
            entries.getDisplayStacks().removeIf(CreativeEmbargo::isEmbargoed);
            entries.getSearchTabStacks().removeIf(CreativeEmbargo::isEmbargoed);
        });
    }

    // Package-external callers: MobEmbargoedEquipmentMixin reuses this so mob
    // equipment and the creative tabs cannot disagree about what is embargoed.
    public static boolean isEmbargoed(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return EMBARGO.contains(id);
    }
}
