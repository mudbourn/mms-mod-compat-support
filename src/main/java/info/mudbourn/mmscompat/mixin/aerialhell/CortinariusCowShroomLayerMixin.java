package info.mudbourn.mmscompat.mixin.aerialhell;

import fr.factionbedrock.aerialhell.Client.EntityModels.CortinariusCowShroomModel;
import fr.factionbedrock.aerialhell.Client.EntityRender.Layers.CortinariusCowShroomLayer;
import info.mudbourn.mmscompat.client.CemLayerPoseRelay;
import net.minecraft.client.model.EntityModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the Cortinarius Cow's mushrooms follow the Fresh Animations cow body.
 *
 * <p>{@code CortinariusCowShroomLayer} draws a standalone
 * {@code CortinariusCowShroomModel} and poses it with a hand-rolled copy of
 * vanilla's quadruped leg swing. Under Fresh Animations the cow beneath it is a
 * CEM model animated by EMF, so the mushrooms keep walking the vanilla walk on
 * top of a body doing something else entirely.
 *
 * <p>The shroom model mirrors the vanilla cow's part layout and pivots exactly,
 * so this is a straight pose relay; see
 * {@link CemLayerPoseRelay#CORTINARIUS_COW_SHROOMS} for how the differing leg
 * names were matched up.
 *
 * <p>Injected after the mod's {@code setupAnim} so the relayed pose lands on top
 * of it, and before the model is submitted.
 */
@Mixin(CortinariusCowShroomLayer.class)
public abstract class CortinariusCowShroomLayerMixin {

    @Shadow @Final private CortinariusCowShroomModel<?> cortinariusCowShroomModel;

    @Shadow protected abstract EntityModel<?> getParentModel();

    @Inject(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lfr/factionbedrock/aerialhell/Client/EntityModels/CortinariusCowShroomModel;"
                            + "setupAnim(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)V",
                    shift = At.Shift.AFTER))
    private void mms$relayCowPose(CallbackInfo ci) {
        CemLayerPoseRelay.relay(this.getParentModel(), this.cortinariusCowShroomModel, CemLayerPoseRelay.CORTINARIUS_COW_SHROOMS);
    }
}
