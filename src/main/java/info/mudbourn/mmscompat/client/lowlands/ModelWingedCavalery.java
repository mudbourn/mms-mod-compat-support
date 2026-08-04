package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Winged Cavalery — {@code lowlands_clothing:wingedcavaleryarmor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelWingedcavaleryarmor}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelWingedCavalery extends LowlandsArmorModel {

    public ModelWingedCavalery(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(20, 14)
              .addBox(-4.0F, -9.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.62F))
              .texOffs(0, 57)
              .addBox(-2.0F, -10.0F, -1.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition bone = Head.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, -0.3927F, 0.0F, 0.0F));
        bone.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, -0.5F, -7.0F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.8F)).mirror(false),
           PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(20, 32)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(44, 32)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -33.0F, -2.0F, 0.0F, 33.0F, 10.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-1.4667F, 9.0F, 3.6444F, -0.215F, -0.0376F, -0.1705F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(0, 14).addBox(1.0F, -33.0F, -2.0F, 0.0F, 33.0F, 10.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(1.4667F, 9.0F, 3.6444F, -0.215F, 0.0376F, 0.1705F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(52, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(56, 48).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(-1.6778F, 1.5F, -0.0333F, 0.0F, 0.0F, 0.1309F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(56, 58).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(1.6778F, 1.5F, -0.0333F, 0.0F, 0.0F, -0.1309F)
        );
        PartDefinition RightLeg = root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(40, 48)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .texOffs(52, 0)
              .mirror()
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F))
              .mirror(false),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        RightLeg.addOrReplaceChild(
           "RightLeg_r1",
           CubeListBuilder.create().texOffs(20, 48).addBox(-4.9F, -12.0F, -3.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.61F)),
           PartPose.offsetAndRotation(0.3222F, 12.0F, 0.5333F, 0.0F, 0.0F, 0.0436F)
        );
        PartDefinition LeftLeg = root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(52, 0)
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F))
              .texOffs(40, 48)
              .mirror()
              .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .mirror(false),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        LeftLeg.addOrReplaceChild(
           "LeftLeg_r1",
           CubeListBuilder.create().texOffs(20, 64).addBox(-0.1F, -12.0F, -3.0F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.61F)),
           PartPose.offsetAndRotation(-0.3222F, 12.0F, 0.5333F, 0.0F, 0.0F, -0.0436F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
