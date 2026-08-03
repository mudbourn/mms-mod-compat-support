package info.mudbourn.mmscompat.mixin.spyglassfov;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restores the spyglass zoom that Leawind's Third Person suppresses.
 *
 * <h2>The bug</h2>
 *
 * <p>Vanilla returns a hard {@code 0.1F} FOV multiplier while scoping, but only
 * under {@code isFirstPerson && isScoping()}, and the {@code isFirstPerson}
 * argument comes from {@code options.getCameraType().isFirstPerson()}.
 *
 * <p>Leawind's {@code CameraTypeMixin} overwrites that method for every caller in
 * the game:
 *
 * <pre>  return this.firstPerson ^ ThirdPersonStatus.isPerspectiveInverted;</pre>
 *
 * <p>and {@code CameraAgent#checkGameStatus} recomputes that flag every frame while
 * the camera type is {@code FIRST_PERSON}:
 *
 * <pre>  isPerspectiveInverted = smoothDistance.get() &gt; 0.05;</pre>
 *
 * <p>So whenever Leawind's smoothed camera distance has not collapsed to the head,
 * {@code CameraType.FIRST_PERSON.isFirstPerson()} answers {@code false}, vanilla
 * concludes it is not in first person, and the scope multiplier is skipped. What is
 * left is the ordinary movement-speed FOV drift, which reads as a spyglass that
 * barely zooms at all.
 *
 * <h2>Why this seam</h2>
 *
 * <p>Clearing the flag does not work — it is not stale state but a live per-frame
 * output of Leawind's camera smoother, so any external write is overwritten before
 * the next render. Contesting {@code isFirstPerson} itself is worse: that is
 * Leawind's own injection, and the winner would depend on load order.
 *
 * <p>This asks the question the poisoned method was standing in for, directly:
 * {@code options.getCameraType() == CameraType.FIRST_PERSON} compares the enum
 * constant, and Leawind mixes into the {@code isFirstPerson} <em>method</em>, not
 * the identity of the constant. BadOptimizations reaches for the same comparison
 * in its own FOV wrapper for the same reason.
 *
 * <p>Injected at {@code RETURN} and only ever forcing the value vanilla itself
 * would have produced, so with Leawind absent — or its camera settled, where the
 * flag is {@code false} and vanilla already returns {@code 0.1F} — this changes
 * nothing. It restores a vanilla behaviour rather than adding one, which is also
 * why it does not need to care whether the scope overlay renders.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class SpyglassFovMixin {

    /** Vanilla's scope multiplier, from {@code AbstractClientPlayer#getFieldOfViewModifier}. */
    private static final float SCOPE = 0.1f;

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void mms$restoreSpyglassZoom(boolean isFirstPerson, float fovScale,
                                         CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValueF() == SCOPE) {
            // Vanilla already agreed it is scoping; nothing to repair.
            return;
        }

        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null || minecraft.getCameraEntity() != self) {
            return;
        }

        // The enum identity, deliberately — see the class note. isFirstPerson is the
        // argument that has already been poisoned by the time it arrives here.
        if (minecraft.options.getCameraType() == CameraType.FIRST_PERSON && self.isScoping()) {
            cir.setReturnValue(SCOPE);
        }
    }
}
