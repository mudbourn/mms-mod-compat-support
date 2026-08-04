package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Mercenary Swordsman — {@code lowlands_clothing:mercenary_swordman}.
 *
 * <p>Geometry transcribed from the source mod's {@code Modelmercenaryswordsman},
 * with the six root parts renamed to vanilla {@link net.minecraft.client.model.HumanoidModel}
 * names so {@code copyTransforms} can find them (see {@link LowlandsArmorModel}).
 * The box offsets, sizes, {@code texOffs} and {@code CubeDeformation} values are
 * unchanged — they index into the set's own 64x64 atlas and mean nothing on their
 * own.
 *
 * <p>This set is the simplest of the 23: six parts, nine boxes, no nested extras.
 * It uses paired cubes at differing inflations to fake layered cloth rather than
 * adding geometry, which is why the part count matches vanilla exactly.
 */
public final class ModelMercenarySwordsman extends LowlandsArmorModel {

    public ModelMercenarySwordsman(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(HEAD,
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.4F))
                .texOffs(32, 32)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild(BODY,
            CubeListBuilder.create()
                .texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 15.0F, 4.0F, new CubeDeformation(0.5F))
                .texOffs(0, 48)
                .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.8F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild(RIGHT_ARM,
            CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                .texOffs(40, 6)
                .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.9F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild(LEFT_ARM,
            CubeListBuilder.create()
                .texOffs(48, 48)
                .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)),
            PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild(RIGHT_LEG,
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.38F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        root.addOrReplaceChild(LEFT_LEG,
            CubeListBuilder.create()
                .texOffs(32, 48)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.39F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
