package info.mudbourn.mmscompat.client.lowlands;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
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
 * how the worn set inherits the wearer's pose — matches parts <em>by name at any
 * depth</em> against the source model (it builds a recursive name map with
 * {@code putIfAbsent}, so the shallowest part of a given name wins), so the six root
 * parts must carry
 * vanilla {@code HumanoidModel} names ({@code head}, {@code body}, {@code right_arm},
 * {@code left_arm}, {@code right_leg}, {@code left_leg}) rather than the source
 * mod's {@code Head}/{@code Body}/{@code RightArm}/… Every set's extra geometry —
 * brims, pauldrons, coat-tails — is nested <em>beneath</em> those six in the source
 * as well, so it follows the pose for free and needs no per-part relay.
 *
 * <p>{@link #setupAnim} is deliberately empty, and this model is never submitted on
 * its own. It is submitted wrapped in {@link LowlandsArmorPose}, which snapshots the
 * wearer's pose at submit time and replays it here at draw time. The source mod
 * hand-rolled a partial humanoid animation per model, which dropped sneaking and
 * riding; inheriting the real pose is both less code and more correct.
 */
public abstract class LowlandsArmorModel extends EntityModel<HumanoidRenderState> {

    /** Vanilla {@link net.minecraft.client.model.HumanoidModel} root-part names, in slot order. */
    public static final String HEAD = "head";
    public static final String BODY = "body";
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_LEG = "right_leg";
    public static final String LEFT_LEG = "left_leg";

    /**
     * Optional dedicated boot geometry, nested beneath the matching leg part.
     *
     * <p>No Clothing of the Lowlands set has these — that mod's boots piece redraws
     * the whole leg from the outer layer. Sets from mods that <em>do</em> model boots
     * separately declare parts under these names and get the split behaviour in
     * {@link #selectSlot} for free.
     *
     * <p>They must be nested beneath {@code right_leg}/{@code left_leg} rather than
     * added as roots. {@link LowlandsArmorPose} replays only the seven vanilla
     * humanoid part names, so a boot part hung off the model root would never be
     * posed and would float at the wearer's feet; nested, it inherits the leg's
     * transform with no relay at all.
     */
    public static final String RIGHT_BOOT = "right_boot";
    public static final String LEFT_BOOT = "left_boot";

    /**
     * How a set's textures are laid out across the two equipment layers.
     *
     * <p>{@link EquipmentClientInfo.LayerType} decides which texture the equipment
     * renderer resolves out of {@code equipment/<asset>.json}, and the choice is a
     * property of how the source mod authored its atlases — not of the slot alone.
     */
    public enum TextureLayout {
        /**
         * Separate inner and outer atlases: the leggings piece draws from
         * {@code humanoid_leggings}, every other slot from {@code humanoid}.
         *
         * <p>Vanilla's own rule, and what Clothing of the Lowlands ships. It is also
         * what keeps that mod's leggings and boots from z-fighting, since both redraw
         * the same leg geometry and only the texture differs.
         */
        INNER_OUTER,

        /**
         * One atlas for every slot, drawn from {@code humanoid} throughout.
         *
         * <p>For sets that ship a single whole-body texture. Forcing such a set onto
         * the inner layer for leggings resolves a texture the pack does not define.
         * Safe only where the slots do not overlap geometrically — i.e. alongside
         * dedicated {@link #RIGHT_BOOT} geometry.
         */
        SINGLE
    }

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    /** Dedicated boot geometry, or null for a set that draws boots as the whole leg. */
    private final ModelPart rightBoot;
    private final ModelPart leftBoot;
    private final boolean hasDedicatedBoots;

    protected LowlandsArmorModel(ModelPart root) {
        super(root, RenderTypes::armorCutoutNoCull);
        this.head = root.getChild(HEAD);
        this.body = root.getChild(BODY);
        this.rightArm = root.getChild(RIGHT_ARM);
        this.leftArm = root.getChild(LEFT_ARM);
        this.rightLeg = root.getChild(RIGHT_LEG);
        this.leftLeg = root.getChild(LEFT_LEG);

        // Detected rather than declared: a set either models boots or it does not,
        // and reading that off the part tree cannot drift out of sync with the
        // geometry the way an overridable flag could.
        this.rightBoot = this.rightLeg.hasChild(RIGHT_BOOT) ? this.rightLeg.getChild(RIGHT_BOOT) : null;
        this.leftBoot = this.leftLeg.hasChild(LEFT_BOOT) ? this.leftLeg.getChild(LEFT_BOOT) : null;
        this.hasDedicatedBoots = this.rightBoot != null && this.leftBoot != null;
    }

    /**
     * How this set's textures span the two equipment layers.
     *
     * <p>Defaults to vanilla's inner/outer split, which is every Clothing of the
     * Lowlands set. Override to {@link TextureLayout#SINGLE} for a set authored as
     * one whole-body atlas.
     */
    public TextureLayout textureLayout() {
        return TextureLayout.INNER_OUTER;
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
     *
     * <p>A set that <em>does</em> model boots (see {@link #RIGHT_BOOT}) takes the
     * other branch: leggings draw the leg, boots draw only the boot children. That
     * removes the overlap the inner/outer texture split existed to hide, which is
     * why {@link TextureLayout#SINGLE} is only safe in that case.
     */
    public void selectSlot(EquipmentSlot slot) {
        boolean isHead = slot == EquipmentSlot.HEAD;
        boolean isChest = slot == EquipmentSlot.CHEST;
        boolean isLegs = slot == EquipmentSlot.LEGS;
        boolean isFeet = slot == EquipmentSlot.FEET;

        this.head.visible = isHead;
        this.body.visible = isChest;
        this.rightArm.visible = isChest;
        this.leftArm.visible = isChest;

        // The leg parts carry both pieces, so they stay visible for either slot.
        this.rightLeg.visible = isLegs || isFeet;
        this.leftLeg.visible = isLegs || isFeet;

        if (!this.hasDedicatedBoots) {
            return;
        }

        // With modelled boots the two pieces stop being the same geometry drawn from
        // two atlases. The boots slot shows only the boot children, and the leg's own
        // cubes are suppressed with skipDraw — visible has to stay true or the boots
        // would go with it, since they are nested beneath the leg to inherit its pose.
        this.rightLeg.skipDraw = isFeet;
        this.leftLeg.skipDraw = isFeet;
        this.rightBoot.visible = isFeet;
        this.leftBoot.visible = isFeet;
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
