package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Bret Corsair — {@code lowlands_clothing:bret_corsair_o__armor_v2}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelBret_armor_corsair_v2}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelBretCorsair extends LowlandsArmorModel {

    public ModelBretCorsair(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(44, 41).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(44, 58).addBox(-10.5F, -1.0F, 0.0F, 21.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.0333F, -4.7651F, 6.9047F, -0.5672F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(0, 43).addBox(0.0F, -1.0F, -10.0F, 0.0F, 2.0F, 22.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-5.0F, -6.6918F, -3.2778F, 0.1376F, -0.5335F, -0.558F)
        );
        Head.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, -1.0F, -10.0F, 0.0F, 2.0F, 22.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(5.0F, -6.6918F, -3.2778F, 0.1376F, 0.5335F, 0.558F)
        );
        Head.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 0.0F, -12.0F, 23.0F, 0.0F, 19.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -5.1778F, -0.1778F, -0.1745F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(44, 60)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(0, 67)
              .addBox(-4.0F, 0.3556F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(44, 19).addBox(-6.0F, -8.0F, -3.0F, 12.0F, 16.0F, 6.0F, new CubeDeformation(1.0F)),
           PartPose.offsetAndRotation(-3.0667F, 8.1778F, 0.3556F, 0.0F, 0.0F, 0.0873F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(40, 76).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(76, 41).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(56, 76).addBox(-1.0F, -4.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(4.4667F, 6.0F, 0.0F, 0.0F, 0.0F, 0.0873F)
        );
        PartDefinition RightLeg = root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(24, 67).addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        RightLeg.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(64, 76).addBox(-3.0F, -2.0F, -1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 7.0F, -1.5778F, 0.48F, 0.0F, 0.0F)
        );
        PartDefinition LeftLeg = root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(68, 60).addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        LeftLeg.addOrReplaceChild(
           "cube_r7",
           CubeListBuilder.create().texOffs(76, 55).addBox(-2.0F, -2.0F, -1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 7.0F, -1.5778F, 0.48F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
