package info.mudbourn.mmscompat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Curated vanity armour kits for /vanity kit &lt;name&gt;.
 *
 * KITS is a hand-maintained table — add a row per set, no logic changes needed.
 *
 * A kit is a list of {@link Piece}s, each of which is either:
 *   - a plain item ("give a netherite helmet"), or
 *   - a "skin": a vanilla base item carrying equippable.asset_id + item_model
 *     components, so a resource pack supplies the look with no item registration
 *     and no client-side mod. This is how the Clothing of the Lowlands sets are
 *     ported forward — see LowlandsVanity.
 *
 * Items that aren't present in the current modset are skipped rather than
 * failing the whole kit, so rows for mods that come and go stay harmless.
 */
public final class VanityKits {

    /**
     * One armour piece.
     *
     * @param slot      which armour slot this occupies
     * @param itemId    the item to hand out (the base item, for skins)
     * @param assetId   equipment asset for the worn layers, or null to keep the base item's
     * @param itemModel item model for the inventory/hand sprite, or null to keep the base item's
     * @param nameKey   translation key for the custom name, or null to keep the base item's
     * @param dyedColor RGB to dye the base item, or null to strip any dye instead
     */
    public record Piece(EquipmentSlot slot, String itemId, String assetId, String itemModel,
                        String nameKey, Integer dyedColor) {

        /**
         * The undyed form, which is every skin but the invisible set. Kept as a
         * constructor rather than folded into the canonical one so the Lowlands table
         * — 23 sets of four — did not have to grow a trailing null per row.
         */
        public Piece(EquipmentSlot slot, String itemId, String assetId, String itemModel, String nameKey) {
            this(slot, itemId, assetId, itemModel, nameKey, null);
        }

        /** A plain item, as-is. */
        public static Piece of(EquipmentSlot slot, String itemId) {
            return new Piece(slot, itemId, null, null, null, null);
        }
    }

    public record Kit(String id, String display, List<Piece> pieces) {}

    public static final Map<String, Kit> KITS = new LinkedHashMap<>();

    public static void add(Kit kit) {
        KITS.put(kit.id(), kit);
    }

    /**
     * Convenience for a full four-piece set of plain items sharing an id prefix.
     *
     * <p>Works for any mod that follows vanilla's {@code _helmet}/{@code _chestplate}/
     * {@code _leggings}/{@code _boots} naming, which all of the sets below do. A set
     * that names a piece differently, or is missing one, gets a literal row instead —
     * a prefix that resolves to nothing would silently produce an empty kit.
     */
    private static void addSet(String id, String display, String prefix) {
        add(new Kit(id, display, List.of(
            Piece.of(EquipmentSlot.HEAD,  prefix + "_helmet"),
            Piece.of(EquipmentSlot.CHEST, prefix + "_chestplate"),
            Piece.of(EquipmentSlot.LEGS,  prefix + "_leggings"),
            Piece.of(EquipmentSlot.FEET,  prefix + "_boots")
        )));
    }

