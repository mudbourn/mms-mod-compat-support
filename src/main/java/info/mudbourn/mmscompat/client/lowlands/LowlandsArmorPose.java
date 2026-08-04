package info.mudbourn.mmscompat.client.lowlands;

import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Poses a Lowlands set at draw time, from the vanilla armour pose and then from
 * the wearer's CEM animation on top.
 *
 * <p>This is Fabric's {@code TransformCopyingModel} with one extra step. It exists
 * because submission is deferred: {@code ModelFeatureRenderer} calls
 * {@code setupAnim(state)} on the submitted model immediately before drawing it, so
 * anything posed at submit time is copied from a model still in its rest pose and
 * the armour renders frozen. Everything therefore happens here, in this order:
 *
 * <ol>
 *   <li>{@code resetPose()} — back to the authored rest pose. This model wraps the
 *       delegate's own root, so it resets the delegate. Part visibility set at bake
 *       time survives, because {@code resetPose} does not touch {@code visible}.</li>
 *   <li>{@code source.setupAnim(state)} then {@code copyTransforms} — the vanilla
 *       humanoid pose: walk, sneak, ride, arm swing.</li>
 *   <li>{@link CemLayerPoseRelay} from the wearer's own model — resource-pack
 *       animation (DetailedAnimations via EMF), when there is any.</li>
 * </ol>
 *
 * <p>Step 3 must be {@code relayOver}, not {@code relay}. The plain relay bases the
 * target on its <em>rest</em> pose and writes the result absolutely, so it discards
 * step 2 entirely; when the wearer is momentarily unanimated it resolves to the
 * authored rest pose and the armour renders frozen — indistinguishable from the
 * deferred-submit bug this class was written to fix, and the regression that shipped
 * in 0.9.72. {@code relayOver} bases it on the pose step 2 just produced, so the
 * wearer's animation arrives as a delta and an unanimated wearer contributes
 * nothing.
 *
 * <p>With that variant step 3 genuinely cannot subtract: the relay also no-ops
 * outright whenever EMF is absent or the wearer's base model is not an animated CEM
 * model, so the floor is exactly the vanilla pose from step 2.
 *
 * <p>Why the relay rather than copying the wearer's model directly: EMF applies CEM
 * animation from inside {@code ModelPart#render}, after {@code setupAnim} has
 * returned, so a plain copy of the parent's parts gets the vanilla pose and not the
 * animated one. The relay reconstructs the animated frame instead, and absorbs the
 * pivot and naming mismatches between two unrelated part trees. See
 * {@code CemLayerPoseRelay} for the matrix it solves.
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
     * @param source   the vanilla armour model for this slot, supplying the base pose
     * @param delegate the Lowlands model actually being drawn
     * @param wearer   the wearer's base model, or null to skip the CEM relay
     */
    public static LowlandsArmorPose of(HumanoidModel<HumanoidRenderState> source,
                                       LowlandsArmorModel delegate,
                                       EntityModel<?> wearer) {
        return new LowlandsArmorPose(source, delegate, wearer);
    }

    /**
     * DIAGNOSTIC — temporary. How far down the pose pipeline to go.
     *
     * <p>All 23 sets render mangled, which puts the fault in this shared path
     * rather than in any one set's transcribed geometry — but the path has three
     * stages and the symptom does not say which one. Rather than guess a fourth
     * time, each stage is switchable so a hotswap can walk them:
     *
     * <ul>
     *   <li>{@code 0} — rest pose only. The authored geometry, untouched. If this
     *       is already wrong, the transcription is at fault and posing is innocent.</li>
     *   <li>{@code 1} — plus the vanilla armour pose. If 0 is right and this is
     *       wrong, {@code copyTransforms} is mismatching the two part trees.</li>
     *   <li>{@code 2} — plus the CEM relay. If 1 is right and this is wrong, the
     *       relay's delta is the culprit; that is the stage 0.9.73 added.</li>
     * </ul>
     *
     * <p>Remove this field and inline stage 2 once the stage is identified.
     */
    private static final int STAGE = 0;

    @Override
    public void setupAnim(Pair<HumanoidRenderState, HumanoidRenderState> state) {
        this.resetPose();
        if (STAGE < 1) {
            return;
        }
        this.source.setupAnim(state.getFirst());
        this.delegate.copyTransforms(this.source);
        if (STAGE < 2) {
            return;
        }
        if (this.wearer != null) {
            CemLayerPoseRelay.relayOver(this.wearer, this.delegate, CemLayerPoseRelay.HUMANOID_ARMOR);
        }
    }
}
