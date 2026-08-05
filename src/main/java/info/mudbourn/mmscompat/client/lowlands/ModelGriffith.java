package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Griffith — {@code weaversparadise:griffith_armor}.
 *
 * <p>Geometry transcribed from Weaver's Paradise's {@code GriffithArmorModel}. The root parts are
 * renamed to vanilla {@link net.minecraft.client.model.HumanoidModel} names so
 * {@code copyTransforms} can find them (see {@link LowlandsArmorModel}); every nested
 * part keeps its original name, and box offsets, sizes, {@code texOffs}, {@code mirror}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 128x128 atlas and mean nothing on their own.
 *
     * <p>The source models boots as roots alongside the legs; here they are nested
     * beneath them, with their offsets made relative, so they inherit the leg pose
     * instead of needing a relay of their own. See {@link LowlandsArmorModel#RIGHT_BOOT}.
 *
 * <p>The source's own {@code setupAnim} is deliberately not carried over. It
 * reconstructed a partial humanoid animation that dropped sneaking and riding;
 * {@link LowlandsArmorPose} replays the wearer's real pose instead.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source.
 */
public final class ModelGriffith extends LowlandsArmorModel {

    public ModelGriffith(ModelPart root) {
        super(root);
    }

    /** One whole-body atlas, so every slot draws from the outer layer. */
    @Override
    public TextureLayout textureLayout() {
        return TextureLayout.SINGLE;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
		PartDefinition LeftArm = root.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(16, 37).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4501F))
        .texOffs(0, 64).addBox(-1.0F, -2.0F, 2.5F, 4.0F, 12.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition LeftArm_r1 = LeftArm.addOrReplaceChild("Left Arm_r1", CubeListBuilder.create().texOffs(60, 71).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offsetAndRotation(1.3F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition RightArm = root.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(0, 37).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(64, 0).addBox(-3.0F, -2.0F, 2.5F, 4.0F, 12.0F, 2.0F, new CubeDeformation(0.2501F))
        .texOffs(48, 48).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition RightArm_r1 = RightArm.addOrReplaceChild("Right Arm_r1", CubeListBuilder.create().texOffs(60, 64).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offsetAndRotation(-1.3F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition Body = root.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(32, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.2503F))
        .texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 21).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2501F))
        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 13.0F, 8.0F, new CubeDeformation(0.4501F))
        .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6001F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightLeg = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(72, 39).addBox(-2.2F, 0.0F, 2.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2501F))
        .texOffs(0, 53).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.4501F))
        .texOffs(56, 27).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2502F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLeg = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(56, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2504F))
        .texOffs(16, 53).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.4502F))
        .texOffs(72, 23).addBox(-1.8F, 0.0F, 2.5F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition RightBoot = RightLeg.addOrReplaceChild(RIGHT_BOOT, CubeListBuilder.create().texOffs(64, 47).addBox(0.0F, 0.0F, 1.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.0001F))
        .texOffs(72, 32).addBox(-2.2F, 5.0F, 2.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2501F))
        .texOffs(12, 64).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.4501F))
        .texOffs(28, 64).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.2503F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftBoot = LeftLeg.addOrReplaceChild(LEFT_BOOT, CubeListBuilder.create().texOffs(44, 64).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.2502F))
        .texOffs(64, 47).mirror().addBox(0.0F, 0.0F, 1.0F, 0.0F, 12.0F, 4.0F, new CubeDeformation(0.0001F)).mirror(false)
        .texOffs(56, 38).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.4502F))
        .texOffs(72, 14).addBox(-1.8F, 5.0F, 2.5F, 4.0F, 7.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
