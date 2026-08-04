package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Furnace Master — {@code lowlands_clothing:furnace_master_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelfurnace_master_armor_v01}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelFurnaceMaster extends LowlandsArmorModel {

    public ModelFurnaceMaster(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(56, 0)
              .addBox(4.0F, 1.0F, -1.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
              .texOffs(0, 19)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(56, 59)
              .addBox(-4.0F, 1.0F, -1.0F, 0.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
              .texOffs(32, 13)
              .addBox(10.0F, -18.1778F, 6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
              .texOffs(0, 37)
              .addBox(9.0F, -16.1778F, 5.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
              .texOffs(32, 0)
              .addBox(-14.2889F, -14.8889F, 5.1778F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
              .texOffs(64, 10)
              .addBox(-13.2889F, -16.8889F, 6.1778F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(13, 50).addBox(-2.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-11.0F, -17.4667F, 8.1778F, 0.0F, -0.7854F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(13, 50).addBox(-1.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-11.6951F, -17.4667F, 8.5333F, 0.0F, 0.7854F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(13, 50).addBox(-2.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(12.3556F, -18.7778F, 8.3556F, 0.0F, -0.7854F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(13, 50).addBox(-1.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(11.6444F, -18.7778F, 8.3556F, 0.0F, 0.7854F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(20, 63).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-4.0F, 0.0F, 9.0F, 0.0F, 0.0F, -0.1745F)
        );
        Body.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(48, 29).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 0.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(-5.6284F, -20.0162F, 9.0F, 0.0F, 0.0F, -1.7017F)
        );
        Body.addOrReplaceChild(
           "cube_r7",
           CubeListBuilder.create().texOffs(52, 29).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 0.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(5.6284F, -20.0162F, 9.0F, 0.0F, 0.0F, 1.7017F)
        );
        Body.addOrReplaceChild(
           "cube_r8",
           CubeListBuilder.create().texOffs(16, 63).addBox(-1.0F, -22.0F, -1.0F, 2.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(4.0F, 0.0F, 9.0F, 0.0F, 0.0F, 0.1745F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(24, 19).addBox(-4.0F, -7.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(0.0F, 6.0F, 5.0F, -0.3927F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r2",
           CubeListBuilder.create().texOffs(64, 16).addBox(-2.0F, -1.5F, -0.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(-3.0F, 11.5F, -3.5F, 0.0F, 0.3054F, -0.1309F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(24, 63)
              .addBox(-3.9556F, 3.4667F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F))
              .texOffs(0, 50)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(37, 53)
              .addBox(-3.4222F, -3.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(24, 37)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(16, 53)
              .addBox(0.4222F, -3.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.4F))
              .texOffs(40, 63)
              .addBox(0.9556F, 3.4667F, -2.4667F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(40, 37)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .texOffs(56, 44)
              .addBox(-3.0F, 0.0F, -2.4667F, 4.0F, 10.0F, 3.0F, new CubeDeformation(0.41F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(48, 13)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F))
              .texOffs(56, 29)
              .addBox(-1.0F, 0.0F, -2.4667F, 4.0F, 10.0F, 3.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
