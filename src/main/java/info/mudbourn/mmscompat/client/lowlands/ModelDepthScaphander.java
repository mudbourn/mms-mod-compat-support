package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Depth Scaphander — {@code lowlands_clothing:scaphander}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelDepth_scaphander}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelDepthScaphander extends LowlandsArmorModel {

    public ModelDepthScaphander(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 0)
              .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F))
              .texOffs(0, 87)
              .addBox(-5.0F, -2.0F, -6.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
              .texOffs(0, 87)
              .addBox(-5.0F, -7.0F, -6.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(1, 80).mirror().addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
           PartPose.offsetAndRotation(3.0F, -4.0F, 0.0F, 0.0F, 0.0F, -1.5708F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(1, 80).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.0F, -4.0F, 0.0F, 0.0F, 0.0F, 1.5708F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create().texOffs(0, 19).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(64, 21).mirror().addBox(0.0F, -1.5F, -0.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)).mirror(false),
           PartPose.offsetAndRotation(1.0F, 10.9667F, -3.5F, 0.3054F, -0.3054F, 0.1309F)
        );
        Body.addOrReplaceChild(
           "Body_r2",
           CubeListBuilder.create().texOffs(64, 11).mirror().addBox(0.0F, -1.5F, -0.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)).mirror(false),
           PartPose.offsetAndRotation(4.0F, 12.0333F, -2.2556F, -0.0547F, -0.3006F, 0.3137F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(57, 60).addBox(-1.0F, -2.0F, -1.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.3556F, 9.0F, -0.3556F, 0.0F, 0.6545F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(56, 59).addBox(0.0F, -1.0F, -4.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.0771F, 2.0F, 3.0981F, -0.2182F, 0.5236F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r3",
           CubeListBuilder.create().texOffs(84, 92).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(2.0F, -2.6189F, 8.768F, -0.9429F, 1.0782F, -0.8816F)
        );
        Body.addOrReplaceChild(
           "Body_r4",
           CubeListBuilder.create().texOffs(47, 93).addBox(1.0F, 1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -3.5333F, 8.8889F, -0.3927F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r5",
           CubeListBuilder.create().texOffs(28, 19).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(2.0F, 6.0F, 5.0F, -0.5299F, -0.7119F, 0.3655F)
        );
        Body.addOrReplaceChild(
           "Body_r6",
           CubeListBuilder.create().texOffs(64, 21).addBox(-2.0F, -1.5F, -0.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(-4.0F, 11.5F, -2.2556F, 0.0F, 0.3054F, -0.1309F)
        );
        Body.addOrReplaceChild(
           "Body_r7",
           CubeListBuilder.create().texOffs(64, 16).addBox(-2.0F, -1.5F, -0.5F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(-1.0F, 10.5F, -3.5F, 0.1309F, 0.3054F, -0.1309F)
        );
        Body.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(56, 0).addBox(1.0F, -2.0F, -1.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(5.0F, 3.0F, 0.0F, 0.0F, -0.5236F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(24, 63)
              .addBox(-3.9556F, 3.4667F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F))
              .texOffs(0, 50)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(37, 53).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-2.2778F, -1.0F, 0.0F, 0.0F, 0.0F, 0.3491F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(24, 37)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(40, 63)
              .addBox(0.9556F, 3.4667F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(16, 53).addBox(-1.5F, -2.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(2.2778F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3491F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(40, 37)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .texOffs(56, 44)
              .addBox(-3.0F, 1.0F, -2.4667F, 4.0F, 10.0F, 3.0F, new CubeDeformation(0.41F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(48, 13)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F))
              .texOffs(56, 29)
              .addBox(-1.0F, 1.0F, -2.4667F, 4.0F, 10.0F, 3.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
