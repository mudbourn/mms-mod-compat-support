package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Executioner — {@code lowlands_clothing:executioner_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelexecutionerclothes}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 64x64 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelExecutioner extends LowlandsArmorModel {

    public ModelExecutioner(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(36, 48).addBox(-2.5F, -4.0F, -4.0F, 6.0F, 8.0F, 8.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-0.5F, -6.0F, 4.0F, -0.829F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(54, 0)
              .addBox(-4.9333F, 9.0F, -2.8222F, 5.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(0, 16)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.59F))
              .texOffs(24, 16)
              .addBox(-4.0F, -0.1778F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -2.0F, -1.0F, 5.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.9333F, 2.0F, -2.8222F, 0.0F, 0.6109F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(16, 47).addBox(-1.0F, -2.0F, -1.0F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-5.9333F, 13.0F, 4.3556F, 0.0F, 0.0436F, -1.0908F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(30, 47).addBox(-1.0F, -2.0F, -1.0F, 5.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(2.0667F, 11.0F, -2.0F, 0.0F, 0.0F, -0.3927F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(24, 25).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(38, 41).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-1.9667F, 7.0F, 0.0F, 0.0F, 0.0F, -0.3491F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(16, 41).addBox(-2.5F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(1.9667F, 7.0F, 0.0F, 0.0F, 0.0F, 0.3491F)
        );
        PartDefinition RightLeg = root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        RightLeg.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(16, 36).addBox(-3.0F, -2.0F, -1.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.7111F, 9.0F, -1.2889F, 0.48F, 0.0F, 0.0F)
        );
        PartDefinition LeftLeg = root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(40, 25).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        LeftLeg.addOrReplaceChild(
           "cube_r7",
           CubeListBuilder.create().texOffs(16, 34).addBox(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.7111F, 9.0F, -1.2889F, 0.48F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }
}
