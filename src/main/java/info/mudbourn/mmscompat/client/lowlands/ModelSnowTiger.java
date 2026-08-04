package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Snow Tiger — {@code lowlands_clothing:snowtigerarmor}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelsnowtigerarmorv01}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelSnowTiger extends LowlandsArmorModel {

    public ModelSnowTiger(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(36, 27)
              .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F))
              .texOffs(40, 16)
              .addBox(4.0F, -8.0F, 5.0F, 2.0F, 10.0F, 1.0F, new CubeDeformation(0.4F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 27).addBox(-4.0F, -2.0F, -4.0F, 9.0F, 4.0F, 9.0F, new CubeDeformation(1.0F)),
           PartPose.offsetAndRotation(0.3556F, -7.5333F, -0.5333F, 0.0F, 0.0F, 0.3054F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(0, 40)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 18.0F, 4.0F, new CubeDeformation(0.6F))
              .texOffs(0, 0)
              .addBox(-7.0F, -1.0F, -1.2889F, 14.0F, 21.0F, 6.0F, new CubeDeformation(0.803F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create()
              .texOffs(46, 20)
              .addBox(4.0F, -3.0F, 0.0F, 3.0F, 6.0F, 1.0F, new CubeDeformation(0.9F))
              .texOffs(46, 20)
              .addBox(-7.0F, -3.0F, 0.0F, 3.0F, 6.0F, 1.0F, new CubeDeformation(0.9F)),
           PartPose.offsetAndRotation(0.0F, 2.2388F, -2.227F, -0.1745F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(40, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(24, 43).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(40, 43).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
