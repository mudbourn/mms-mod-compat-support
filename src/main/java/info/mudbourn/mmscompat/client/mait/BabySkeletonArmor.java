package info.mudbourn.mmscompat.client.mait;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

import java.util.List;

// Baby armor layers shaped like the skeleton's own limbs, scaled by Mob AI Tweaks' SkeletonModel hook.
public final class BabySkeletonArmor extends HumanoidModel<HumanoidRenderState> {

    private static final String NAMESPACE = "mms_compat";
    private static final CubeDeformation OUTER = new CubeDeformation(1.0F);
    private static final CubeDeformation INNER = new CubeDeformation(0.5F);
    private static final float WITHER_SCALE = 1.2F;

    public record Kind(String name, ArmorModelSet<ModelLayerLocation> adultArmor, MeshTransformer scale) {

        public ArmorModelSet<ModelLayerLocation> babyArmor() {
            Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name + "_baby");
            return new ArmorModelSet<>(
                new ModelLayerLocation(id, "helmet"),
                new ModelLayerLocation(id, "chestplate"),
                new ModelLayerLocation(id, "leggings"),
                new ModelLayerLocation(id, "boots")
            );
        }

        public ArmorModelSet<SkeletonModel<SkeletonRenderState>> bakeArmor(EntityModelSet models) {
            return ArmorModelSet.bake(babyArmor(), models, SkeletonModel<SkeletonRenderState>::new);
        }
    }

    private static final List<Kind> KINDS = List.of(
        new Kind("skeleton", ModelLayers.SKELETON_ARMOR, MeshTransformer.IDENTITY),
        new Kind("stray", ModelLayers.STRAY_ARMOR, MeshTransformer.IDENTITY),
        new Kind("bogged", ModelLayers.BOGGED_ARMOR, MeshTransformer.IDENTITY),
        new Kind("parched", ModelLayers.PARCHED_ARMOR, MeshTransformer.IDENTITY),
        new Kind("wither_skeleton", ModelLayers.WITHER_SKELETON_ARMOR, MeshTransformer.scaling(WITHER_SCALE))
    );

    private BabySkeletonArmor(ModelPart root) {
        super(root);
    }

    public static void register() {
        ArmorModelSet<LayerDefinition> armor = createArmorMeshSet(
            BabySkeletonArmor::skeletonMesh,
            ADULT_ARMOR_PARTS_PER_SLOT,
            INNER,
            OUTER
        ).map(mesh -> LayerDefinition.create(mesh, 64, 32));
        for (Kind kind : KINDS) {
            ArmorModelSet<LayerDefinition> scaled = armor.map(layer -> layer.apply(kind.scale()));
            ModelLayerRegistry.registerArmorModelLayers(kind.babyArmor(), () -> scaled);
        }
    }

    public static Kind forArmor(ArmorModelSet<ModelLayerLocation> adultArmor) {
        for (Kind kind : KINDS) {
            if (kind.adultArmor().head().equals(adultArmor.head())) {
                return kind;
            }
        }
        return null;
    }

    private static MeshDefinition skeletonMesh(CubeDeformation deformation) {
        MeshDefinition mesh = createMesh(deformation, 0.0F);
        mesh.getRoot().addOrReplaceChild(
            "right_arm",
            CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation),
            PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        mesh.getRoot().addOrReplaceChild(
            "left_arm",
            CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation),
            PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        mesh.getRoot().addOrReplaceChild(
            "right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation),
            PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        mesh.getRoot().addOrReplaceChild(
            "left_leg",
            CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F, deformation),
            PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        return mesh;
    }
}
