package info.mudbourn.mmscompat.client.lowlands;

import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Poses a Lowlands set from a snapshot of the wearer's pose taken at submit time.
 *
 * <h2>Where the pose comes from</h2>
 *
 * <p>The vanilla armour model for the slot is posed by
 * {@code mixin.cemrelay.HumanoidArmorLayerMixin}, which relays the wearer's CEM
 * animation onto all four slot models at {@code HumanoidArmorLayer#submit}. That
 * runs before {@code renderArmorPiece}, so by the time {@link #of} is called the
 * pose is already there and the whole job is to read it off.
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
 * <p>So the pose is read once, into this object, which is live for exactly one piece
 * on one entity for one frame. {@link #setupAnim} then only replays it, and touches
 * no state anything else can write.
 *
 * <h2>Pooling</h2>
 *
 * <p>These are acquired from a per-delegate ring rather than allocated. A fresh
 * instance per piece per entity per frame meant running {@link Model}'s constructor
 * that often, and that constructor walks the delegate's entire part tree to build
 * its part map — twice over, since {@code fabric-rendering-v1} mixes a second walk
 * into it. On a 1.21.11 client that measured at ~3% of the whole render thread with
 * only a handful of armour stands in view, which does not survive a room full of
 * them.
 *
 * <p>The ring is keyed on the delegate because {@link Model}'s root and render type
 * are bound at construction, so a pooled instance belongs to one
 * {@link LowlandsArmorModel} for life. {@link LowlandsArmorSets} hands out one
 * delegate per (asset, slot), so that is the granularity of the ring.
 *
 * <p>{@link #RING} entries per key bounds the pool without needing a frame boundary
 * to reset a cursor on — there is no hook that reliably brackets both the world pass
 * and the inventory/GUI paths that also render armour. The cost of the bound is that
 * a key acquired more than {@code RING} times between a submit and its deferred draw
 * wraps onto a still-pending entry. That takes {@code RING} entities wearing the same
 * set in the same slot on screen at once, and the failure mode is the one the shared
 * delegate already has (see below), not a crash.
 *
 * <h2>Known limit: the delegate is shared</h2>
 *
 * <p>{@link #setupAnim} writes into {@code delegate.root()}, and the delegate is a
 * singleton per (asset, slot). Two entities wearing the same set in the same slot in
 * one frame therefore draw with whichever posed last, however many distinct pose
 * objects are involved. Pooling neither causes nor worsens this. Fixing it needs a
 * per-wearer delegate instance, which is a separate change.
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

    /** The nine transform fields a {@code ModelPart} can express. */
    private static final int FIELDS = 9;

    /** Pooled instances per delegate. See the class javadoc for how this is bounded. */
    private static final int RING = 32;

    /**
     * The ring for one delegate: its instances and the next index to hand out.
     *
     * <p>Render thread only, so unsynchronised. Everything that reaches
     * {@link #of} comes through {@code HumanoidArmorLayer#renderArmorPiece}, and
     * {@link LowlandsArmorSets#model} already cannot run off the render thread
     * because baking touches {@code Minecraft.getInstance()}.
     */
    private static final class Ring {
        final LowlandsArmorPose[] slots = new LowlandsArmorPose[RING];
        int cursor;
    }

    private static final Map<LowlandsArmorModel, Ring> POOL = new IdentityHashMap<>();

    private final LowlandsArmorModel delegate;

    /** {@code delegate}'s parts, resolved once, aligned with {@link #PARTS}. */
    private final ModelPart[] delegateParts = new ModelPart[PARTS.length];

    /**
     * The vanilla armour model to read from, and its resolved parts.
     *
     * <p>Re-resolved only when the identity changes. It is per-slot but also
     * per-{@code HumanoidArmorLayer}, so a set worn by a player and by an armour
     * stand in the same frame arrives here with two different sources on the same
     * pooled instance.
     */
    private HumanoidModel<HumanoidRenderState> source;
    private final ModelPart[] sourceParts = new ModelPart[PARTS.length];

    private EntityModel<?> wearer;

    /** Snapshot storage, aligned with {@link #PARTS}. Reused; never reallocated. */
    private final float[][] captured = new float[PARTS.length][FIELDS];
    private final boolean[] present = new boolean[PARTS.length];

    /** The wearer's root transform, valid only while {@link #hasRoot}. */
    private final float[] capturedRoot = new float[FIELDS];
    private boolean hasRoot;

    private LowlandsArmorPose(LowlandsArmorModel delegate) {
        super(delegate.root(), delegate::renderType);
        this.delegate = delegate;
        for (int i = 0; i < PARTS.length; i++) {
            this.delegateParts[i] = CemLayerPoseRelay.findChild(delegate.root(), PARTS[i]);
        }
    }

    /**
     * A pose for {@code delegate} carrying a snapshot of the wearer's current pose.
     *
     * <p>The snapshot is taken here rather than by a separate call the caller has to
     * remember, because it has to happen at submit time — see the class javadoc — and
     * because a pooled instance that was handed out but never captured would replay
     * the previous borrower's pose rather than drawing at rest.
     *
     * @param source   the vanilla armour model for this slot, already relayed
     * @param delegate the Lowlands model actually being drawn
     * @param wearer   the wearer's base model, used to decide whether the relay will
     *                 have posed {@code source} and to supply the root transform;
     *                 null forces the vanilla floor
     * @param state    the wearer's render state, for the vanilla-floor reconstruction
     */
    public static LowlandsArmorPose of(HumanoidModel<HumanoidRenderState> source,
                                       LowlandsArmorModel delegate,
                                       EntityModel<?> wearer,
                                       HumanoidRenderState state) {
        Ring ring = POOL.computeIfAbsent(delegate, key -> new Ring());
        int index = ring.cursor;
        ring.cursor = index + 1 == RING ? 0 : index + 1;

        LowlandsArmorPose pose = ring.slots[index];
        if (pose == null) {
            pose = new LowlandsArmorPose(delegate);
            ring.slots[index] = pose;
        }

        pose.bind(source, wearer);
        pose.capture(state);
        return pose;
    }

    /** Drops pooled instances so a resource reload rebuilds them against fresh models. */
    public static void invalidate() {
        POOL.clear();
    }

    private void bind(HumanoidModel<HumanoidRenderState> source, EntityModel<?> wearer) {
        if (this.source != source) {
            this.source = source;
            for (int i = 0; i < PARTS.length; i++) {
                this.sourceParts[i] = CemLayerPoseRelay.findChild(source.root(), PARTS[i]);
            }
        }
        this.wearer = wearer;
    }

    /**
     * Reads the wearer's current pose off the shared models, while it is still theirs.
     *
     * <p>Runs from {@code renderArmorPiece}, i.e. inside the wearer's own submit,
     * before returning to the render loop. See the class javadoc for why this cannot
     * wait until draw time.
     */
    private void capture(HumanoidRenderState state) {
        boolean animated = this.wearer != null && CemLayerPoseRelay.isAnimatedCemModel(this.wearer);
        if (!animated) {
            // Nobody else has posed source; the vanilla reconstruction is the floor.
            this.source.setupAnim(state);
        }

        for (int i = 0; i < PARTS.length; i++) {
            ModelPart part = this.sourceParts[i];
            this.present[i] = part != null;
            if (part != null) {
                read(part, this.captured[i]);
            }
        }

        // The limb poses above come from the relayed vanilla armour model, but the
        // root does not: the relay never folds the root in, and source's root is
        // unanimated. DA puts the swim and crawl body transform in the root, so
        // without this the set swims with its limbs while its body stays upright.
        this.hasRoot = animated;
        if (animated) {
            read(this.wearer.root(), this.capturedRoot);
        }
    }

    @Override
    public void setupAnim(Pair<HumanoidRenderState, HumanoidRenderState> state) {
        this.resetPose();

        for (int i = 0; i < PARTS.length; i++) {
            ModelPart part = this.delegateParts[i];
            if (this.present[i] && part != null) {
                write(part, this.captured[i]);
            }
        }
        if (this.hasRoot) {
            write(this.delegate.root(), this.capturedRoot);
        }
    }

    /** Reads {@code part}'s nine transform fields into {@code into}, in write order. */
    private static void read(ModelPart part, float[] into) {
        into[0] = part.x;
        into[1] = part.y;
        into[2] = part.z;
        into[3] = part.xRot;
        into[4] = part.yRot;
        into[5] = part.zRot;
        into[6] = part.xScale;
        into[7] = part.yScale;
        into[8] = part.zScale;
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
