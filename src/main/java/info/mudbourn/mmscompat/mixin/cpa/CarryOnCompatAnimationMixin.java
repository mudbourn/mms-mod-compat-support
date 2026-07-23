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
 * CPA's Carry On compat blind-casts AnimationContext#player() to Player, but
 * since 1.21.9 the accessor's static type is Avatar, which Mannequins also
 * extend — ticking a mannequin (e.g. the stand-in Cinematic Respawn poses
 * during its death cinematic) throws ClassCastException and crashes the
 * client ("Ticking entity" -> CarryOnCompatAnimation.shouldPlayAnimation).
 *
 * Guard: bail out (no carry animation) whenever the context's avatar isn't
 * an actual Player. The accessor is resolved reflectively so we need no
 * compile-time dependency on CPA; if CPA ever reshapes the record we fall
 * through to its original behavior unchanged.
 */
@Mixin(targets = "com.github.razorplay01.cpa.platform.common.animation.animations.compat.CarryOnCompatAnimation", remap = false)
public abstract class CarryOnCompatAnimationMixin {

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
