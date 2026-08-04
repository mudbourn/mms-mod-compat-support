package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Norsian Knight — {@code lowlands_clothing:norse_ravager_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelNorsian_armor_corected}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelNorsianKnight extends LowlandsArmorModel {

    public ModelNorsianKnight(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(32, 32).mirror().addBox(1.0F, -10.0F, -1.0F, 0.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false),
           PartPose.offsetAndRotation(5.0F, -2.0F, 1.0F, 0.0F, 1.5708F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(32, 32).addBox(-1.0F, -10.0F, -1.0F, 0.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-5.0F, -2.0F, 1.0F, 0.0F, -1.5708F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(32, 0)
              .addBox(-4.0F, 0.3556F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(32, 16)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.0F))
              .texOffs(0, 0)
              .addBox(-8.0F, -0.7556F, 3.7111F, 16.0F, 21.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(32, 61).addBox(-9.0F, 1.0F, 1.0F, 18.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -2.7556F, 1.8889F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(0, 38)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(32, 51)
              .addBox(-4.0F, 4.0F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(50, 32).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-2.0333F, -1.0F, 0.0F, 0.0F, 0.0F, -0.0873F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(56, 0)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(0, 70)
              .mirror()
              .addBox(1.0F, 4.0F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F))
              .mirror(false),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(18, 70).mirror().addBox(-2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.4F)).mirror(false),
           PartPose.offsetAndRotation(2.0333F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0873F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(16, 38)
              .addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .texOffs(0, 54)
              .addBox(-2.8222F, 6.0F, -2.3556F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.432F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(60, 73)
              .mirror()
              .addBox(-2.1778F, 6.0F, -2.3556F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.435F))
              .mirror(false)
              .texOffs(16, 38)
              .mirror()
              .addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .mirror(false),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
