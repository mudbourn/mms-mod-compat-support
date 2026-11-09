package info.mudbourn.mmscompat.mixin.apoli;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Drops a removed attacker before Apoli's setLastHurtByMob sync resolves it to null and crashes.
 */
@Mixin(LivingEntity.class)
public abstract class ApoliRemovedAttackerGuardMixin {

    @ModifyVariable(
            method = "setLastHurtByMob",
            at = @At("HEAD"),
            argsOnly = true
    )
    private LivingEntity mmsCompat$dropRemovedAttacker(LivingEntity attacker) {
        return attacker != null && attacker.isRemoved() ? null : attacker;
    }
}
