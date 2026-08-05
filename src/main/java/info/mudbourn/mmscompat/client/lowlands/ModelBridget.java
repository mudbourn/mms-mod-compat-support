package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Bridget — {@code weaversparadise:bridget_clothing}.
 *
 * <p>Geometry transcribed from Weaver's Paradise's {@code BridgetClothingModel}. The root parts are
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
public final class ModelBridget extends LowlandsArmorModel {

    public ModelBridget(ModelPart root) {
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
		PartDefinition RightLegPants = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(92, 48).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(76, 32).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLegPants = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(76, 48).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(92, 32).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition RightArm = root.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(44, 47).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(0, 47).addBox(-3.7F, 2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(116, -6).addBox(-4.0F, 2.0F, -3.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(56, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.349F))
        .texOffs(126, 8).addBox(-4.2F, 9.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition LeftArm = root.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(56, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(116, -6).addBox(3.7F, 2.0F, -3.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(22, 47).addBox(-1.4F, 2.0F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(126, 8).addBox(3.2F, 9.2F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(0, 60).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.349F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition Body = root.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(32, 15).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(32, 31).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
        .texOffs(112, 37).addBox(-4.0F, 0.0F, -2.4F, 8.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.7F, -1.25F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
        .texOffs(48, 64).addBox(-2.5F, -6.2F, -3.25F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(32, 60).addBox(-1.5F, -4.2F, -5.25F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6776F, 13.7F, -3.1441F, 0.0F, 0.8727F, 0.0F));

        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(104, 65).addBox(-5.2F, 0.0F, -6.25F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(104, 65).addBox(-0.3F, 0.0F, -6.25F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6776F, 13.7F, -3.1441F, 0.8727F, 0.0F, 1.5708F));

        PartDefinition Body_r3 = Body.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(104, 65).addBox(-2.7F, -4.25F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
        .texOffs(104, 65).addBox(2.2F, -4.25F, -1.5F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.6776F, 13.7F, -3.1441F, 2.4435F, 0.0F, 1.5708F));

        PartDefinition Head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2501F))
        .texOffs(0, 31).addBox(-4.0F, -8.7F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F))
        .texOffs(0, 77).addBox(-4.0F, -8.7F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.8F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightLeg = RightLegPants.addOrReplaceChild(RIGHT_BOOT, CubeListBuilder.create().texOffs(16, 60).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(68, 64).addBox(-2.05F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.25F))
        .texOffs(110, 83).addBox(-2.05F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F))
        .texOffs(60, 32).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Right_Leg_r1 = RightLeg.addOrReplaceChild("Right_Leg_r1", CubeListBuilder.create().texOffs(117, 97).addBox(-3.8F, -4.0F, -3.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(1.75F, 9.9F, 2.4F, 0.7854F, 0.0F, 0.0F));

        PartDefinition LeftLeg = LeftLegPants.addOrReplaceChild(LEFT_BOOT, CubeListBuilder.create().texOffs(60, 48).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(72, 0).addBox(-2.05F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.25F))
        .texOffs(90, 0).addBox(-2.05F, 9.0F, -3.0F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.4F))
        .texOffs(32, 63).addBox(-2.05F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Right_Leg_r2 = LeftLeg.addOrReplaceChild("Right_Leg_r2", CubeListBuilder.create().texOffs(117, 97).addBox(-3.9F, -4.0F, -3.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(1.85F, 9.9F, 2.4F, 0.7854F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
