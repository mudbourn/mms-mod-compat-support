package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Siege — {@code lowlands_clothing:siege_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelSiegeArmorv4}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelSiege extends LowlandsArmorModel {

    public ModelSiege(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.1778F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -6.5F, -4.0F, 0.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.8222F, -6.6778F, 0.0F, 0.0F, -0.1309F, 0.2618F)
        );
        Head.addOrReplaceChild(
           "Head_r2",
           CubeListBuilder.create().texOffs(58, 24).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 9.0F, 2.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(0.0F, -2.2111F, -3.3556F, 0.1309F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r3",
           CubeListBuilder.create().texOffs(24, 30).addBox(0.0F, -6.5F, -4.0F, 0.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.8222F, -6.6778F, 0.0F, 0.0F, 0.1309F, -0.2618F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(0, 16)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.65F))
              .texOffs(24, 16)
              .addBox(-4.0F, 0.0F, -2.4667F, 8.0F, 9.0F, 5.0F, new CubeDeformation(0.66F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "LeftLeg_r1",
           CubeListBuilder.create().texOffs(50, 10).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.53F)),
           PartPose.offsetAndRotation(4.5F, 15.0F, 0.0F, 0.0F, 0.0F, -0.1745F)
        );
        Body.addOrReplaceChild(
           "RightLeg_r1",
           CubeListBuilder.create().texOffs(40, 48).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.53F)),
           PartPose.offsetAndRotation(-4.5F, 15.0F, 0.0F, 0.0F, 0.0F, 0.1745F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(70, 0).addBox(0.0F, -4.5F, -2.5F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-4.0F, 4.5F, 4.0333F, 0.0F, -0.48F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r2",
           CubeListBuilder.create().texOffs(0, 69).addBox(0.0F, -4.5F, -2.5F, 0.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(4.0F, 4.5F, 4.0333F, 0.0F, 0.48F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(0, 53).addBox(-3.7F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.95F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create()
              .texOffs(16, 51)
              .addBox(-7.75F, -6.0F, 0.0167F, 12.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(32, 62)
              .addBox(-0.75F, -2.0F, -2.5167F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.7F)),
           PartPose.offsetAndRotation(-4.0749F, -1.9652F, 0.05F, 0.0F, 0.0F, 0.2182F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r2",
           CubeListBuilder.create().texOffs(70, 15).addBox(-0.5F, -3.0F, -2.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.7F)),
           PartPose.offsetAndRotation(-3.9889F, 8.4F, 0.0333F, 0.0F, 0.0F, -0.4363F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(60, 51).addBox(-0.3F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.95F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create()
              .texOffs(52, 67)
              .addBox(-4.25F, -2.0F, -2.5167F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.7F))
              .texOffs(32, 0)
              .addBox(-4.25F, -6.0F, 0.0167F, 12.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(4.0749F, -1.9652F, 0.05F, 0.0F, 0.0F, -0.2182F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r2",
           CubeListBuilder.create().texOffs(72, 67).addBox(-2.5F, -3.0F, -2.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.7F)),
           PartPose.offsetAndRotation(3.9889F, 8.4F, 0.0333F, 0.0F, 0.0F, 0.4363F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(16, 61).addBox(-2.5333F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.53F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(60, 35).addBox(-1.4667F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.53F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
