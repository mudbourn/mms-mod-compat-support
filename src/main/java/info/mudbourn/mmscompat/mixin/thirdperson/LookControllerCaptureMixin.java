package info.mudbourn.mmscompat.mixin.thirdperson;

import info.mudbourn.mmscompat.client.CrampedFirstPerson;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hands Leawind's camera look controller to CrampedFirstPerson, so forced first person can carry the camera's direction across.
@Pseudo
@Mixin(targets = "io.github.leawind.thirdperson.internal.logic.base.rotation.LookController", remap = false)
public abstract class LookControllerCaptureMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsCompat$capture(CallbackInfo ci) {
        CrampedFirstPerson.captureLook(this);
    }
}
