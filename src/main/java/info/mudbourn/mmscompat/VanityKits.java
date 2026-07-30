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
     */
    public record Piece(EquipmentSlot slot, String itemId, String assetId, String itemModel, String nameKey) {

        /** A plain item, as-is. */
        public static Piece of(EquipmentSlot slot, String itemId) {
            return new Piece(slot, itemId, null, null, null);
        }
    }

    public record Kit(String id, String display, List<Piece> pieces) {}

    public static final Map<String, Kit> KITS = new LinkedHashMap<>();

    public static void add(Kit kit) {
        KITS.put(kit.id(), kit);
    }

    /** Convenience for a full four-piece set of plain items sharing an id prefix. */
    private static void addVanillaSet(String id, String display, String prefix) {
        add(new Kit(id, display, List.of(
            Piece.of(EquipmentSlot.HEAD,  prefix + "_helmet"),
            Piece.of(EquipmentSlot.CHEST, prefix + "_chestplate"),
            Piece.of(EquipmentSlot.LEGS,  prefix + "_leggings"),
            Piece.of(EquipmentSlot.FEET,  prefix + "_boots")
        )));
    }

    static {
        // ── Vanilla ──────────────────────────────────────────────────────────
        addVanillaSet("leather",   "Leather",   "minecraft:leather");
        addVanillaSet("chainmail", "Chainmail", "minecraft:chainmail");
        addVanillaSet("copper",    "Copper",    "minecraft:copper");
        addVanillaSet("iron",      "Iron",      "minecraft:iron");
        addVanillaSet("golden",    "Gold",      "minecraft:golden");
        addVanillaSet("diamond",   "Diamond",   "minecraft:diamond");
        addVanillaSet("netherite", "Netherite", "minecraft:netherite");
        add(new Kit("turtle", "Turtle Shell",
            List.of(Piece.of(EquipmentSlot.HEAD, "minecraft:turtle_helmet"))));

        // ── Clothing of the Lowlands (resource-pack skins) ───────────────────
        LowlandsVanity.register();

        // ── Modded (curated) ─────────────────────────────────────────────────
        // TODO: fill from eli3's armour-type audit. One add(...) per set.
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
        // Leather bases carry a default dye tint. Our equipment assets and item models
        // have no tint layer so it would not show, but drop it so the stack is clean.
        stack.remove(DataComponents.DYED_COLOR);
    }

    private static Optional<Item> resolve(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) return Optional.empty();
        return BuiltInRegistries.ITEM.getOptional(key);
    }

    private VanityKits() {}
}
