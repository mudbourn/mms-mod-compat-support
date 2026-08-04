package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Guard's Captain — {@code lowlands_clothing:guard_captain_uniform_r}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelGuardCaptainUniform}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelGuardCaptain extends LowlandsArmorModel {

    public ModelGuardCaptain(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(0, 45).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.48F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(32, 18).addBox(-1.0F, -11.0F, -1.0F, 0.0F, 9.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-7.0F, -6.0F, -6.0F, -0.2831F, -0.2928F, 1.0358F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -11.0F, -1.0F, 0.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-4.0F, -6.0F, -5.0F, -0.1461F, -0.8139F, 0.6498F)
        );
        Head.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -8.0F, 17.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-6.0F, -7.3556F, 0.0F, -0.1745F, 0.0F, 0.2182F)
        );
        root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create().texOffs(32, 45).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(56, 45).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.32F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(66, 0).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)),
           PartPose.offsetAndRotation(-2.1778F, 1.0F, 0.0F, 0.0F, 0.0F, -0.7854F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(32, 61).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.32F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(64, 61).addBox(-1.5F, -3.0F, -3.0F, 3.0F, 6.0F, 6.0F, new CubeDeformation(0.3F)),
           PartPose.offsetAndRotation(1.8242F, 0.6464F, 0.0F, 0.0F, 0.0F, 0.7854F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(48, 61)
              .addBox(-2.0F, -0.1778F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.32F))
              .texOffs(64, 18)
              .addBox(-2.5333F, -0.1778F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.51F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create()
              .texOffs(0, 63)
              .addBox(-2.0F, -0.1778F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.321F))
              .texOffs(16, 63)
              .addBox(-1.4667F, -0.1778F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
