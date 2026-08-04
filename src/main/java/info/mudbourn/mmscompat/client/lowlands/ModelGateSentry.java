package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Gate Sentry — {@code lowlands_clothing:gatekeeperarmorb}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelGatekeeper_corrected}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelGateSentry extends LowlandsArmorModel {

    public ModelGateSentry(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(0, 15)
              .addBox(-4.0F, -8.1778F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.7F))
              .texOffs(0, 31)
              .addBox(-4.0F, -8.1778F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.2F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -0.5F, -7.0F, 14.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, -5.6778F, 0.0F, -0.0873F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(32, 36)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.65F))
              .texOffs(32, 15)
              .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 17.0F, 4.0F, new CubeDeformation(1.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(56, 0).addBox(-7.0F, -3.0F, -1.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.1111F, 12.0F, 2.4444F, -1.3963F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(56, 6).addBox(0.0F, -10.0F, -1.0F, 1.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(5.4667F, 17.0F, 4.0F, 1.0018F, 0.0736F, -0.047F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(0, 47).addBox(-3.8889F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.7F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(16, 47).addBox(-0.1111F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.7F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(48, 52).addBox(-2.5333F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.53F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(32, 52).addBox(-1.4667F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.531F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