    static {
        // ── Vanilla ──────────────────────────────────────────────────────────
        addSet("leather",   "Leather",   "minecraft:leather");
        addSet("chainmail", "Chainmail", "minecraft:chainmail");
        addSet("copper",    "Copper",    "minecraft:copper");
        addSet("iron",      "Iron",      "minecraft:iron");
        addSet("golden",    "Gold",      "minecraft:golden");
        addSet("diamond",   "Diamond",   "minecraft:diamond");
        addSet("netherite", "Netherite", "minecraft:netherite");
        // The turtle helmet is not listed here: it is the head slot of the
        // turtle_armor kit below, which supersedes the shell-only kit this used to be.

        // ── Clothing of the Lowlands (resource-pack skins) ───────────────────
        LowlandsVanity.register();

        // ── Curated cross-mod kits ───────────────────────────────────────────
        // Themed sets whose pieces come from several mods, or from items that are
        // not obviously armour at all. These are the ones a player would never
        // find by guessing an item name.

        // Bountiful Fish's four wearables. The first three are head equipment; the
        // necklace is a Trinkets item (chest/necklace slot) and so does not go in an
        // EquipmentSlot at all. That costs nothing here — give() only puts stacks in
        // the inventory, and Piece.slot is unused for a plain item — but it is why
        // the kit hands out three "head" pieces without conflicting.
        add(new Kit("seafaring_trinkets", "Seafaring Trinkets", List.of(
            Piece.of(EquipmentSlot.HEAD,  "bountiful-fish:rusted_crown"),
            Piece.of(EquipmentSlot.HEAD,  "bountiful-fish:mongo_mask"),
            Piece.of(EquipmentSlot.HEAD,  "bountiful-fish:old_ribbon"),
            Piece.of(EquipmentSlot.CHEST, "bountiful-fish:pearl_necklace")
        )));

        // What Invisible Armor Recipe's crafting_transmute produces: the base item
        // kept, its equippable repointed at minecraft:structure_void — an asset with
        // no layers, so the worn armour draws nothing. White dye survives the
        // transmute and is what the inventory sprite shows, since the item model is
        // still leather.
        add(new Kit("invisible", "Invisible Armor", List.of(
            invisible(EquipmentSlot.HEAD,  "minecraft:leather_helmet",     "helmet"),
            invisible(EquipmentSlot.CHEST, "minecraft:leather_chestplate", "chestplate"),
            invisible(EquipmentSlot.LEGS,  "minecraft:leather_leggings",   "leggings"),
            invisible(EquipmentSlot.FEET,  "minecraft:leather_boots",      "boots")
        )));

        add(new Kit("end_treasures", "End Treasures", List.of(
            Piece.of(EquipmentSlot.CHEST, "minecraft:elytra"),
            Piece.of(EquipmentSlot.LEGS,  "enderscape:drift_leggings")
        )));

        add(new Kit("ice_skates", "Ice Skates", List.of(
            Piece.of(EquipmentSlot.FEET, "frostiful:ice_skates"),
            Piece.of(EquipmentSlot.FEET, "frostiful:armored_ice_skates")
        )));

        // Scorchful names its three pieces Carapace/Knee Pads/Flippers; the helmet
        // slot is vanilla's turtle shell, which is why this set spans two sources.
        // This replaced a shell-only "turtle" kit — the helmet alone was a strict
        // subset of this, so keeping both meant two kits and one of them a trap.
        add(new Kit("turtle_armor", "Turtle Armor", List.of(
            Piece.of(EquipmentSlot.HEAD,  "minecraft:turtle_helmet"),
            Piece.of(EquipmentSlot.CHEST, "scorchful:turtle_chestplate"),
            Piece.of(EquipmentSlot.LEGS,  "scorchful:turtle_leggings"),
            Piece.of(EquipmentSlot.FEET,  "scorchful:turtle_boots")
        )));

        add(new Kit("nether_treasures", "Nether Treasures", List.of(
            Piece.of(EquipmentSlot.HEAD, "friendsandfoes:wildfire_crown")
        )));

        // ── Modded armour sets ───────────────────────────────────────────────
        // Ordinary four-piece sets that were already in the game but had no kit.

        // Aerial Hell
        addSet("ruby",         "Ruby",             "aerialhell:ruby");
        addSet("azurite",      "Azurite Crystal",  "aerialhell:azurite");
        addSet("magmatic_gel", "Magmatic Gel",     "aerialhell:magmatic_gel");
        addSet("volucite",     "Volucite",         "aerialhell:volucite");
        addSet("obsidian",     "Obsidian",         "aerialhell:obsidian");
        addSet("lunatic",      "Lunar",            "aerialhell:lunatic");
        addSet("arsonist",     "Arsonist",         "aerialhell:arsonist");
        addSet("shadow",       "Shadow",           "aerialhell:shadow");
        add(new Kit("blue_meanie", "Blue Meanie",
            List.of(Piece.of(EquipmentSlot.HEAD, "aerialhell:blue_meanie_cap"))));

        // Enderscape
        addSet("shadoline", "Shadoline", "enderscape:shadoline");

        // Expanded Weaponry
        addSet("heavy_copper",    "Heavy Copper",    "expanded_weaponry:heavy_copper");
        addSet("heavy_iron",      "Heavy Iron",      "expanded_weaponry:heavy_iron");
        addSet("heavy_golden",    "Heavy Gold",      "expanded_weaponry:heavy_golden");
        addSet("heavy_diamond",   "Heavy Diamond",   "expanded_weaponry:heavy_diamond");
        addSet("heavy_netherite", "Heavy Netherite", "expanded_weaponry:heavy_netherite");

        // Frostiful
        addSet("fur",                   "Fur",                   "frostiful:fur");
        addSet("fur_padded_chainmail",  "Fur Padded Chainmail",  "frostiful:fur_padded_chainmail");
        add(new Kit("frostology", "Cloak of Frostology",
            List.of(Piece.of(EquipmentSlot.CHEST, "frostiful:frostology_cloak"))));

        // Mutant Monsters ships no helmet for this set, so it is three pieces.
        add(new Kit("mutant_skeleton", "Mutant Skeleton", List.of(
            Piece.of(EquipmentSlot.CHEST, "mutantmonsters:mutant_skeleton_chestplate"),
            Piece.of(EquipmentSlot.LEGS,  "mutantmonsters:mutant_skeleton_leggings"),
            Piece.of(EquipmentSlot.FEET,  "mutantmonsters:mutant_skeleton_boots")
        )));
    }

    /** White dyed leather repointed at the no-layer asset the transmute recipe uses. */
    private static Piece invisible(EquipmentSlot slot, String itemId, String name) {
        return new Piece(slot, itemId, "minecraft:structure_void", null,
            "item.mms_compat.invisible_" + name, 0xFFFFFF);
    }

    /**
     * Gives every resolvable piece of {@code kit} to {@code player}, vanity-applied.
     * Returns the number of pieces actually handed out.
     */
    public static int give(ServerPlayer player, Kit kit) {
        int given = 0;
        for (Piece piece : kit.pieces()) {
            Optional<Item> item = resolve(piece.itemId());
            if (item.isEmpty()) continue;

            ItemStack stack = new ItemStack(item.get());
            applySkin(stack, piece);
            VanityUtil.applyVanity(stack);

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            given++;
        }
        return given;
    }

    /** Repoints a base item's worn layers, inventory sprite and name at pack-supplied assets. */
    private static void applySkin(ItemStack stack, Piece piece) {
        if (piece.assetId() != null) {
            Identifier assetId = Identifier.parse(piece.assetId());
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, assetId);
            stack.set(DataComponents.EQUIPPABLE, Equippable.builder(piece.slot())
                .setAsset(asset)
                .setDamageOnHurt(false)
                .build());
        }
        if (piece.itemModel() != null) {
            stack.set(DataComponents.ITEM_MODEL, Identifier.parse(piece.itemModel()));
        }
        if (piece.nameKey() != null) {
            stack.set(DataComponents.ITEM_NAME, Component.translatable(piece.nameKey()));
        }
        if (piece.dyedColor() != null) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(piece.dyedColor()));
        } else {
            // Leather bases carry a default dye tint. Our equipment assets and item models
            // have no tint layer so it would not show, but drop it so the stack is clean.
            stack.remove(DataComponents.DYED_COLOR);
        }
    }

    private static Optional<Item> resolve(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) return Optional.empty();
        return BuiltInRegistries.ITEM.getOptional(key);
    }

    private VanityKits() {}
}
