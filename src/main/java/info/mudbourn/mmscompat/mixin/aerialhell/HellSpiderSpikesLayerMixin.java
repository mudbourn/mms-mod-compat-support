package info.mudbourn.mmscompat.mixin.aerialhell;

import fr.factionbedrock.aerialhell.Client.EntityModels.HellSpiderSpikeModel;
import fr.factionbedrock.aerialhell.Client.EntityRender.Layers.HellSpiderSpikesLayer;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the Hell / Shadow / Crystal Spider spikes follow the FA+Spiders body.
 *
 * <p>{@code HellSpiderSpikesLayer} draws a standalone
 * {@code HellSpiderSpikeModel} whose only animation is head yaw and pitch copied
 * straight off the render state. Under FA+Spiders the spider underneath is a CEM
 * model whose abdomen, thorax and head all move, so the spikes sit in the
 * vanilla rest pose while the body they belong to walks away from them.
 *
 * <p>Unlike the Cortinarius Cow, this model does <em>not</em> mirror the base
 * model's layout. Both spike parts hang off the root at pivot (0, 24, 0) with
 * their cubes authored in absolute coordinates, so a plain pose copy would swing
 * them about the wrong point entirely. That is exactly the pivot mismatch
 * {@link CemLayerPoseRelay} exists to absorb — it re-expresses the authored
 * geometry in the source part's rest frame before carrying it to the animated
 * one, so no pivot or naming agreement is required.
 *
 * <p>Source names are vanilla {@code SpiderModel} part names, not the jem's; see
 * {@link CemLayerPoseRelay#HELL_SPIDER_SPIKES}. Note that vanilla parents the
 * head to {@code body0}, so the relay has to accumulate that chain rather than
 * read the head's local rotation alone.
 */
@Mixin(HellSpiderSpikesLayer.class)
public abstract class HellSpiderSpikesLayerMixin {

    @Shadow @Final private HellSpiderSpikeModel spiderSpikeModel;

    /**
     * The wearer's own model, reached by cast rather than {@code @Shadow}.
     *
     * <p>{@code getParentModel()} is declared on {@link RenderLayer}, the
     * superclass, not on the layer this mixin targets. Mixin resolves
     * {@code @Shadow} members against the target class itself, so shadowing it
     * threw {@code InvalidMixinException} at apply time and silently disabled
     * this entire mixin. It is {@code public} on {@code RenderLayer}, so a cast
     * reaches it with no shadow at all.
     */
    @Unique
    private EntityModel<?> mms$parentModel() {
        return ((RenderLayer<?, ?>) (Object) this).getParentModel();
    }

    @Inject(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lfr/factionbedrock/aerialhell/Client/EntityModels/HellSpiderSpikeModel;"
                            + "setupAnim(Lfr/factionbedrock/aerialhell/Client/EntityRender/State/"
                            + "HellSpiderRenderState;)V",
                    shift = At.Shift.AFTER))
    private void mms$relaySpiderPose(CallbackInfo ci) {
        CemLayerPoseRelay.relay(this.mms$parentModel(), this.spiderSpikeModel, CemLayerPoseRelay.HELL_SPIDER_SPIKES);
    }
}
