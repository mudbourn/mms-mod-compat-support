package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Netherborn Pirate — {@code lowlands_clothing:netherborn_pirate}.
 *
 * <p>Geometry transcribed from the source mod's {@code ModelHellborn_Pirate}. The six root
 * parts are renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel});
 * every nested part keeps its original name, and box offsets, sizes, {@code texOffs}
 * and {@code CubeDeformation} values are unchanged — they index into this set's own
 * 144x144 atlas and mean nothing on their own.
 *
 * <p>Generated, then reviewed by hand. Do not edit the numbers without re-checking
 * them against the source jar.
 */
public final class ModelNetherbornPirate extends LowlandsArmorModel {

    public ModelNetherbornPirate(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition Head = root.addOrReplaceChild(
           HEAD,
           CubeListBuilder.create().texOffs(56, 87).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r1",
           CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, -8.0F, -12.0F, 0.0F, 9.0F, 28.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-4.5333F, -7.0473F, -3.9889F, 0.0594F, -0.5464F, -0.4061F)
        );
        Head.addOrReplaceChild(
           "cube_r2",
           CubeListBuilder.create().texOffs(84, 0).addBox(-14.5F, -5.0F, 0.0F, 30.0F, 6.0F, 0.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(-0.5667F, -4.7651F, 6.9047F, -0.5672F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r3",
           CubeListBuilder.create().texOffs(56, 19).addBox(0.0F, -5.0F, -13.0F, 0.0F, 6.0F, 28.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(-6.3556F, -6.6918F, -2.9223F, 0.1376F, -0.5335F, -0.558F)
        );
        Head.addOrReplaceChild(
           "cube_r4",
           CubeListBuilder.create().texOffs(56, 53).addBox(0.0F, -5.0F, -12.0F, 0.0F, 6.0F, 28.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(5.4667F, -6.6918F, -3.6334F, 0.1376F, 0.5335F, 0.558F)
        );
        Head.addOrReplaceChild(
           "cube_r5",
           CubeListBuilder.create().texOffs(0, 56).addBox(0.0F, -8.0F, -12.0F, 0.0F, 9.0F, 28.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.6444F, -7.0473F, -3.9889F, 0.0594F, 0.5464F, 0.4061F)
        );
        Head.addOrReplaceChild(
           "cube_r6",
           CubeListBuilder.create().texOffs(84, 18).addBox(-11.5F, -2.0F, 0.0F, 23.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.0333F, -10.4756F, -14.1815F, 0.1309F, 0.0F, 0.0F)
        );
        Head.addOrReplaceChild(
           "cube_r7",
           CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, 0.0F, -12.0F, 23.0F, 0.0F, 19.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-0.5333F, -5.1778F, -0.1778F, -0.1745F, 0.0F, 0.0F)
        );
        PartDefinition Body = root.addOrReplaceChild(
           BODY,
           CubeListBuilder.create().texOffs(88, 87).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
           PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r1",
           CubeListBuilder.create().texOffs(80, 104).addBox(-2.0F, -5.0F, 0.0F, 4.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(2.3556F, 5.0F, -3.1111F, 0.0871F, 0.5528F, 0.3872F)
        );
        Body.addOrReplaceChild(
           "Body_r2",
           CubeListBuilder.create().texOffs(0, 107).addBox(-4.5F, -5.0F, 0.0F, 9.0F, 10.0F, 0.0F, new CubeDeformation(0.01F)),
           PartPose.offsetAndRotation(0.0333F, 14.3556F, 3.3556F, 0.1745F, 0.0F, 0.0F)
        );
        Body.addOrReplaceChild(
           "Body_r3",
           CubeListBuilder.create().texOffs(72, 104).addBox(-2.0F, -5.0F, 0.0F, 4.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(-2.3556F, 5.0F, -3.1111F, 0.0871F, -0.5528F, -0.3872F)
        );
        PartDefinition RightArm = root.addOrReplaceChild(
           RIGHT_ARM,
           CubeListBuilder.create().texOffs(88, 103).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)),
           PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        RightArm.addOrReplaceChild(
           "cube_r8",
           CubeListBuilder.create().texOffs(18, 109).mirror().addBox(0.0F, -2.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
           PartPose.offsetAndRotation(-3.8226F, 5.6982F, 0.0F, 0.0F, 0.0F, -0.2182F)
        );
        RightArm.addOrReplaceChild(
           "RightArm_r1",
           CubeListBuilder.create().texOffs(104, 103).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.6F)),
           PartPose.offsetAndRotation(-2.5333F, -1.0F, 0.1778F, 0.0F, 0.0F, -0.7854F)
        );
        PartDefinition LeftArm = root.addOrReplaceChild(
           LEFT_ARM,
           CubeListBuilder.create().texOffs(56, 104).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)),
           PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        LeftArm.addOrReplaceChild(
           "cube_r9",
           CubeListBuilder.create().texOffs(104, 10).addBox(0.0F, -2.0F, -2.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(3.8226F, 5.3427F, 0.0F, 0.0F, 0.0F, 0.2182F)
        );
        PartDefinition RightLeg = root.addOrReplaceChild(
           RIGHT_LEG,
           CubeListBuilder.create()
              .texOffs(24, 93)
              .addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.43F))
              .texOffs(40, 93)
              .mirror()
              .addBox(-2.3556F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F))
              .mirror(false),
           PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        RightLeg.addOrReplaceChild(
           "cube_r10",
           CubeListBuilder.create().texOffs(36, 109).addBox(-3.0F, -2.0F, -1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 7.0F, -1.5778F, 0.48F, 0.0F, 0.0F)
        );
        PartDefinition LeftLeg = root.addOrReplaceChild(
           LEFT_LEG,
           CubeListBuilder.create().texOffs(40, 93).addBox(-1.6444F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)),
           PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        LeftLeg.addOrReplaceChild(
           "cube_r11",
           CubeListBuilder.create().texOffs(26, 109).addBox(-2.0F, -2.0F, -1.0F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
           PartPose.offsetAndRotation(0.0F, 7.0F, -1.5778F, 0.48F, 0.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 144, 144);
    }
}
