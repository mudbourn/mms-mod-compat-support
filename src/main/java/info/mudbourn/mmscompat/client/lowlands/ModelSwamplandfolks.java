package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Swamplandfolks — {@code lowlands_clothing:swampfolk_outfit}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelSwampfolk_attires}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelSwamplandfolks extends LowlandsArmorModel {

    public ModelSwamplandfolks(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(HEAD, CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, -8.0F, 14.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 1.5778F, -6.9111F, 1.309F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r2",
           CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.65F)),
           PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0436F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(32, 17)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 13.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(0, 33)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(56, 31).addBox(-3.5F, -1.5F, 1.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.5F, 8.8556F, 2.2889F, 1.1509F, 0.147F, -0.3655F)
        );
        Body.addOrReplaceChild(
           "Body_r2",
           CubeListBuilder.create().texOffs(32, 51).addBox(-3.5F, -1.5F, -1.0F, 7.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(0.5F, 10.5F, 4.0F, 0.3655F, 0.147F, -0.3655F)
        );
        Body.addOrReplaceChild(
           "Body_r3",
           CubeListBuilder.create().texOffs(56, 22).addBox(-0.5F, -2.0F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(1.0F)),
           PartPose.offsetAndRotation(5.3889F, 12.0F, -0.0333F, -0.0692F, 0.2527F, -0.3579F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(16, 51).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(56, 17).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-3.4667F, 3.7111F, 2.4667F, -0.9747F, -0.6793F, 0.2667F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(24, 35).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(50, 51).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.4667F, 3.7111F, 2.4667F, -0.9747F, 0.6793F, -0.2667F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(40, 35).addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.47F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(0, 51).addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.46F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
