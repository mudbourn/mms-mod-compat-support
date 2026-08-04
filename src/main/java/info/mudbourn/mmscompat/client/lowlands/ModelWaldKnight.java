package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Wald Knight — {@code lowlands_clothing:waldknightarmor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelWaldknightarmor}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelWaldKnight extends LowlandsArmorModel {

    public ModelWaldKnight(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 26)
              .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.6F))
              .texOffs(40, 16)
              .addBox(-2.0F, -12.5333F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(32, 66)
              .addBox(-6.1778F, -3.5333F, -5.0F, 12.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(40, 20).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(1.0F, -8.5333F, 0.0F, 0.0F, 1.5708F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(-1, 69).addBox(-5.5F, -0.5F, -5.0F, 11.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.0333F, -4.4333F, -1.0F, -0.0873F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(36, 0)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(32, 26)
              .addBox(-4.0F, 0.2889F, -2.0F, 8.0F, 20.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -15.0F, -1.0F, 18.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 5.9556F, 4.0F, -0.3927F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(56, 16).addBox(-3.0F, -3.5F, -2.0F, 6.0F, 7.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offsetAndRotation(0.0F, 5.7889F, -1.7778F, 0.3927F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.0F, -1.0F, 1.0F, 0.0F, 0.0F, -0.6545F)
        );
        RightArm.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(40, 20).addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-2.0F, -1.0F, 0.0F, 0.0F, 1.5708F, -0.6545F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(56, 36).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-1.5F, -0.6778F, -0.0333F, 0.0F, 0.0F, -0.0873F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(16, 43).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(40, 20).mirror().addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
           PartPose.offsetAndRotation(2.0F, -1.0F, 0.0F, 0.0F, -1.5708F, 0.6545F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2.0F, -4.0F, -1.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
           PartPose.offsetAndRotation(3.0F, -1.0F, 1.0F, 0.0F, 0.0F, 0.6545F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(56, 28).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(1.5F, -0.6778F, -0.0333F, 0.0F, 0.0F, 0.0873F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(32, 50).addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(48, 50).addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
