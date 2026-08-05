package info.mudbourn.mmscompat.client.lowlands;

import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Poses a Lowlands set at draw time by copying the vanilla armour model.
 *
 * <p>This is Fabric's {@code TransformCopyingModel} with one condition added. It
 * exists because submission is deferred: {@code ModelFeatureRenderer} calls
 * {@code setupAnim(state)} on the submitted model immediately before drawing it, so
 * anything posed at submit time would be copied from a model still in its rest pose.
 *
 * <h2>Where the pose comes from</h2>
 *
 * <p>{@code source} — the vanilla armour model for this slot — is already posed by
 * the time this runs, by {@code mixin.cemrelay.HumanoidArmorLayerMixin}, which
 * relays the wearer's CEM animation onto all four slot models at {@code submit}.
 * So the whole job here is to copy it.
 *
 * <p>The one thing that must <em>not</em> happen is calling
 * {@code source.setupAnim(state)} first. {@code HumanoidModel#setupAnim} writes limb
 * angles absolutely, so it overwrites the relayed pose with a vanilla reconstruction
 * one line before the copy — which is why the sets tracked vanilla walk cycles and
 * ignored DetailedAnimations entirely, most visibly by continuing to swing their
 * limbs in mid-air where DA eases them to a stop.
 *
 * <p>It is still needed as a floor, though: on a client with no EMF, or a wearer
 * whose base model carries no CEM animation, the relay is a no-op and {@code source}
 * is never posed by anyone. {@link CemLayerPoseRelay#isAnimatedCemModel} is the same
 * predicate the relay gates itself on, so the two agree by construction — if the
 * relay posed {@code source}, this copies it; if it declined, this poses
 * {@code source} the vanilla way and copies that.
 *
 * <h2>History</h2>
 *
 * <p>Earlier versions of this class relayed from the wearer's model directly, on the
 * theory that the armour was rendering frozen. It was not: until 0.9.76 the mixin
 * that diverts the draw here failed to apply at all (see
 * {@code LowlandsArmorPieceMixin}), so none of this code had ever run and the sets
 * were being drawn by the stock equipment renderer. The frozen-armour symptom that
 * motivated {@code relayOver}, and the deferred-submit reasoning built on top of it,
 * were describing a code path that was not executing. Both are gone; what remains is
 * the copy and its fallback.
 */
public final class LowlandsArmorPose extends Model<Pair<HumanoidRenderState, HumanoidRenderState>> {

    private final HumanoidModel<HumanoidRenderState> source;
    private final LowlandsArmorModel delegate;
    private final EntityModel<?> wearer;

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
     * @param source   the vanilla armour model for this slot, already relayed
     * @param delegate the Lowlands model actually being drawn
     * @param wearer   the wearer's base model, used only to decide whether the
     *                 relay will have posed {@code source}; null forces the
     *                 vanilla floor
     */
    public static LowlandsArmorPose of(HumanoidModel<HumanoidRenderState> source,
                                       LowlandsArmorModel delegate,
                                       EntityModel<?> wearer) {
        return new LowlandsArmorPose(source, delegate, wearer);
    }

    @Override
    public void setupAnim(Pair<HumanoidRenderState, HumanoidRenderState> state) {
        this.resetPose();
        if (this.wearer == null || !CemLayerPoseRelay.isAnimatedCemModel(this.wearer)) {
            this.source.setupAnim(state.getFirst());
        }
        this.delegate.copyTransforms(this.source);

        // The limb poses above come from the relayed vanilla armour model, but the
        // root does not: the relay never folds the root in, and source's root is
        // unanimated. DA puts the swim and crawl body transform in the root, so
        // without this the set swims with its limbs while its body stays upright.
        // No-ops when the wearer is not an animated CEM model.
        CemLayerPoseRelay.relayRoot(this.wearer, this.delegate);
    }
}
