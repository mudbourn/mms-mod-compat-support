package info.mudbourn.mmscompat.client.lowlands;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The table of ported Clothing of the Lowlands sets, keyed by equipment asset id.
 *
 * <p>One row per set. The key is the same {@code equippable.asset_id} that
 * {@code LowlandsVanity} already stamps onto the vanity stacks, so the server-side
 * kit table and this client-side model table stay joined by a value both already
 * carry — no new registry, no item registration, and a client without this mod
 * degrades to the (wrong-looking, but harmless) vanilla equipment path rather than
 * desyncing.
 *
 * <p>Models are baked lazily on first use and cached. Baking touches
 * {@code Minecraft.getInstance()}, so nothing here may run off the render thread.
 *
 * <p><b>Status: 1 of 23 sets ported.</b> The remaining 22 are mechanical
 * transcriptions of the same shape as {@link ModelMercenarySwordsman}; the rows
 * are commented out below in the order they should be worked, cheapest first.
 */
public final class LowlandsArmorSets {

    /** A ported set: where its geometry lives and how to build it. */
    private record Set(ModelLayerLocation layer,
                       Supplier<LayerDefinition> definition,
                       Function<ModelPart, LowlandsArmorModel> factory) {}

    private static final Map<Identifier, Set> SETS = new LinkedHashMap<>();

    /** Baked models, one per set. Populated on first render of that set. */
    private static final Map<Identifier, LowlandsArmorModel> BAKED = new LinkedHashMap<>();

    private static void add(String assetId,
                            Supplier<LayerDefinition> definition,
                            Function<ModelPart, LowlandsArmorModel> factory) {
        Identifier id = Identifier.parse(assetId);
        SETS.put(id, new Set(new ModelLayerLocation(id, "main"), definition, factory));
    }

    static {
        add("lowlands_clothing:mercenary_swordman",
            ModelMercenarySwordsman::createBodyLayer, ModelMercenarySwordsman::new);

        // ── Remaining 22, cheapest first (part count / box count / atlas size) ──
        // penitant                    6 / 8  / 64x64
        // axolotl_knight              8 / 14 / 80x80
        // mountainmen__clothes        8 / 11 / 80x80
        // snowtigerarmor              8 / 11 / 80x80   (source: Modelsnowtigerarmorv01)
        // bret_fight_armor            9 / 13 / 80x80
        // gatekeeperarmorb            9 / 11 / 80x80   (source: ModelGatekeeper_corrected)
        // plaguedoctor                9 / 13 / 96x96   (source: ModelPlaguedoctor_v01)
        // gamemaster_outfit          10 / 10 / 80x80
        // guard_captain_uniform_r    11 / 13 / 96x96
        // norse_ravager_armor        11 / 17 / 80x80   (source: ModelNorsian_armor_corected)
        // highlands_suitb            12 / 14 / 80x80   (no leggings piece)
        // maskerade_armor            12 / 19 / 96x96   (source: Modelmaskerade_armor_u)
        // swampfolk_outfit           13 / 13 / 80x80   (no boots piece)
        // bret_corsair_o__armor_v2   14 / 15 / 96x96
        // executioner_armor          14 / 16 / 64x64   (source: Modelexecutionerclothes)
        // wingedcavaleryarmor        14 / 17 / 80x80
        // ratcatcherc01              16 / 18 / 80x80
        // waldknightarmor            16 / 19 / 80x80
        // furnace_master_armor       16 / 28 / 96x96   (source: Modelfurnace_master_armor_v01)
        // siege_armor                17 / 20 / 96x96   (source: ModelSiegeArmorv4)
        // scaphander                 20 / 26 / 96x96   (source: ModelDepth_scaphander)
        // netherborn_pirate          21 / 22 / 144x144 (source: ModelHellborn_Pirate)
        //
        // The source jar keeps dead variants alongside the live model for several
        // sets — Modelsnowtigerarmor vs …v01, ModelSiegeArmor/v2/v3/v4, and so on.
        // The "source:" note above is the one the item's client extension actually
        // instantiates; the others are abandoned drafts and must not be used.
    }

    /** Registers every ported set's model layer. Call once, from client init. */
    public static void registerModelLayers() {
        for (Set set : SETS.values()) {
            EntityModelLayerRegistry.registerModelLayer(set.layer(), set.definition()::get);
        }
    }

    /** True if {@code assetId} is a set this mod supplies geometry for. */
    public static boolean isPorted(Identifier assetId) {
        return SETS.containsKey(assetId);
    }

    /**
     * The baked model for {@code assetId}, or null if that set is not ported.
     *
     * <p>The returned instance is shared and mutated per render — callers must set
     * slot visibility and copy transforms immediately before submitting it.
     */
    public static LowlandsArmorModel model(Identifier assetId) {
        Set set = SETS.get(assetId);
        if (set == null) {
            return null;
        }
        return BAKED.computeIfAbsent(assetId, id -> {
            ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(set.layer());
            return set.factory().apply(root);
        });
    }

    /** Drops baked models so a resource reload rebuilds them. */
    public static void invalidate() {
        BAKED.clear();
    }

    private LowlandsArmorSets() {}
}
