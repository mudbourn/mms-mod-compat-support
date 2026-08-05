package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Felix — {@code weaversparadise:felix_clothing}.
 *
 * <p>Geometry transcribed from Weaver's Paradise's {@code FelixClothingModel}. The root parts are
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
public final class ModelFelix extends LowlandsArmorModel {

    public ModelFelix(ModelPart root) {
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
		PartDefinition Left_Arm = root.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(40, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
        .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F)), PartPose.offset(5.0F, 1.0F, 0.0F));

        PartDefinition Right_Arm = root.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(32, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(0, 80).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.4501F))
        .texOffs(24, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(-5.0F, 1.0F, 0.0F));

        PartDefinition Right_Arm_r1 = Right_Arm.addOrReplaceChild("Right_Arm_r1", CubeListBuilder.create().texOffs(70, 60).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2F, 4.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

        PartDefinition Right_Arm_r2 = Right_Arm.addOrReplaceChild("Right_Arm_r2", CubeListBuilder.create().texOffs(64, 29).addBox(-3.5F, -2.0F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2F, 2.7F, 0.1F, 0.0F, 0.0F, 0.7418F));

        PartDefinition Body = root.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(57, 1).addBox(-3.0F, 6.0F, 2.0F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(72, 19).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(32, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(56, 32).mirror().addBox(-7.4801F, -4.8969F, -0.05F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.75F, 3.05F, 0.0843F, -0.0226F, 0.2608F));

        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(64, 16).mirror().addBox(-3.6774F, -0.7352F, 0.05F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 10.75F, 2.95F, 0.0791F, -0.0368F, 0.4349F));

        PartDefinition Body_r3 = Body.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(56, 32).addBox(-0.5199F, -4.8969F, -0.05F, 8.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.75F, 3.05F, 0.0843F, 0.0226F, -0.2608F));

        PartDefinition Body_r4 = Body.addOrReplaceChild("Body_r4", CubeListBuilder.create().texOffs(72, 0).addBox(0.05F, -0.4475F, -1.4898F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 9.2F, 0.0F, -0.4363F, 0.0F, -0.0873F));

        PartDefinition Body_r5 = Body.addOrReplaceChild("Body_r5", CubeListBuilder.create().texOffs(68, 71).addBox(-0.65F, -2.2F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 9.2F, 0.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition Body_r6 = Body.addOrReplaceChild("Body_r6", CubeListBuilder.create().texOffs(56, 68).addBox(-0.15F, -2.1412F, -2.9693F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 9.2F, 0.0F, 0.3491F, 0.0F, -0.0873F));

        PartDefinition Body_r7 = Body.addOrReplaceChild("Body_r7", CubeListBuilder.create().texOffs(22, 71).addBox(0.15F, -0.4475F, -0.5102F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 9.2F, 0.0F, 0.4363F, 0.0F, -0.0873F));

        PartDefinition Body_r8 = Body.addOrReplaceChild("Body_r8", CubeListBuilder.create().texOffs(64, 56).addBox(-0.15F, -2.1412F, -0.0307F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.75F, 9.2F, 0.0F, -0.3491F, 0.0F, -0.0873F));

        PartDefinition Body_r9 = Body.addOrReplaceChild("Body_r9", CubeListBuilder.create().texOffs(64, 16).addBox(-0.3226F, -0.7352F, 0.05F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.75F, 2.95F, 0.0791F, 0.0368F, -0.4349F));

        PartDefinition Body_r10 = Body.addOrReplaceChild("Body_r10", CubeListBuilder.create().texOffs(74, 52).addBox(-1.0F, -1.5F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3F, 2.5F, -2.6F, 0.0F, 0.0F, -0.4363F));

        PartDefinition Body_r11 = Body.addOrReplaceChild("Body_r11", CubeListBuilder.create().texOffs(74, 28).addBox(-1.0F, -1.5F, 0.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3F, 2.5F, -2.6F, 0.0F, 0.0F, 0.4363F));

        PartDefinition Body_r12 = Body.addOrReplaceChild("Body_r12", CubeListBuilder.create().texOffs(56, 44).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 2.4F, 0.1745F, 0.0F, 0.0F));

        PartDefinition Body_r13 = Body.addOrReplaceChild("Body_r13", CubeListBuilder.create().texOffs(56, 14).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -2.4F, -0.1745F, 0.0F, 0.0F));

        PartDefinition Body_r14 = Body.addOrReplaceChild("Body_r14", CubeListBuilder.create().texOffs(66, 62).addBox(0.15F, -2.1412F, -0.0307F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 9.2F, 0.0F, -0.3491F, 0.0F, 0.0873F));

        PartDefinition Body_r15 = Body.addOrReplaceChild("Body_r15", CubeListBuilder.create().texOffs(62, 68).addBox(0.15F, -2.1412F, -2.9693F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 9.2F, 0.0F, 0.3491F, 0.0F, 0.0873F));

        PartDefinition Body_r16 = Body.addOrReplaceChild("Body_r16", CubeListBuilder.create().texOffs(0, 72).addBox(-0.15F, -0.4475F, -0.5102F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 9.2F, 0.0F, 0.4363F, 0.0F, 0.0873F));

        PartDefinition Body_r17 = Body.addOrReplaceChild("Body_r17", CubeListBuilder.create().texOffs(26, 71).addBox(-0.05F, -0.4475F, -1.4898F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 9.2F, 0.0F, -0.4363F, 0.0F, 0.0873F));

        PartDefinition Body_r18 = Body.addOrReplaceChild("Body_r18", CubeListBuilder.create().texOffs(70, 56).addBox(-0.35F, -2.2F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, 9.2F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition Body_r19 = Body.addOrReplaceChild("Body_r19", CubeListBuilder.create().texOffs(72, 38).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 14.2103F, 8.7686F, -0.637F, 0.0F, 0.0F));

        PartDefinition Body_r20 = Body.addOrReplaceChild("Body_r20", CubeListBuilder.create().texOffs(72, 41).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 16.1279F, 9.4184F, -1.2479F, 0.0F, 0.0F));

        PartDefinition Body_r21 = Body.addOrReplaceChild("Body_r21", CubeListBuilder.create().texOffs(72, 35).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 17.8635F, 8.5935F, -1.9984F, 0.0F, 0.0F));

        PartDefinition Body_r22 = Body.addOrReplaceChild("Body_r22", CubeListBuilder.create().texOffs(72, 32).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 18.3841F, 6.8006F, -2.8274F, 0.0F, 0.0F));

        PartDefinition Body_r23 = Body.addOrReplaceChild("Body_r23", CubeListBuilder.create().texOffs(30, 72).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 18.4061F, 6.9799F, -0.5585F, 0.0F, 0.0F));

        PartDefinition Body_r24 = Body.addOrReplaceChild("Body_r24", CubeListBuilder.create().texOffs(22, 64).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 15.5061F, 4.2799F, -1.0821F, 0.0F, 0.0F));

        PartDefinition Body_r25 = Body.addOrReplaceChild("Body_r25", CubeListBuilder.create().texOffs(64, 24).addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.2501F)), PartPose.offsetAndRotation(0.0F, 11.5F, 2.6F, -1.2654F, 0.0F, 0.0F));

        PartDefinition Head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.251F))
        .texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F))
        .texOffs(72, 6).addBox(3.9F, -7.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(72, 10).addBox(-4.8F, -7.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(8, 70).mirror().addBox(0.0F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
        .texOffs(50, 70).addBox(-8.9F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -4.2F, 1.2F, 0.5236F, 0.0F, 0.0F));

        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(56, 64).addBox(-1.5F, -1.0F, 0.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -9.9F, 0.0F, -0.1339F, -0.4623F, 0.2934F));

        PartDefinition Head_r3 = Head.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(72, 48).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(72, 44).addBox(9.3F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -5.7F, -1.7F, 0.2618F, 0.0F, 0.0F));

        PartDefinition Head_r4 = Head.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(16, 72).addBox(0.0F, -2.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(12, 72).addBox(-9.1F, -2.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6F, -4.2F, -0.7F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Head_r5 = Head.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(46, 70).addBox(0.0F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(42, 70).addBox(-8.9F, -2.0F, -1.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -4.2F, -1.2F, -0.5236F, 0.0F, 0.0F));

        PartDefinition Head_r6 = Head.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(72, 14).addBox(0.0F, -2.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(4, 72).addBox(9.3F, -2.0F, -1.0F, 0.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.6F, -4.2F, 0.7F, 0.3491F, 0.0F, 0.0F));

        PartDefinition Head_r7 = Head.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(74, 24).addBox(0.0F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(72, 62).addBox(-9.1F, -1.0F, -1.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6F, -5.7F, 1.7F, -0.2618F, 0.0F, 0.0F));

        PartDefinition Head_r8 = Head.addOrReplaceChild("Head_r8", CubeListBuilder.create().texOffs(64, 52).addBox(-2.5F, -1.0F, 0.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -9.9F, 0.0F, -0.1339F, 0.4623F, -0.2934F));

        PartDefinition Right_Leg = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition Left_Leg_r1 = Right_Leg.addOrReplaceChild("Left_Leg_r1", CubeListBuilder.create().texOffs(64, 46).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 8.8F, -0.1F, 0.0F, 0.0F, -0.3491F));

        PartDefinition Right_Leg_r1 = Right_Leg.addOrReplaceChild("Right_Leg_r1", CubeListBuilder.create().texOffs(40, 64).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 8.8F, -0.1F, 0.0F, 0.0F, 0.3491F));

        PartDefinition Right_Leg_r2 = Right_Leg.addOrReplaceChild("Right_Leg_r2", CubeListBuilder.create().texOffs(0, 70).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.8F, 2.5F, 0.3491F, 0.0F, 0.0F));

        PartDefinition Right_Leg_r3 = Right_Leg.addOrReplaceChild("Right_Leg_r3", CubeListBuilder.create().texOffs(56, 46).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.8F, -2.5F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Left_Leg = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(48, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F))
        .texOffs(32, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2501F))
        .texOffs(56, 39).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition Left_Leg_r2 = Left_Leg.addOrReplaceChild("Left_Leg_r2", CubeListBuilder.create().texOffs(48, 64).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 8.8F, -0.1F, 0.0F, 0.0F, -0.3491F));

        PartDefinition Left_Leg_r3 = Left_Leg.addOrReplaceChild("Left_Leg_r3", CubeListBuilder.create().texOffs(30, 70).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.8F, -2.5F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Left_Leg_r4 = Left_Leg.addOrReplaceChild("Left_Leg_r4", CubeListBuilder.create().texOffs(22, 69).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.8F, 2.5F, 0.3491F, 0.0F, 0.0F));

        PartDefinition Left_Leg_r5 = Left_Leg.addOrReplaceChild("Left_Leg_r5", CubeListBuilder.create().texOffs(68, 68).addBox(-1.0F, -2.0F, 0.0F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2F, 7.6F, 0.1F, 0.0F, 0.0F, -0.7418F));

        PartDefinition Left_Leg_r6 = Left_Leg.addOrReplaceChild("Left_Leg_r6", CubeListBuilder.create().texOffs(72, 22).addBox(-1.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 8.0F, 0.0F, 0.0F, 0.0F, 0.829F));

        PartDefinition Left_Leg_r7 = Left_Leg.addOrReplaceChild("Left_Leg_r7", CubeListBuilder.create().texOffs(32, 64).addBox(0.0F, -1.0F, -2.0F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 8.8F, -0.1F, 0.0F, 0.0F, 0.3491F));

        PartDefinition Right_Leg2 = Right_Leg.addOrReplaceChild(RIGHT_BOOT, CubeListBuilder.create().texOffs(36, 83).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2502F))
        .texOffs(52, 83).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4501F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Left_Leg2 = Left_Leg.addOrReplaceChild(LEFT_BOOT, CubeListBuilder.create().texOffs(84, 51).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4501F))
        .texOffs(68, 83).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.2502F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
