package info.mudbourn.mmscompat.mixin.heldpose;

import info.mudbourn.mmscompat.client.PoseDebugEmf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.parts.EMFModelPartRoot;

/**
 * Samples the arms at {@code RETURN} of {@code EMFModelPartRoot#animate}, tagged
 * {@code animate:early}.
 *
 * <p>Paired with {@link PoseDebugAnimateLateMixin} at the opposite priority so the
 * pair straddles EMF Compat's restore, which injects at this same point at the
 * default priority of 1000. One of the two runs before that restore and one after;
 * which is which is deliberately not asserted here — the trace prints them in the
 * order they actually fire, and that order is itself one of the unknowns being
 * measured.
 *
 * <p>The gap between the two samples is the restore's contribution. The gap between
 * {@code setupAnim} and the earlier of these is the CEM animation's contribution,
 * which is the number that says whether DetailedAnimations ran at all.
 */
@Mixin(value = EMFModelPartRoot.class, priority = 2000, remap = false)
public class PoseDebugAnimateEarlyMixin {

    @Inject(method = "animate", at = @At("RETURN"))
    private void mms$sampleAnimate(CallbackInfo ci) {
        PoseDebugEmf.sample("animate:early", (EMFModelPartRoot) (Object) this);
    }
}
