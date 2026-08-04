package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Ratcatcher — {@code lowlands_clothing:ratcatcherc01}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelratcatcher_armor}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelRatcatcher extends LowlandsArmorModel {

    public ModelRatcatcher(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -14.0F, -4.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.4F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(0, 53).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(6.3556F, -3.4F, -1.2889F, 0.0165F, 4.0E-4F, -0.004F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(32, 15).addBox(-4.5F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-0.5F, -6.0F, 0.0F, 0.0F, 0.0F, -0.0873F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create().texOffs(32, 27).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 18.0F, 4.0F, new CubeDeformation(0.8F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(22, 52).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-2.0F, 13.8889F, -3.0F, 0.0867F, 0.5597F, 0.2046F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(11, 63).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-2.5778F, 15.5333F, -1.4667F, -0.0555F, -0.5648F, 0.1034F)
        );
        Body.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(12, 70).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-2.5778F, 13.9556F, 5.6667F, 0.0278F, -0.5666F, -0.0517F)
        );
        Body.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(56, 27).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.9778F, 13.5333F, -2.0F, 0.2821F, 0.4359F, 0.0481F)
        );
        Body.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(56, 37).addBox(-1.0F, -4.0F, -4.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.0667F, 14.8444F, -1.2889F, 0.4745F, -0.9858F, -0.1884F)
        );
        Body.addOrReplaceChild(
           "Head_r2",
           CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.5F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0436F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(48, 0)
              .addBox(-3.4667F, 8.0F, -2.4667F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.5F))
              .texOffs(48, 49)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(65, 47).addBox(-1.5F, -6.0F, 0.5F, 4.0F, 12.0F, -1.0F, new CubeDeformation(0.5F)),
           PartPose.offsetAndRotation(-4.1444F, 4.0F, 0.3556F, 0.0894F, 0.3829F, 0.2355F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(0, 37)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
              .texOffs(48, 6)
              .addBox(-1.5333F, 8.0F, -2.4667F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.5F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(22, 63).addBox(-2.5F, -6.0F, 0.5F, 4.0F, 12.0F, -1.0F, new CubeDeformation(0.5F)),
           PartPose.offsetAndRotation(4.1444F, 4.0F, 0.3556F, 0.0894F, -0.3829F, -0.2355F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(16, 37).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(32, 49).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
