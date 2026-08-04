package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Highlander — {@code lowlands_clothing:highlands_suitb}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelHighlands_longkilt}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelHighlander extends LowlandsArmorModel {

    public ModelHighlander(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.4F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(4.0F, -9.0F, -4.0F, -0.5236F, -0.6981F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 68).addBox(-5.5F, -1.5F, -4.5F, 11.0F, 3.0F, 9.0F, new CubeDeformation(0.4F)),
           PartPose.offsetAndRotation(0.5F, -6.5F, 0.0333F, 0.0F, 0.0F, 0.1309F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(0, 17)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 19.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(24, 17)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 19.0F, 4.0F, new CubeDeformation(0.9F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(48, 6).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-7.6309F, -4.0521F, 0.9651F, 0.1309F, -0.5672F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(48, 0).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-5.2889F, -1.0F, -2.7111F, 1.0908F, -0.5672F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(48, 6).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-11.5792F, -2.1571F, 0.5887F, 0.5497F, -0.6348F, -0.8718F)
        );
        Body.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(48, 0).addBox(-3.0F, 0.0F, -3.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-6.8889F, -1.0F, -2.1778F, 1.2105F, -0.6826F, -0.8848F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(0, 40)
              .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
              .texOffs(48, 12)
              .addBox(-0.5333F, 7.0F, -2.4667F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(16, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(32, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
