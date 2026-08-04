package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Bret — {@code lowlands_clothing:bret_fight_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelBret_base_clothes}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelBret extends LowlandsArmorModel {

    public ModelBret(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 17)
              .addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.6F))
              .texOffs(74, 72)
              .addBox(-4.8222F, -5.1111F, 6.9333F, 3.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(24, 69).addBox(-9.0F, 0.0F, -11.0F, 18.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -4.5873F, 10.9889F, -0.1309F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(24, 57).addBox(-9.0F, 0.0F, -6.0F, 18.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -6.8889F, -3.8222F, -0.2182F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(0, 58).addBox(-1.0F, -6.0F, -3.0F, 0.0F, 6.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(6.0F, -6.3556F, 0.0F, -0.1485F, 0.7848F, 0.2402F)
        );
        root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(32, 18)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(0, 34)
              .addBox(-4.0F, 0.3556F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(0, 48).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(50, 0)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(56, 53)
              .addBox(0.0F, -3.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.2F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(24, 33).addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(40, 33)
              .addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F))
              .texOffs(56, 14)
              .addBox(-1.0F, 3.0F, -3.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
