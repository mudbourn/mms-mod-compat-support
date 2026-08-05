package info.mudbourn.mmscompat.client.lowlands;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

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
 * <p><b>Status: all 23 sets ported, none verified in game.</b> Highlander has no
 * leggings piece and Swamplandfolks no boots — that is the source mod's own gap,
 * matching {@code LowlandsVanity}, not a missing row here.
 */
public final class LowlandsArmorSets {

    /** A ported set: where its geometry lives and how to build it. */
    private record Set(ModelLayerLocation layer,
                       Supplier<LayerDefinition> definition,
                       Function<ModelPart, LowlandsArmorModel> factory) {}

    private static final Map<Identifier, Set> SETS = new LinkedHashMap<>();

    /** Cache key: a set's model for one specific armour slot. */
    private record Baked(Identifier asset, EquipmentSlot slot) {}

    /**
     * Baked models, one per set <em>per slot</em>. Populated on first render.
     *
     * <p>Sharing a single instance across the four slots does not work. Rendering
     * is deferred — {@code submitModel} only collects a node, and the draw happens
     * at end of frame — so four submissions of the same object all render with
     * whatever the last caller left on it. {@code HumanoidArmorLayer} submits in
     * the order CHEST, LEGS, FEET, HEAD, so a shared instance ends up carrying
     * HEAD's part visibility and HEAD's transforms and every piece but the helmet
     * silently vanishes. Vanilla has the same constraint and solves it the same
     * way, with four separate models in {@code ArmorModelSet}.
     */
    private static final Map<Baked, LowlandsArmorModel> BAKED = new LinkedHashMap<>();

    private static void add(String assetId,
                            Supplier<LayerDefinition> definition,
                            Function<ModelPart, LowlandsArmorModel> factory) {
        Identifier id = Identifier.parse(assetId);
        SETS.put(id, new Set(new ModelLayerLocation(id, "main"), definition, factory));
    }

    static {
        // All 23 sets, in the order they appear in LowlandsVanity's kit table.
        //
        // The source jar keeps dead variants alongside the live model for several
        // sets — Modelsnowtigerarmor vs …v01, ModelSiegeArmor/v2/v3/v4, and so on.
        // Each generated class names the source it was transcribed from in its
        // javadoc; that is the one the item's client extension actually
        // instantiates, and the others are abandoned drafts.
        add("lowlands_clothing:axolotl_knight",
            ModelAxolotlKnight::createBodyLayer, ModelAxolotlKnight::new);
        add("lowlands_clothing:bret_fight_armor",
            ModelBret::createBodyLayer, ModelBret::new);
        add("lowlands_clothing:bret_corsair_o__armor_v2",
            ModelBretCorsair::createBodyLayer, ModelBretCorsair::new);
        add("lowlands_clothing:scaphander",
            ModelDepthScaphander::createBodyLayer, ModelDepthScaphander::new);
        add("lowlands_clothing:executioner_armor",
            ModelExecutioner::createBodyLayer, ModelExecutioner::new);
        add("lowlands_clothing:furnace_master_armor",
            ModelFurnaceMaster::createBodyLayer, ModelFurnaceMaster::new);
        add("lowlands_clothing:gamemaster_outfit",
            ModelGamekeeper::createBodyLayer, ModelGamekeeper::new);
        add("lowlands_clothing:gatekeeperarmorb",
            ModelGateSentry::createBodyLayer, ModelGateSentry::new);
        add("lowlands_clothing:guard_captain_uniform_r",
            ModelGuardCaptain::createBodyLayer, ModelGuardCaptain::new);
        add("lowlands_clothing:highlands_suitb",
            ModelHighlander::createBodyLayer, ModelHighlander::new);
        add("lowlands_clothing:maskerade_armor",
            ModelMasquerade::createBodyLayer, ModelMasquerade::new);
        add("lowlands_clothing:mercenary_swordman",
            ModelMercenarySwordsman::createBodyLayer, ModelMercenarySwordsman::new);
        add("lowlands_clothing:mountainmen__clothes",
            ModelMountainmen::createBodyLayer, ModelMountainmen::new);
        add("lowlands_clothing:netherborn_pirate",
            ModelNetherbornPirate::createBodyLayer, ModelNetherbornPirate::new);
        add("lowlands_clothing:norse_ravager_armor",
            ModelNorsianKnight::createBodyLayer, ModelNorsianKnight::new);
        add("lowlands_clothing:penitant",
            ModelPenitent::createBodyLayer, ModelPenitent::new);
        add("lowlands_clothing:plaguedoctor",
            ModelPlagueDoctor::createBodyLayer, ModelPlagueDoctor::new);
        add("lowlands_clothing:ratcatcherc01",
            ModelRatcatcher::createBodyLayer, ModelRatcatcher::new);
        add("lowlands_clothing:siege_armor",
            ModelSiege::createBodyLayer, ModelSiege::new);
        add("lowlands_clothing:snowtigerarmor",
            ModelSnowTiger::createBodyLayer, ModelSnowTiger::new);
        add("lowlands_clothing:swampfolk_outfit",
            ModelSwamplandfolks::createBodyLayer, ModelSwamplandfolks::new);
        add("lowlands_clothing:waldknightarmor",
            ModelWaldKnight::createBodyLayer, ModelWaldKnight::new);
        add("lowlands_clothing:wingedcavaleryarmor",
            ModelWingedCavalery::createBodyLayer, ModelWingedCavalery::new);
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
     * The baked model for {@code assetId} in {@code slot}, or null if that set is
     * not ported.
     *
     * <p>Part visibility is fixed at bake time and never touched again, so the
     * instance stays valid for a deferred draw. Callers still copy transforms onto
     * it each frame, which is safe because no other slot shares it.
     */
    public static LowlandsArmorModel model(Identifier assetId, EquipmentSlot slot) {
        Set set = SETS.get(assetId);
        if (set == null) {
            return null;
        }
        return BAKED.computeIfAbsent(new Baked(assetId, slot), key -> {
            ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(set.layer());
            LowlandsArmorModel model = set.factory().apply(root);
            model.selectSlot(key.slot());
            return model;
        });
    }

    /** Drops baked models so a resource reload rebuilds them. */
    public static void invalidate() {
        BAKED.clear();
        // Pooled poses are keyed on the baked models and hold their part references,
        // so they have to go with them or they would replay onto a dead part tree.
        LowlandsArmorPose.invalidate();
    }

    private LowlandsArmorSets() {}
}
