package info.mudbourn.mmscompat.mixin.cpa;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * CPA's ClientMannequinMixin declares `implements IAnimationControl` but never
 * implements three of the interface's methods — forceEnableBodyPart,
 * enableBodyPartAnimation and enableBodyPartAnimationInAllContainers. They stay
 * abstract on Mannequin, so anything reaching the enable path dies with
 * AbstractMethodError ("Ticking entity" -> forceEnableBodyPart). The disable
 * counterparts are all present; the enable half looks like it was simply missed
 * when the mannequin mixin was derived from AbstractClientPlayerEntityMixin.
 *
 * Only two animations reach that path — GenericHandSwingAnimation and
 * CrossbowAnimation, both of which call forceEnableBodyPart to re-enable an arm
 * they had disabled. Guard: report "don't play" whenever the context's avatar
 * isn't an actual Player, which stops CPA from dispatching playAnimation at all
 * (playAnimations() gates every call on shouldPlayAnimation). Mannequins lose
 * hand-swing and crossbow arm handling — they are stand-ins spawned by mods like
 * Bubblellaneous' camera monitor and Cinematic Respawn, so there is nothing to
 * animate. Players are untouched.
 *
 * Bonus: these animation objects are static singletons in AnimationProvider,
 * shared by every avatar, so a ticking mannequin was also stomping the local
 * player's swing state. Same reflective accessor rationale as
 * {@link CarryOnCompatAnimationMixin} — no compile-time dependency on CPA, and
 * we fall through to stock behavior if CPA reshapes AnimationContext.
 */
@Mixin(targets = {
        "com.github.razorplay01.cpa.platform.common.animation.animations.overlay.GenericHandSwingAnimation",
        "com.github.razorplay01.cpa.platform.common.animation.animations.overlay.CrossbowAnimation"
}, remap = false)
public abstract class ForceEnableArmAnimationMixin {

    @Unique private static volatile MethodHandle mmsCompat$playerAccessor;
    @Unique private static volatile boolean mmsCompat$accessorFailed;

    @Inject(method = "shouldPlayAnimation", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$skipNonPlayers(@Coerce Object context, CallbackInfoReturnable<Boolean> cir) {
        if (mmsCompat$accessorFailed || context == null) {
            return;
        }
        try {
            MethodHandle accessor = mmsCompat$playerAccessor;
            if (accessor == null) {
                accessor = MethodHandles.publicLookup().unreflect(context.getClass().getMethod("player"));
                mmsCompat$playerAccessor = accessor;
            }
            if (!(accessor.invoke(context) instanceof Player)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable t) {
            // CPA reshaped AnimationContext — stand down and leave it stock
            mmsCompat$accessorFailed = true;
        }
    }
}
