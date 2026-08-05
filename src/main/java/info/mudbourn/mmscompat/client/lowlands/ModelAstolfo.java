package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Astolfo — {@code weaversparadise:astolfo_armor}.
 *
 * <p>Geometry transcribed from Weaver's Paradise's {@code AstolfoArmorModel}. The root parts are
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
public final class ModelAstolfo extends LowlandsArmorModel {

    public ModelAstolfo(ModelPart root) {
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
		PartDefinition Head = root.addOrReplaceChild(HEAD,
        		CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.51F)).texOffs(0, 17).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F)),
        		PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(40, 68).mirror().addBox(-1.0F, -1.0F, -0.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
        		PartPose.offsetAndRotation(-4.1F, -8.1F, 1.8F, -0.7854F, -0.1745F, 0.3927F));
        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(58, 26).mirror().addBox(-1.0F, -1.0F, -1.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        		PartPose.offsetAndRotation(-4.6F, -6.35F, 1.6F, 0.4363F, 0.0F, 0.3927F));
        PartDefinition Head_r3 = Head.addOrReplaceChild("Head_r3", CubeListBuilder.create().texOffs(47, 68).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
        		PartPose.offsetAndRotation(-4.6F, -7.35F, 0.6F, 0.0F, 0.0F, 0.3927F));
        PartDefinition Head_r4 = Head.addOrReplaceChild("Head_r4", CubeListBuilder.create().texOffs(68, 48).mirror().addBox(-1.0F, -1.0F, -2.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
        		PartPose.offsetAndRotation(-4.6F, -6.9F, -0.9F, -0.7854F, 0.2182F, 0.3927F));
        PartDefinition Head_r5 = Head.addOrReplaceChild("Head_r5", CubeListBuilder.create().texOffs(68, 55).addBox(-1.0F, -1.0F, -1.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(-4.7F, -6.05F, 0.0F, -0.4363F, 0.0F, 0.3927F));
        PartDefinition Head_r6 = Head.addOrReplaceChild("Head_r6", CubeListBuilder.create().texOffs(58, 26).addBox(1.0F, -1.0F, -1.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(4.6F, -6.35F, 1.6F, 0.4363F, 0.0F, -0.3927F));
        PartDefinition Head_r7 = Head.addOrReplaceChild("Head_r7", CubeListBuilder.create().texOffs(58, 26).addBox(1.0F, -1.0F, -1.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(4.7F, -6.05F, 0.0F, -0.4363F, 0.0F, -0.3927F));
        PartDefinition Head_r8 = Head.addOrReplaceChild("Head_r8", CubeListBuilder.create().texOffs(68, 48).addBox(1.0F, -1.0F, -2.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(4.6F, -6.9F, -0.9F, -0.7854F, -0.2182F, -0.3927F));
        PartDefinition Head_r9 = Head.addOrReplaceChild("Head_r9", CubeListBuilder.create().texOffs(47, 68).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(4.6F, -7.35F, 0.6F, 0.0F, 0.0F, -0.3927F));
        PartDefinition Head_r10 = Head.addOrReplaceChild("Head_r10", CubeListBuilder.create().texOffs(40, 68).addBox(1.0F, -1.0F, -0.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(4.1F, -8.1F, 1.6F, -0.7854F, 0.1745F, -0.3927F));
        PartDefinition Head_r11 = Head.addOrReplaceChild("Head_r11", CubeListBuilder.create().texOffs(0, 67).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(-3.25F, -5.1F, -4.6F, 0.0F, 0.0F, 0.5236F));
        PartDefinition Head_r12 = Head.addOrReplaceChild("Head_r12", CubeListBuilder.create().texOffs(3, 110).addBox(-3.7F, -4.0F, -0.5F, 3.0F, 7.0F, 8.0F, new CubeDeformation(0.5F)),
        		PartPose.offsetAndRotation(-2.0F, -2.7F, -3.4F, 0.0F, 0.0F, 0.3491F));
        PartDefinition Head_r13 = Head.addOrReplaceChild("Head_r13", CubeListBuilder.create().texOffs(28, 110).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 7.0F, 8.0F, new CubeDeformation(0.5F)),
        		PartPose.offsetAndRotation(3.9F, -3.4F, -3.4F, 0.0F, 0.0F, -0.3491F));
        PartDefinition Head_r14 = Head.addOrReplaceChild("Head_r14", CubeListBuilder.create().texOffs(9, 67).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(3.25F, -5.1F, -4.6F, 0.0F, 0.0F, -0.5672F));
        PartDefinition Body = root.addOrReplaceChild(BODY,
        		CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.27F)).texOffs(33, 17).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
        		PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(63, 37).mirror().addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(-0.1F)).mirror(false),
        		PartPose.offsetAndRotation(-4.7F, 12.9F, 0.0F, 0.0F, 0.0F, 0.2618F));
        PartDefinition Body_r2 = Body.addOrReplaceChild("Body_r2", CubeListBuilder.create().texOffs(63, 26).mirror().addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
        		PartPose.offsetAndRotation(-5.0F, 11.5F, 0.0F, 0.0F, 0.0F, 0.3927F));
        PartDefinition Body_r3 = Body.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(63, 37).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(-0.1F)),
        		PartPose.offsetAndRotation(4.6F, 12.9F, 0.0F, 0.0F, 0.0F, -0.2618F));
        PartDefinition Body_r4 = Body.addOrReplaceChild("Body_r4", CubeListBuilder.create().texOffs(63, 26).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(5.0F, 11.5F, 0.0F, 0.0F, 0.0F, -0.3927F));
        PartDefinition Body_r5 = Body.addOrReplaceChild("Body_r5", CubeListBuilder.create().texOffs(56, 68).mirror().addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(56, 68).mirror()
        		.addBox(-2.0F, -1.0F, -5.2F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, 11.0F, 2.6F, 0.0F, 0.0F, -0.384F));
        PartDefinition Body_r6 = Body.addOrReplaceChild("Body_r6",
        		CubeListBuilder.create().texOffs(56, 68).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(56, 68).addBox(-2.0F, -1.0F, -5.2F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(-3.0F, 11.0F, 2.6F, 0.0F, 0.0F, 0.384F));
        PartDefinition Body_r7 = Body.addOrReplaceChild("Body_r7", CubeListBuilder.create().texOffs(18, 67).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
        		PartPose.offsetAndRotation(0.0F, 12.65F, -1.4F, -0.3491F, 0.0F, 0.0F));
        PartDefinition BodyLayer_r1 = Body.addOrReplaceChild("BodyLayer_r1", CubeListBuilder.create().texOffs(58, 17).addBox(-2.5F, -1.5F, -2.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)),
        		PartPose.offsetAndRotation(0.5F, 0.25F, -0.35F, 0.0F, -0.7854F, 0.0F));
        PartDefinition RightArm = root.addOrReplaceChild(RIGHT_ARM,
        		CubeListBuilder.create().texOffs(46, 34).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.271F)).texOffs(0, 50).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
        		PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition LeftArm = root.addOrReplaceChild(LEFT_ARM,
        		CubeListBuilder.create().texOffs(17, 50).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.271F)).texOffs(34, 51).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.41F)),
        		PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition RightLeg = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(51, 51).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.261F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition LeftLeg_r1 = RightLeg.addOrReplaceChild("LeftLeg_r1", CubeListBuilder.create().texOffs(29, 68).mirror().addBox(-2.0F, -2.0F, 1.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.263F)).mirror(false),
        		PartPose.offsetAndRotation(0.0F, 2.5F, 0.9F, 0.2618F, 0.0F, 0.0F));
        PartDefinition LeftLeg = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(58, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.262F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition LeftLeg_r2 = LeftLeg.addOrReplaceChild("LeftLeg_r2", CubeListBuilder.create().texOffs(29, 68).addBox(-2.0F, -2.0F, 1.0F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.263F)),
        		PartPose.offsetAndRotation(0.0F, 2.5F, 0.8F, 0.2618F, 0.0F, 0.0F));
        PartDefinition RightBoot = RightLeg.addOrReplaceChild(RIGHT_BOOT, CubeListBuilder.create().texOffs(53, 109).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.261F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition LeftBoot = LeftLeg.addOrReplaceChild(LEFT_BOOT, CubeListBuilder.create().texOffs(72, 109).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.262F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
