package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Giselle — {@code weaversparadise:giselle_armor}.
 *
 * <p>Geometry transcribed from Weaver's Paradise's {@code GiselleArmorModel}. The root parts are
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
public final class ModelGiselle extends LowlandsArmorModel {

    public ModelGiselle(ModelPart root) {
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
		PartDefinition LeftArm = root.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(0, 46).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
        .texOffs(16, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition RightArm = root.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(28, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
        .texOffs(44, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Body = root.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.252F))
        .texOffs(32, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Body_r1 = Body.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(64, 59).addBox(-1.0F, -2.25F, -2.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.25F))
        .texOffs(40, 64).addBox(-1.0F, -0.25F, -2.0F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(4.0F, 9.582F, -0.553F, 0.6037F, 0.0998F, -0.1434F));

        PartDefinition Head = root.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F))
        .texOffs(0, 32).addBox(-7.0F, -14.0F, 0.3F, 14.0F, 14.0F, 0.0F, new CubeDeformation(0.001F))
        .texOffs(60, 32).addBox(-4.0F, 0.0F, 2.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.6001F))
        .texOffs(0, 16).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Head_r1 = Head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(60, 42).addBox(-4.5F, 1.0F, -2.5F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.001F))
        .texOffs(0, 64).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, -9.5F, -2.1F, 0.0F, -0.2182F, -0.1484F));

        PartDefinition Head_r2 = Head.addOrReplaceChild("Head_r2", CubeListBuilder.create().texOffs(20, 64).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(-3.3F, -9.5F, -2.2F, 0.0F, -0.2182F, -0.1484F));

        PartDefinition RightLeg = root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create().texOffs(56, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
        .texOffs(32, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.451F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLeg = root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create().texOffs(56, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.251F))
        .texOffs(48, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.4502F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition RightBoot = RightLeg.addOrReplaceChild(RIGHT_BOOT, CubeListBuilder.create().texOffs(64, 47).addBox(-2.0F, 10.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.45F))
        .texOffs(32, 55).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.451F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightLeg_r1 = RightBoot.addOrReplaceChild("RightLeg_r1", CubeListBuilder.create().texOffs(54, 64).addBox(-3.9F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.45F)), PartPose.offsetAndRotation(1.9F, 11.3563F, -2.2495F, 0.3491F, 0.0F, 0.0F));

        PartDefinition LeftBoot = LeftLeg.addOrReplaceChild(LEFT_BOOT, CubeListBuilder.create().texOffs(48, 55).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.4502F))
        .texOffs(64, 55).addBox(-2.0F, 10.0F, -4.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.451F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftLeg_r1 = LeftBoot.addOrReplaceChild("LeftLeg_r1", CubeListBuilder.create().texOffs(64, 51).addBox(-0.1F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.45F)), PartPose.offsetAndRotation(-1.9F, 11.3563F, -2.2495F, 0.3491F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
