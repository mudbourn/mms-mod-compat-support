package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Plague Doctor — {@code lowlands_clothing:plaguedoctor}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelPlaguedoctor_v01}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelPlagueDoctor extends LowlandsArmorModel {

    public ModelPlagueDoctor(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 17)
              .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.65F))
              .texOffs(32, 17)
              .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F, new CubeDeformation(0.5F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(32, 61).addBox(1.0F, -5.0F, -3.0F, 0.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.8889F, 0.7111F, -9.3556F, 0.1745F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(65, 17).addBox(-1.0F, -4.0F, -2.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, 0.0873F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -0.5F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -7.5F, 0.0F, 0.0F, 1.5708F, 0.0F)
        );
        root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(0, 38)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(32, 36)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 20.0F, 4.0F, new CubeDeformation(0.9F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(0, 56)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(64, 8)
              .addBox(-3.5333F, 5.1111F, -2.4667F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(16, 56)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(64, 0)
              .addBox(-0.5333F, 5.1111F, -2.4667F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(56, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(56, 52).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
