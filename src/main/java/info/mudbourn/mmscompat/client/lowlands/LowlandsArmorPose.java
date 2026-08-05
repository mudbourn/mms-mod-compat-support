package info.mudbourn.mmscompat.client.lowlands;

import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import java.util.HashMap;
import java.util.Map;

/**
 * Poses a Lowlands set from a snapshot of the wearer's pose taken at submit time.
 *
 * <h2>Where the pose comes from</h2>
 *
 * <p>The vanilla armour model for the slot is posed by
 * {@code mixin.cemrelay.HumanoidArmorLayerMixin}, which relays the wearer's CEM
 * animation onto all four slot models at {@code HumanoidArmorLayer#submit}. That
 * runs before {@code renderArmorPiece}, so by the time
 * {@link #capture(HumanoidRenderState)} is called the pose is already there and the
 * whole job is to read it off.
 *
 * <p>{@code source.setupAnim} must <em>not</em> run in the CEM case:
 * {@code HumanoidModel#setupAnim} writes limb angles absolutely, so it would
 * overwrite the relayed pose with a vanilla reconstruction. It is still the floor
 * for a client with no EMF, or a wearer whose base model carries no CEM animation,
 * where the relay is a no-op and nobody else poses {@code source}.
 * {@link CemLayerPoseRelay#isAnimatedCemModel} is the same predicate the relay
 * gates itself on, so the two agree by construction.
 *
 * <h2>Why it is a snapshot and not a copy at draw time</h2>
 *
 * <p>Submission is deferred: {@code submitModel} only collects a node, and
 * {@code ModelFeatureRenderer} calls {@code setupAnim(state)} on the submitted model
 * at end of frame. Earlier versions did the copy there, reading {@code source} — the
 * shared per-slot {@code ArmorModelSet} model — at draw time. Those models are one
 * instance for the whole game, re-posed by every armour-wearing entity that submits
 * after this one and re-posed again by the vanilla draw path, so what got copied was
 * whoever wrote last, not the wearer. The same was true of the wearer's own model,
 * which {@link CemLayerPoseRelay#relayRoot} was reading for the root transform.
 *
 * <p>The result held still while the player did, which is why it looked right at
 * rest, and detached exactly where the pose moves fastest — attack swings and rolls,
 * where one entity's write lands a whole animation's distance from another's.
 *
 * <p>So the pose is read once, into this object, which exists for exactly one piece
 * on one entity for one frame. {@link #setupAnim} then only replays it, and touches
 * no state anything else can write.
 *
 * <h2>History</h2>
 *
 * <p>Before 0.9.76 the mixin that diverts the draw here failed to apply at all (see
 * {@code LowlandsArmorPieceMixin}), so none of this code ran and the sets were drawn
 * by the stock equipment renderer. The frozen-armour symptom that motivated
 * {@code relayOver}, and the reasoning built on top of it, were describing a code
 * path that was not executing.
 */
public final class LowlandsArmorPose extends Model<Pair<HumanoidRenderState, HumanoidRenderState>> {

    /**
     * The parts read off the vanilla armour model, by name.
     *
     * <p>The same set {@link CemLayerPoseRelay#HUMANOID_ARMOR} relays, so a part the
     * relay poses is a part this carries. A name missing from either model is
     * skipped, leaving it at rest rather than mispositioned. Every set's extra
     * geometry — brims, pauldrons, coat-tails — is nested beneath these six in the
     * ported models, so it follows for free.
     */
    private static final String[] PARTS = {
            "head", "hat", "body", "right_arm", "left_arm", "right_leg", "left_leg"
    };

    private final HumanoidModel<HumanoidRenderState> source;
    private final LowlandsArmorModel delegate;
    private final EntityModel<?> wearer;

    /** Part name to its nine transform fields, filled by {@link #capture}. */
    private final Map<String, float[]> captured = new HashMap<>(PARTS.length);

    /** The wearer's root transform, or null when there is no animated root to take. */
    private float[] capturedRoot;

    private LowlandsArmorPose(HumanoidModel<HumanoidRenderState> source,
                              LowlandsArmorModel delegate,
                              EntityModel<?> wearer) {
        super(delegate.root(), delegate::renderType);
        this.source = source;
        this.delegate = delegate;
        this.wearer = wearer;
    }

    /**
     * Wraps {@code delegate} so it poses itself when drawn.
     *
     * <p>{@link #capture} must be called on the returned object before it is
     * submitted; a pose that was never captured replays nothing and draws at rest.
     *
     * @param source   the vanilla armour model for this slot, already relayed
     * @param delegate the Lowlands model actually being drawn
     * @param wearer   the wearer's base model, used to decide whether the relay will
     *                 have posed {@code source} and to supply the root transform;
     *                 null forces the vanilla floor
     */
    public static LowlandsArmorPose of(HumanoidModel<HumanoidRenderState> source,
                                       LowlandsArmorModel delegate,
                                       EntityModel<?> wearer) {
        return new LowlandsArmorPose(source, delegate, wearer);
    }

    /**
     * Reads the wearer's current pose off the shared models, while it is still theirs.
     *
     * <p>Call from {@code renderArmorPiece}, i.e. inside the wearer's own submit,
     * before returning to the render loop. See the class javadoc for why this cannot
     * wait until draw time.
     */
    public void capture(HumanoidRenderState state) {
        boolean animated = this.wearer != null && CemLayerPoseRelay.isAnimatedCemModel(this.wearer);
        if (!animated) {
            // Nobody else has posed source; the vanilla reconstruction is the floor.
            this.source.setupAnim(state);
        }

        this.captured.clear();
        for (String name : PARTS) {
            ModelPart part = CemLayerPoseRelay.findChild(this.source.root(), name);
            if (part != null) {
                this.captured.put(name, read(part));
            }
        }

        // The limb poses above come from the relayed vanilla armour model, but the
        // root does not: the relay never folds the root in, and source's root is
        // unanimated. DA puts the swim and crawl body transform in the root, so
        // without this the set swims with its limbs while its body stays upright.
        this.capturedRoot = animated ? read(this.wearer.root()) : null;
    }

    @Override
    public void setupAnim(Pair<HumanoidRenderState, HumanoidRenderState> state) {
        this.resetPose();

        ModelPart root = this.delegate.root();
        for (Map.Entry<String, float[]> entry : this.captured.entrySet()) {
            ModelPart part = CemLayerPoseRelay.findChild(root, entry.getKey());
            if (part != null) {
                write(part, entry.getValue());
            }
        }
        if (this.capturedRoot != null) {
            write(root, this.capturedRoot);
        }
    }

    /** The nine transform fields a {@code ModelPart} can express, in write order. */
    private static float[] read(ModelPart part) {
        return new float[] {
                part.x, part.y, part.z,
                part.xRot, part.yRot, part.zRot,
                part.xScale, part.yScale, part.zScale
        };
    }

    private static void write(ModelPart part, float[] pose) {
        part.x = pose[0];
        part.y = pose[1];
        part.z = pose[2];
        part.xRot = pose[3];
        part.yRot = pose[4];
        part.zRot = pose[5];
        part.xScale = pose[6];
        part.yScale = pose[7];
        part.zScale = pose[8];
    }
}
