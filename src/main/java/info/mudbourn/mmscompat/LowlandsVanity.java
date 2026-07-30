package info.mudbourn.mmscompat;

import static info.mudbourn.mmscompat.VanityKits.add;
import info.mudbourn.mmscompat.VanityKits.Kit;
import info.mudbourn.mmscompat.VanityKits.Piece;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

/**
 * Clothing of the Lowlands armour sets, ported forward to 1.21.11 as vanity skins.
 *
 * Generated from Clothing_Of_The_Lowlands-1.0.8-neoforge-1.21.1.jar. Each piece is a
 * vanilla leather base carrying equippable.asset_id + item_model, so nothing is
 * registered and no client-side mod is needed — the look comes entirely from the
 * "lowlands-vanity" resource pack, which holds:
 *
 *   assets/lowlands_clothing/equipment/&lt;asset&gt;.json          worn layers
 *   assets/lowlands_clothing/textures/entity/equipment/...    layer_1 -> humanoid,
 *                                                            layer_2 -> humanoid_leggings
 *   assets/lowlands_clothing/items/&lt;item&gt;.json               inventory sprite
 *   assets/lowlands_clothing/lang/en_us.json                 display names
 *
 * Without that pack these show as leather armour with untranslated names — degraded,
 * not broken.
 *
 * Two sets are deliberately incomplete because the source mod has no such piece:
 * Highlander has no leggings, Swamplandfolks has no boots.
 */
final class LowlandsVanity {

    static void register() {
        add(new Kit("axolotl_knight", "Axolotl Knight", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:axolotl_knight", "lowlands_clothing:axolotl_armor_helmet", "item.lowlands_clothing.axolotl_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:axolotl_knight", "lowlands_clothing:axolotl_armor_chestplate", "item.lowlands_clothing.axolotl_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:axolotl_knight", "lowlands_clothing:axolotl_armor_leggings", "item.lowlands_clothing.axolotl_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:axolotl_knight", "lowlands_clothing:axolotl_armor_boots", "item.lowlands_clothing.axolotl_armor_boots")
        )));

