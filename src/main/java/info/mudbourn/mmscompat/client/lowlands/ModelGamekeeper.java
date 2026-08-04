package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Gamekeeper — {@code lowlands_clothing:gamemaster_outfit}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelGamekeeperarmor}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 80x80 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelGamekeeper extends LowlandsArmorModel {

    public ModelGamekeeper(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(38, 28).addBox(-4.0F, -10.1778F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r1",
           CubeListBuilder.create().texOffs(0, 12).addBox(-5.3556F, -1.0F, -7.0F, 11.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.1444F, -7.0472F, -2.0086F, -0.1309F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "Head_r2",
           CubeListBuilder.create().texOffs(36, 62).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(5.5333F, -6.1778F, 5.3556F, 0.3927F, 0.7854F, 0.0F)
        );
        root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create().texOffs(42, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 15.0F, 4.0F, new CubeDeformation(0.68F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(48, 46).addBox(-4.2444F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(0, 59).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.55F)),
           PartPose.offsetAndRotation(-3.0667F, 4.7444F, -0.5333F, 0.0F, 0.0F, -0.1745F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(0, 43).addBox(0.2444F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "LeftArm_r1",
           CubeListBuilder.create().texOffs(18, 62).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.55F)),
           PartPose.offsetAndRotation(3.0667F, 4.7444F, -0.5333F, 0.0F, 0.0F, 0.1745F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(16, 43).addBox(-2.5333F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.53F)),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(32, 46).addBox(-1.4667F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.529F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 80, 80);
    }
}
