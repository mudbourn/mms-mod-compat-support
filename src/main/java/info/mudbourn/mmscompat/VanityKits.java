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

    /**
     * A four-piece set handed out as leather bases wearing another mod's equipment
     * asset, rather than as that mod's own items.
     *
     * <p>This is the only way a vanity piece can be truly inert. {@link VanityUtil}
     * can strip everything that lives in a component — it replaces
     * {@code ATTRIBUTE_MODIFIERS} wholesale, so armour, toughness, weight and any
     * mod-added attribute go with it — but a great deal of armour behaviour is not a
     * component at all. Mutant Skeleton's quick draw, Drift Leggings' gravity,
     * Frostiful's cold protection and vanilla's own turtle-helmet water breathing are
     * all hardcoded checks of the form "is the worn item this item". Nothing written
     * onto the stack can answer no to that. Handing out a different item can.
     *
     * <p>Safe only because every asset used here draws through the stock
     * {@code humanoid}/{@code humanoid_leggings} layers, which were checked one by
     * one. A set with a custom armour model would render as flat vanilla-shaped slabs
     * this way — the mistake the Lowlands port already made once. Re-check before
     * adding a row.
     *
     * @param namespace the mod supplying both the asset and the item model
     * @param asset     equipment asset name — <em>not</em> always the item prefix;
     *                  Expanded Weaponry's {@code heavy_golden_*} items wear
     *                  {@code heavy_gold}, and Frostiful's {@code fur_padded_chainmail_*}
     *                  wear {@code fur_lined_chainmail}
     * @param prefix    the mod's item id prefix, for the sprite and the name
     */
    private static void addSkinSet(String id, String display, String namespace, String asset, String prefix) {
        add(new Kit(id, display, List.of(
            skin(EquipmentSlot.HEAD,  "minecraft:leather_helmet",     namespace, asset, prefix + "_helmet"),
            skin(EquipmentSlot.CHEST, "minecraft:leather_chestplate", namespace, asset, prefix + "_chestplate"),
            skin(EquipmentSlot.LEGS,  "minecraft:leather_leggings",   namespace, asset, prefix + "_leggings"),
            skin(EquipmentSlot.FEET,  "minecraft:leather_boots",      namespace, asset, prefix + "_boots")
        )));
    }

    /** One leather base wearing {@code namespace:asset}, named and sprited as the real item. */
    private static Piece skin(EquipmentSlot slot, String base, String namespace, String asset, String item) {
        return new Piece(slot, base, namespace + ":" + asset, namespace + ":" + item,
            "item." + namespace + "." + item);
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

        // ── Weavers Paradise (resource-pack skins) ───────────────────────────
        WeaversVanity.register();

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

        // The elytra stays a real elytra and is still fully inert: gliding is the
        // GLIDER marker component, which VanityUtil removes, while the wings are the
        // "wings" layer of its equipment asset, which it keeps. Skinning it onto a
        // leather chestplate would have worked too, but there is nothing to gain by
        // it once the component is gone.
        add(new Kit("end_treasures", "End Treasures", List.of(
            Piece.of(EquipmentSlot.CHEST, "minecraft:elytra"),
            skin(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "enderscape", "drift_leggings", "drift_leggings")
        )));

        add(new Kit("ice_skates", "Ice Skates", List.of(
            Piece.of(EquipmentSlot.FEET, "frostiful:ice_skates"),
            Piece.of(EquipmentSlot.FEET, "frostiful:armored_ice_skates")
        )));

        // Scorchful names its three pieces Carapace/Knee Pads/Flippers; the helmet
        // slot is vanilla's turtle shell, which is why this set spans two sources.
        // This replaced a shell-only "turtle" kit — the helmet alone was a strict
        // subset of this, so keeping both meant two kits and one of them a trap.
        // The head is a leather helmet wearing vanilla's own turtle_scute asset, not a
        // turtle helmet: water breathing is a hardcoded check for the item in the head
        // slot, so only not being that item removes it.
        add(new Kit("turtle_armor", "Turtle Armor", List.of(
            new Piece(EquipmentSlot.HEAD, "minecraft:leather_helmet",
                "minecraft:turtle_scute", "minecraft:turtle_helmet", "item.minecraft.turtle_helmet"),
            skin(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "scorchful", "turtle", "turtle_chestplate"),
            skin(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "scorchful", "turtle", "turtle_leggings"),
            skin(EquipmentSlot.FEET, "minecraft:leather_boots",
                "scorchful", "turtle", "turtle_boots")
        )));

        add(new Kit("nether_treasures", "Nether Treasures", List.of(
            Piece.of(EquipmentSlot.HEAD, "friendsandfoes:wildfire_crown")
        )));

        // ── Modded armour sets ───────────────────────────────────────────────
        // Ordinary four-piece sets that were already in the game but had no kit.

        // Aerial Hell
        addSkinSet("ruby",         "Ruby",            "aerialhell", "ruby",         "ruby");
        addSkinSet("azurite",      "Azurite Crystal", "aerialhell", "azurite",      "azurite");
        addSkinSet("magmatic_gel", "Magmatic Gel",    "aerialhell", "magmatic_gel", "magmatic_gel");
        addSkinSet("volucite",     "Volucite",        "aerialhell", "volucite",     "volucite");
        addSkinSet("obsidian",     "Obsidian",        "aerialhell", "obsidian",     "obsidian");
        addSkinSet("lunatic",      "Lunar",           "aerialhell", "lunatic",      "lunatic");
        addSkinSet("arsonist",     "Arsonist",        "aerialhell", "arsonist",     "arsonist");
        addSkinSet("shadow",       "Shadow",          "aerialhell", "shadow",       "shadow");
        // No blue_meanie kit: the cap is not equippable, which is also why Aerial Hell
        // ships no equipment asset for it. It was briefly listed here as a head piece,
        // which would have handed out an item that cannot be worn.

        // Enderscape
        addSkinSet("shadoline", "Shadoline", "enderscape", "shadoline", "shadoline");

        // Expanded Weaponry. Note heavy_golden_* items wear the heavy_gold asset.
        addSkinSet("heavy_copper",    "Heavy Copper",    "expanded_weaponry", "heavy_copper",    "heavy_copper");
        addSkinSet("heavy_iron",      "Heavy Iron",      "expanded_weaponry", "heavy_iron",      "heavy_iron");
        addSkinSet("heavy_golden",    "Heavy Gold",      "expanded_weaponry", "heavy_gold",      "heavy_golden");
        addSkinSet("heavy_diamond",   "Heavy Diamond",   "expanded_weaponry", "heavy_diamond",   "heavy_diamond");
        addSkinSet("heavy_netherite", "Heavy Netherite", "expanded_weaponry", "heavy_netherite", "heavy_netherite");

        // Frostiful. Note fur_padded_chainmail_* items wear the fur_lined_chainmail asset.
        addSkinSet("fur",                  "Fur",                  "frostiful", "fur",                 "fur");
        addSkinSet("fur_padded_chainmail", "Fur Padded Chainmail", "frostiful", "fur_lined_chainmail", "fur_padded_chainmail");
        add(new Kit("frostology", "Cloak of Frostology",
            List.of(Piece.of(EquipmentSlot.CHEST, "frostiful:frostology_cloak"))));

        // Mutant Skeleton's three worn pieces skin cleanly. The head slot does not:
        // the skull is a *block* (lang key block.mutantmonsters.mutant_skeleton_skull),
        // worn like a vanilla mob head and drawn from its block model rather than an
        // equipment layer, so a leather helmet cannot wear it. It stays the real item
        // and keeps whatever the mod hangs off it.
        add(new Kit("mutant_skeleton", "Mutant Skeleton", List.of(
            Piece.of(EquipmentSlot.HEAD, "mutantmonsters:mutant_skeleton_skull"),
            skin(EquipmentSlot.CHEST, "minecraft:leather_chestplate",
                "mutantmonsters", "mutant_skeleton", "mutant_skeleton_chestplate"),
            skin(EquipmentSlot.LEGS, "minecraft:leather_leggings",
                "mutantmonsters", "mutant_skeleton", "mutant_skeleton_leggings"),
            skin(EquipmentSlot.FEET, "minecraft:leather_boots",
                "mutantmonsters", "mutant_skeleton", "mutant_skeleton_boots")
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
