package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.mutantmonsters.world.entity.mutant.MutantZombie;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clears a mutant zombie's leftover death timer once it is alive again, so it does not run around lying down.
 */
@Mixin(MutantZombie.class)
public abstract class MutantZombieDeathPoseResetMixin {

    @Shadow
    private int vanishTime;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void mmsCompat$resetDeathPose(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.deathTime > 0 && !self.isDeadOrDying()) {
            self.deathTime = 0;
            this.vanishTime = 0;
        }
    }
}