        add(new Kit("bret", "Bret", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:bret_fight_armor", "lowlands_clothing:bret_clothes_helmet", "item.lowlands_clothing.bret_clothes_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:bret_fight_armor", "lowlands_clothing:bret_clothes_chestplate", "item.lowlands_clothing.bret_clothes_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:bret_fight_armor", "lowlands_clothing:bret_clothes_leggings", "item.lowlands_clothing.bret_clothes_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:bret_fight_armor", "lowlands_clothing:bret_clothes_boots", "item.lowlands_clothing.bret_clothes_boots")
        )));

        add(new Kit("bret_corsair", "Bret Corsair", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:bret_corsair_o__armor_v2", "lowlands_clothing:bret_corsair_armor_helmet", "item.lowlands_clothing.bret_corsair_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:bret_corsair_o__armor_v2", "lowlands_clothing:bret_corsair_armor_chestplate", "item.lowlands_clothing.bret_corsair_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:bret_corsair_o__armor_v2", "lowlands_clothing:bret_corsair_armor_leggings", "item.lowlands_clothing.bret_corsair_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:bret_corsair_o__armor_v2", "lowlands_clothing:bret_corsair_armor_boots", "item.lowlands_clothing.bret_corsair_armor_boots")
        )));

        add(new Kit("depth_scaphander", "Depth Scaphander", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:scaphander", "lowlands_clothing:depth_scaphander_helmet", "item.lowlands_clothing.depth_scaphander_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:scaphander", "lowlands_clothing:depth_scaphander_chestplate", "item.lowlands_clothing.depth_scaphander_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:scaphander", "lowlands_clothing:depth_scaphander_leggings", "item.lowlands_clothing.depth_scaphander_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:scaphander", "lowlands_clothing:depth_scaphander_boots", "item.lowlands_clothing.depth_scaphander_boots")
        )));

        add(new Kit("executioner", "Executioner", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:executioner_armor", "lowlands_clothing:executorarmor_helmet", "item.lowlands_clothing.executorarmor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:executioner_armor", "lowlands_clothing:executorarmor_chestplate", "item.lowlands_clothing.executorarmor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:executioner_armor", "lowlands_clothing:executorarmor_leggings", "item.lowlands_clothing.executorarmor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:executioner_armor", "lowlands_clothing:executorarmor_boots", "item.lowlands_clothing.executorarmor_boots")
        )));

        add(new Kit("furnace_master", "Furnace Master", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:furnace_master_armor", "lowlands_clothing:furnacemasteramor_helmet", "item.lowlands_clothing.furnacemasteramor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:furnace_master_armor", "lowlands_clothing:furnacemasteramor_chestplate", "item.lowlands_clothing.furnacemasteramor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:furnace_master_armor", "lowlands_clothing:furnacemasteramor_leggings", "item.lowlands_clothing.furnacemasteramor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:furnace_master_armor", "lowlands_clothing:furnacemasteramor_boots", "item.lowlands_clothing.furnacemasteramor_boots")
        )));

        add(new Kit("gamekeeper", "Gamekeeper", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:gamemaster_outfit", "lowlands_clothing:gamekeeper_attires_helmet", "item.lowlands_clothing.gamekeeper_attires_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:gamemaster_outfit", "lowlands_clothing:gamekeeper_attires_chestplate", "item.lowlands_clothing.gamekeeper_attires_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:gamemaster_outfit", "lowlands_clothing:gamekeeper_attires_leggings", "item.lowlands_clothing.gamekeeper_attires_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:gamemaster_outfit", "lowlands_clothing:gamekeeper_attires_boots", "item.lowlands_clothing.gamekeeper_attires_boots")
        )));

        add(new Kit("gate_sentry", "Gate Sentry", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:gatekeeperarmorb", "lowlands_clothing:gatesentry_armor_helmet", "item.lowlands_clothing.gatesentry_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:gatekeeperarmorb", "lowlands_clothing:gatesentry_armor_chestplate", "item.lowlands_clothing.gatesentry_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:gatekeeperarmorb", "lowlands_clothing:gatesentry_armor_leggings", "item.lowlands_clothing.gatesentry_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:gatekeeperarmorb", "lowlands_clothing:gatesentry_armor_boots", "item.lowlands_clothing.gatesentry_armor_boots")
        )));

        add(new Kit("guard_s_captain", "Guard's Captain", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:guard_captain_uniform_r", "lowlands_clothing:guard_captain_armor_helmet", "item.lowlands_clothing.guard_captain_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:guard_captain_uniform_r", "lowlands_clothing:guard_captain_armor_chestplate", "item.lowlands_clothing.guard_captain_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:guard_captain_uniform_r", "lowlands_clothing:guard_captain_armor_leggings", "item.lowlands_clothing.guard_captain_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:guard_captain_uniform_r", "lowlands_clothing:guard_captain_armor_boots", "item.lowlands_clothing.guard_captain_armor_boots")
        )));

        add(new Kit("highlander", "Highlander", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:highlands_suitb", "lowlands_clothing:highlandslongkilt_helmet", "item.lowlands_clothing.highlandslongkilt_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:highlands_suitb", "lowlands_clothing:highlandslongkilt_chestplate", "item.lowlands_clothing.highlandslongkilt_chestplate"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:highlands_suitb", "lowlands_clothing:highlandslongkilt_boots", "item.lowlands_clothing.highlandslongkilt_boots")
        )));

        add(new Kit("masquerade", "Masquerade", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:maskerade_armor", "lowlands_clothing:maskerade_armor_helmet", "item.lowlands_clothing.maskerade_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:maskerade_armor", "lowlands_clothing:maskerade_armor_chestplate", "item.lowlands_clothing.maskerade_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:maskerade_armor", "lowlands_clothing:maskerade_armor_leggings", "item.lowlands_clothing.maskerade_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:maskerade_armor", "lowlands_clothing:maskerade_armor_boots", "item.lowlands_clothing.maskerade_armor_boots")
        )));

        add(new Kit("mercenary_swordsman", "Mercenary Swordsman", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:mercenary_swordman", "lowlands_clothing:mercenary_swordman_helmet", "item.lowlands_clothing.mercenary_swordman_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:mercenary_swordman", "lowlands_clothing:mercenary_swordman_chestplate", "item.lowlands_clothing.mercenary_swordman_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:mercenary_swordman", "lowlands_clothing:mercenary_swordman_leggings", "item.lowlands_clothing.mercenary_swordman_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:mercenary_swordman", "lowlands_clothing:mercenary_swordman_boots", "item.lowlands_clothing.mercenary_swordman_boots")
        )));

        add(new Kit("mountainmen", "Mountainmen", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:mountainmen__clothes", "lowlands_clothing:mountainmenclothes_helmet", "item.lowlands_clothing.mountainmenclothes_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:mountainmen__clothes", "lowlands_clothing:mountainmenclothes_chestplate", "item.lowlands_clothing.mountainmenclothes_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:mountainmen__clothes", "lowlands_clothing:mountainmenclothes_leggings", "item.lowlands_clothing.mountainmenclothes_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:mountainmen__clothes", "lowlands_clothing:mountainmenclothes_boots", "item.lowlands_clothing.mountainmenclothes_boots")
        )));

        add(new Kit("netherborn_pirate", "Netherborn Pirate", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:netherborn_pirate", "lowlands_clothing:netherborn_pirate_helmet", "item.lowlands_clothing.netherborn_pirate_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:netherborn_pirate", "lowlands_clothing:netherborn_pirate_chestplate", "item.lowlands_clothing.netherborn_pirate_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:netherborn_pirate", "lowlands_clothing:netherborn_pirate_leggings", "item.lowlands_clothing.netherborn_pirate_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:netherborn_pirate", "lowlands_clothing:netherborn_pirate_boots", "item.lowlands_clothing.netherborn_pirate_boots")
        )));

        add(new Kit("norsian_knight", "Norsian Knight", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:norse_ravager_armor", "lowlands_clothing:norsian_armor_helmet", "item.lowlands_clothing.norsian_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:norse_ravager_armor", "lowlands_clothing:norsian_armor_chestplate", "item.lowlands_clothing.norsian_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:norse_ravager_armor", "lowlands_clothing:norsian_armor_leggings", "item.lowlands_clothing.norsian_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:norse_ravager_armor", "lowlands_clothing:norsian_armor_boots", "item.lowlands_clothing.norsian_armor_boots")
        )));

        add(new Kit("penitent", "Penitent", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:penitant", "lowlands_clothing:penitent_rags_helmet", "item.lowlands_clothing.penitent_rags_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:penitant", "lowlands_clothing:penitent_rags_chestplate", "item.lowlands_clothing.penitent_rags_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:penitant", "lowlands_clothing:penitent_rags_leggings", "item.lowlands_clothing.penitent_rags_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:penitant", "lowlands_clothing:penitent_rags_boots", "item.lowlands_clothing.penitent_rags_boots")
        )));

        add(new Kit("plague_doctor", "Plague Doctor", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:plaguedoctor", "lowlands_clothing:plague_doctor_suit_helmet", "item.lowlands_clothing.plague_doctor_suit_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:plaguedoctor", "lowlands_clothing:plague_doctor_suit_chestplate", "item.lowlands_clothing.plague_doctor_suit_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:plaguedoctor", "lowlands_clothing:plague_doctor_suit_leggings", "item.lowlands_clothing.plague_doctor_suit_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:plaguedoctor", "lowlands_clothing:plague_doctor_suit_boots", "item.lowlands_clothing.plague_doctor_suit_boots")
        )));

        add(new Kit("ratcatcher", "Ratcatcher", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:ratcatcherc01", "lowlands_clothing:ratcatcherrobes_helmet", "item.lowlands_clothing.ratcatcherrobes_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:ratcatcherc01", "lowlands_clothing:ratcatcherrobes_chestplate", "item.lowlands_clothing.ratcatcherrobes_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:ratcatcherc01", "lowlands_clothing:ratcatcherrobes_leggings", "item.lowlands_clothing.ratcatcherrobes_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:ratcatcherc01", "lowlands_clothing:ratcatcherrobes_boots", "item.lowlands_clothing.ratcatcherrobes_boots")
        )));

        add(new Kit("siege", "Siege", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:siege_armor", "lowlands_clothing:siege_armor_helmet", "item.lowlands_clothing.siege_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:siege_armor", "lowlands_clothing:siege_armor_chestplate", "item.lowlands_clothing.siege_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:siege_armor", "lowlands_clothing:siege_armor_leggings", "item.lowlands_clothing.siege_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:siege_armor", "lowlands_clothing:siege_armor_boots", "item.lowlands_clothing.siege_armor_boots")
        )));

        add(new Kit("snow_tiger", "Snow Tiger", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:snowtigerarmor", "lowlands_clothing:snowtigerarmor_helmet", "item.lowlands_clothing.snowtigerarmor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:snowtigerarmor", "lowlands_clothing:snowtigerarmor_chestplate", "item.lowlands_clothing.snowtigerarmor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:snowtigerarmor", "lowlands_clothing:snowtigerarmor_leggings", "item.lowlands_clothing.snowtigerarmor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:snowtigerarmor", "lowlands_clothing:snowtigerarmor_boots", "item.lowlands_clothing.snowtigerarmor_boots")
        )));

        add(new Kit("swamplandfolks", "Swamplandfolks", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:swampfolk_outfit", "lowlands_clothing:swampland_folks_attires_helmet", "item.lowlands_clothing.swampland_folks_attires_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:swampfolk_outfit", "lowlands_clothing:swampland_folks_attires_chestplate", "item.lowlands_clothing.swampland_folks_attires_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:swampfolk_outfit", "lowlands_clothing:swampland_folks_attires_leggings", "item.lowlands_clothing.swampland_folks_attires_leggings")
        )));

        add(new Kit("wald_knight", "Wald Knight", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:waldknightarmor", "lowlands_clothing:wald_knight_armor_helmet", "item.lowlands_clothing.wald_knight_armor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:waldknightarmor", "lowlands_clothing:wald_knight_armor_chestplate", "item.lowlands_clothing.wald_knight_armor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:waldknightarmor", "lowlands_clothing:wald_knight_armor_leggings", "item.lowlands_clothing.wald_knight_armor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:waldknightarmor", "lowlands_clothing:wald_knight_armor_boots", "item.lowlands_clothing.wald_knight_armor_boots")
        )));

        add(new Kit("winged_cavalery", "Winged Cavalery", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "lowlands_clothing:wingedcavaleryarmor", "lowlands_clothing:wingedcavaleryarmor_helmet", "item.lowlands_clothing.wingedcavaleryarmor_helmet"),
            new Piece(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "lowlands_clothing:wingedcavaleryarmor", "lowlands_clothing:wingedcavaleryarmor_chestplate", "item.lowlands_clothing.wingedcavaleryarmor_chestplate"),
            new Piece(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "lowlands_clothing:wingedcavaleryarmor", "lowlands_clothing:wingedcavaleryarmor_leggings", "item.lowlands_clothing.wingedcavaleryarmor_leggings"),
            new Piece(EquipmentSlot.FEET, "minecraft:leather_boots",
                "lowlands_clothing:wingedcavaleryarmor", "lowlands_clothing:wingedcavaleryarmor_boots", "item.lowlands_clothing.wingedcavaleryarmor_boots")
        )));
    }

    private LowlandsVanity() {}
}
