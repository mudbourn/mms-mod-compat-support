package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Masquerade — {@code lowlands_clothing:maskerade_armor}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelmaskerade_armor_u}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 96x96 atlas and mean nothing on their own.
 *
 * <p>This set is one of the two that does not use the source mod's usual
 * {@code Head}/{@code Body}/{@code RightArm}/… root names — it ships
 * {@code head}/{@code body}/{@code leg0}/{@code leg1}/{@code arms_left}/
 * {@code arm_right}. Sides were resolved from the pivot signs rather than the
 * names: {@code leg0} sits at x=+2.0 and {@code arms_left} at x=+4.5, and +x is
 * left in model space, so {@code leg0}/{@code arms_left} are the left limbs.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelMasquerade extends LowlandsArmorModel {

    public ModelMasquerade(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create()
              .texOffs(62, 24)
              .addBox(-1.0F, -3.0F, -5.8222F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.5F))
              .texOffs(28, 0)
              .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        head.addOrReplaceChild(
           "head4",
           CubeListBuilder.create().texOffs(22, 52).addBox(0.1624F, -8.0F, -0.1854F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, 0.829F, 0.0F)
        );
        head.addOrReplaceChild(
           "head3",
           CubeListBuilder.create().texOffs(0, 48).addBox(-5.1624F, -8.0F, -0.1854F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, -0.829F, 0.0F)
        );
        head.addOrReplaceChild(
           "nose3",
           CubeListBuilder.create().texOffs(32, 66).addBox(-5.1624F, -1.0F, -2.1854F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(8.0F, -2.0F, 0.0F, 0.0F, -0.829F, 0.0F)
        );
        head.addOrReplaceChild(
           "nose4",
           CubeListBuilder.create().texOffs(70, 24).addBox(3.1624F, -1.0F, -2.1854F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-8.0F, -2.0F, 0.0F, 0.0F, 0.829F, 0.0F)
        );
        PartDefinition body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create()
              .texOffs(28, 34)
              .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
              .texOffs(0, 0)
              .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.6F))
              .texOffs(0, 24)
              .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.99F))
              .texOffs(60, 8)
              .addBox(-1.0F, 10.0F, 8.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F))
              .texOffs(62, 16)
              .addBox(-7.0F, -1.0F, 10.8889F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        body.addOrReplaceChild(
           "body_r1",
           CubeListBuilder.create().texOffs(44, 52).addBox(-6.0F, -2.0F, -1.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.98F)),
           PartPose.offsetAndRotation(0.0F, -1.9778F, 7.4222F, 0.6545F, 0.0F, 0.0F)
        );
        body.addOrReplaceChild(
           "body_r2",
           CubeListBuilder.create().texOffs(28, 18).addBox(-6.0F, -26.0F, -2.0F, 12.0F, 11.0F, 5.0F, new CubeDeformation(0.99F)),
           PartPose.offsetAndRotation(0.0F, 24.0F, 3.5778F, -0.1309F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(56, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.52F)),
           PartPose.offset(2.0F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create().texOffs(44, 58).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
           PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create()
              .texOffs(60, 58)
              .addBox(-0.5F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
              .texOffs(16, 66)
              .addBox(3.5F, 2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
           PartPose.offset(4.5F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create()
              .texOffs(0, 62)
              .addBox(-3.5F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
              .texOffs(24, 66)
              .addBox(-5.5F, 2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
           PartPose.offset(-4.5F, 2.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 96, 96);
    }
}
