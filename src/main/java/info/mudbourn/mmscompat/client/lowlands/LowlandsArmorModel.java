package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * A worn Clothing of the Lowlands set.
 *
 * <p>The source mod's armour is not vanilla-layout armour art: each set ships a
 * custom {@code EntityModel} with its own texture size (64x64 up to 144x144) and
 * its own UV layout, and the "layer_1/layer_2" PNGs are atlases for <em>that</em>
 * geometry. Feeding them to the vanilla humanoid equipment renderer maps arbitrary
 * texture regions onto vanilla boxes, which is why the asset-only port rendered
 * every set as flat slabs. The geometry has to come across too; this is the base
 * class for it.
 *
 * <p>Part naming is load-bearing. Fabric's {@code Model#copyTransforms} — which is
 * how the worn set inherits the wearer's pose — matches the <em>direct children of
 * the root</em> by name against the source model, so the six root parts must carry
 * vanilla {@code HumanoidModel} names ({@code head}, {@code body}, {@code right_arm},
 * {@code left_arm}, {@code right_leg}, {@code left_leg}) rather than the source
 * mod's {@code Head}/{@code Body}/{@code RightArm}/… Every set's extra geometry —
 * brims, pauldrons, coat-tails — is nested <em>beneath</em> those six in the source
 * as well, so it follows the pose for free and needs no per-part relay.
 *
 * <p>Because the pose arrives via {@code copyTransforms}, {@link #setupAnim} is
 * deliberately empty. The source mod hand-rolled a partial humanoid animation in
 * each model, which dropped sneaking and riding; inheriting the real pose from the
 * wearer's model is both less code and more correct.
 */
public abstract class LowlandsArmorModel extends EntityModel<HumanoidRenderState> {

    /** Vanilla {@link net.minecraft.client.model.HumanoidModel} root-part names, in slot order. */
    public static final String HEAD = "head";
    public static final String BODY = "body";
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_LEG = "right_leg";
    public static final String LEFT_LEG = "left_leg";

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    protected LowlandsArmorModel(ModelPart root) {
        super(root, RenderTypes::armorCutoutNoCull);
        this.head = root.getChild(HEAD);
        this.body = root.getChild(BODY);
        this.rightArm = root.getChild(RIGHT_ARM);
        this.leftArm = root.getChild(LEFT_ARM);
        this.rightLeg = root.getChild(RIGHT_LEG);
        this.leftLeg = root.getChild(LEFT_LEG);
    }

    /**
     * Shows only the parts belonging to {@code slot}.
     *
     * <p>Mirrors how the source mod split one whole-body model across four armour
     * pieces: the helmet draws the head, the chestplate the torso and both arms,
     * and leggings and boots both draw the legs. Boots drawing the full leg rather
     * than a foot is the source's behaviour, not an oversight — the sets have no
     * separate foot geometry, and the boots piece is textured from the outer layer
     * while leggings use the inner one, so the two do not z-fight.
     */
    public void selectSlot(EquipmentSlot slot) {
        boolean isHead = slot == EquipmentSlot.HEAD;
        boolean isChest = slot == EquipmentSlot.CHEST;
        boolean isLegs = slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;

        this.head.visible = isHead;
        this.body.visible = isChest;
        this.rightArm.visible = isChest;
        this.leftArm.visible = isChest;
        this.rightLeg.visible = isLegs;
        this.leftLeg.visible = isLegs;
    }

    /**
     * No-op: the pose is copied from the wearer's model before this model is
     * submitted. See the class javadoc.
     */
    @Override
    public void setupAnim(HumanoidRenderState state) {
        // intentionally empty
    }
}
