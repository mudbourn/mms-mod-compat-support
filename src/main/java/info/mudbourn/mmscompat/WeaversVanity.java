package info.mudbourn.mmscompat;

import static info.mudbourn.mmscompat.VanityKits.add;
import info.mudbourn.mmscompat.VanityKits.Kit;
import info.mudbourn.mmscompat.VanityKits.Piece;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Weavers Paradise cosplay sets, ported forward to 1.21.11 as vanity skins.
 *
 * Transcribed from Weavers Paradise 1.5.4 (MIT). Each piece is a vanilla leather base
 * carrying equippable.asset_id + item_model, so nothing is registered and the look
 * comes entirely from the "lowlands-vanity" resource pack, which holds:
 *
 *   assets/weaversparadise/equipment/&lt;asset&gt;.json           worn layers
 *   assets/weaversparadise/textures/entity/equipment/humanoid/ the one atlas per set
 *   assets/weaversparadise/items/&lt;item&gt;.json                inventory sprite
 *   assets/weaversparadise/lang/en_us.json                   display names
 *
 * Unlike the Lowlands sets there is no humanoid_leggings layer: each set is authored
 * as a single whole-body atlas. See {@code LowlandsArmorModel.TextureLayout.SINGLE}.
 *
 * The source is a Curios mod, so upstream these are accessory-slot items rather than
 * armour. Nothing of Curios is needed here — the geometry was re-hosted on the four
 * vanilla armour slots, which is what makes the resource-pack-skin approach work at
 * all. The accessory tier that has no armour-slot equivalent (chokers, rings, bells,
 * hand warmers, thigh highs, plushies) is deliberately not ported.
 *
 * Mikkela is deliberately incomplete: the source has no boots for it, in the model
 * and in its own lang file alike.
 *
 * Piece names vary per set — a head piece may be a wig, a hat or a helmet, and legs
 * may be a skirt or leggings — so the item suffixes are spelled out per row rather
 * than derived. They have to match the pack's item ids exactly or the piece falls
 * back to looking like leather.
 */
final class WeaversVanity {

    /** Sentinel for a slot the source set has no piece for. */
    private static final String NONE = null;

    static void register() {
        set("astolfo",  "Astolfo",  "astolfo_armor",     "wig",    "chestplate", "skirt",    "boots");
        set("bridget",  "Bridget",  "bridget_clothing",  "hat",    "jacket",     "skirt",    "boots");
        set("felix",    "Felix",    "felix_clothing",    "hat",    "jacket",     "skirt",    "boots");
        set("gabriel",  "Gabriel",  "gabriel_armor",     "helmet", "chestplate", "leggings", "boots");
        set("giselle",  "Giselle",  "giselle_armor",     "wig",    "chestplate", "leggings", "boots");
        set("griffith", "Griffith", "griffith_armor",    "wig",    "chestplate", "leggings", "boots");
        set("niko",     "Niko",     "niko_armor",        "hat",    "chestplate", "leggings", "boots");
        set("mikkela",  "Mikkela",  "mikkela_armor",     "wig",    "chestplate", "leggings", NONE);
    }

    /**
     * One set. {@code head}/{@code chest}/{@code legs}/{@code feet} are the item-id
     * suffixes after {@code <prefix>_armor_}; a null suffix omits that piece.
     *
     * <p>The item prefix is not the asset name: Bridget and Felix wear
     * {@code bridget_clothing}/{@code felix_clothing} but their items are
     * {@code bridget_armor_*}/{@code felix_armor_*}.
     */
    private static void set(String id, String display, String asset,
                            String head, String chest, String legs, String feet) {
        List<Piece> pieces = new ArrayList<>(4);
        piece(pieces, EquipmentSlot.HEAD,  "minecraft:leather_helmet",     asset, id, head);
        piece(pieces, EquipmentSlot.CHEST, "minecraft:leather_chestplate", asset, id, chest);
        piece(pieces, EquipmentSlot.LEGS,  "minecraft:leather_leggings",   asset, id, legs);
        piece(pieces, EquipmentSlot.FEET,  "minecraft:leather_boots",      asset, id, feet);
        add(new Kit(id, display, List.copyOf(pieces)));
    }

    private static void piece(List<Piece> into, EquipmentSlot slot, String base,
                              String asset, String prefix, String suffix) {
        if (suffix == null) {
            return;
        }
        String item = prefix + "_armor_" + suffix;
        into.add(new Piece(slot, base,
            "weaversparadise:" + asset,
            "weaversparadise:" + item,
            "item.weaversparadise." + item));
    }

    private WeaversVanity() {}
}
