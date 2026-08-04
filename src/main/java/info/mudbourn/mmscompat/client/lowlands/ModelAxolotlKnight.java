package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Axolotl Knight — {@code lowlands_clothing:axolotl_knight}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelaxolotl_armor}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelAxolotlKnight extends LowlandsArmorModel {

    public ModelAxolotlKnight(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 14)
              .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F))
              .texOffs(0, 0)
              .addBox(-5.0F, -8.3556F, -5.5333F, 10.0F, 5.0F, 9.0F, new CubeDeformation(0.6F))
              .texOffs(0, 30)
              .addBox(-8.0F, -12.8222F, 4.1F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(32, 14)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(54, 68)
              .addBox(-4.4667F, 11.0F, -2.6F, 9.0F, 8.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(30, 29)
              .addBox(0.0F, 0.0F, -4.0F, 0.0F, 12.0F, 9.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(48, 30)
              .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(56, 11)
              .addBox(-7.0F, -6.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(0, 41).addBox(-4.0F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(48, 46)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(56, 19)
              .addBox(0.0F, -6.0F, 0.0F, 7.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(38, 0).addBox(-1.0F, -3.0F, -3.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(22, 50).addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(0, 52).addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
