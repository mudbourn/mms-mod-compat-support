package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.ParryStagger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// A staggered mob's melee swing does nothing until the stagger wears off.
@Mixin(Mob.class)
public abstract class ParryStaggerMixin {

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$staggered(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (ParryStagger.staggered((Mob) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
