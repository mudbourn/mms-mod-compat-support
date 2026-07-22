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
        // weaponsexpanded longswords (7)
        Identifier.parse("weaponsexpanded:wooden_longsword"),
        Identifier.parse("weaponsexpanded:stone_longsword"),
        Identifier.parse("weaponsexpanded:copper_longsword"),
        Identifier.parse("weaponsexpanded:iron_longsword"),
        Identifier.parse("weaponsexpanded:golden_longsword"),
        Identifier.parse("weaponsexpanded:diamond_longsword"),
        Identifier.parse("weaponsexpanded:netherite_longsword"),
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
        Identifier.parse("weaponsexpanded:longbow"),
        // jobsplustools longswords (7)
        Identifier.parse("jobsplustools:copper_longsword"),
        Identifier.parse("jobsplustools:diamond_longsword"),
        Identifier.parse("jobsplustools:golden_longsword"),
        Identifier.parse("jobsplustools:iron_longsword"),
        Identifier.parse("jobsplustools:netherite_longsword"),
        Identifier.parse("jobsplustools:stone_longsword"),
        Identifier.parse("jobsplustools:wooden_longsword"),
        // jobsplustools compound bows (7)
        Identifier.parse("jobsplustools:copper_compound_bow"),
        Identifier.parse("jobsplustools:diamond_compound_bow"),
        Identifier.parse("jobsplustools:golden_compound_bow"),
        Identifier.parse("jobsplustools:iron_compound_bow"),
        Identifier.parse("jobsplustools:netherite_compound_bow"),
        Identifier.parse("jobsplustools:stone_compound_bow"),
        Identifier.parse("jobsplustools:wooden_compound_bow"),
        // === JEG guns, ammo, throwables, spawn eggs (61) ===
        Identifier.parse("jeg:abstract_gun"),
        Identifier.parse("jeg:assault_rifle"),
        Identifier.parse("jeg:blaze_round"),
        Identifier.parse("jeg:blossom_rifle"),
        Identifier.parse("jeg:bolt_action_rifle"),
        Identifier.parse("jeg:burst_rifle"),
        Identifier.parse("jeg:combat_pistol"),
        Identifier.parse("jeg:combat_rifle"),
        Identifier.parse("jeg:custom_smg"),
        Identifier.parse("jeg:double_barrel_shotgun"),
        Identifier.parse("jeg:finger_gun"),
        Identifier.parse("jeg:flamethrower"),
        Identifier.parse("jeg:flare"),
        Identifier.parse("jeg:flare_gun"),
        Identifier.parse("jeg:grenade"),
        Identifier.parse("jeg:grenade_launcher"),
        Identifier.parse("jeg:gunner_drowned_spawn_egg"),
        Identifier.parse("jeg:gunner_ghoul_spawn_egg"),
        Identifier.parse("jeg:gunner_husk_spawn_egg"),
        Identifier.parse("jeg:gunner_piglin_brute_spawn_egg"),
        Identifier.parse("jeg:gunner_piglin_spawn_egg"),
        Identifier.parse("jeg:gunner_pillager_spawn_egg"),
        Identifier.parse("jeg:gunner_skeleton_spawn_egg"),
        Identifier.parse("jeg:gunner_stray_spawn_egg"),
        Identifier.parse("jeg:gunner_vindicator_spawn_egg"),
        Identifier.parse("jeg:gunner_wither_skeleton_spawn_egg"),
        Identifier.parse("jeg:gunner_zombie_spawn_egg"),
        Identifier.parse("jeg:gunner_zombie_villager_spawn_egg"),
        Identifier.parse("jeg:gunner_zombified_piglin_spawn_egg"),
        Identifier.parse("jeg:handmade_shell"),
        Identifier.parse("jeg:hollenfire_mk2"),
        Identifier.parse("jeg:holy_shotgun"),
        Identifier.parse("jeg:hypersonic_cannon"),
        Identifier.parse("jeg:infantry_rifle"),
        Identifier.parse("jeg:light_machine_gun"),
        Identifier.parse("jeg:minigun"),
        Identifier.parse("jeg:molotov_cocktail"),
        Identifier.parse("jeg:phantom_gunner_spawn_egg"),
        Identifier.parse("jeg:phantom_smg"),
        Identifier.parse("jeg:pistol_ammo"),
        Identifier.parse("jeg:pump_shotgun"),
        Identifier.parse("jeg:repeating_shotgun"),
        Identifier.parse("jeg:revolver"),
        Identifier.parse("jeg:rifle_ammo"),
        Identifier.parse("jeg:rocket"),
        Identifier.parse("jeg:rocket_launcher"),
        Identifier.parse("jeg:semi_auto_pistol"),
        Identifier.parse("jeg:semi_auto_rifle"),
        Identifier.parse("jeg:service_rifle"),
        Identifier.parse("jeg:shotgun_shell"),
        Identifier.parse("jeg:smoke_grenade"),
        Identifier.parse("jeg:soulhunter_mk2"),
        Identifier.parse("jeg:spectre_round"),
        Identifier.parse("jeg:stun_grenade"),
        Identifier.parse("jeg:subsonic_rifle"),
        Identifier.parse("jeg:supersonic_shotgun"),
        Identifier.parse("jeg:terror_phantom_guardian_spawn_egg"),
        Identifier.parse("jeg:terror_phantom_spawn_egg"),
        Identifier.parse("jeg:typhoonee"),
        Identifier.parse("jeg:water_bomb"),
        Identifier.parse("jeg:waterpipe_shotgun")
        // total: 118 (57 weapon-dups + 61 JEG)
    );

    public static void register() {
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
            entries.getDisplayStacks().removeIf(CreativeEmbargo::isEmbargoed);
            entries.getSearchTabStacks().removeIf(CreativeEmbargo::isEmbargoed);
        });
    }

    private static boolean isEmbargoed(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return EMBARGO.contains(id);
    }
}
