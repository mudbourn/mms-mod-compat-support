package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.UnparryableDamage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Marks damage with no attacker just before Combat Amenities' hurtServer head hook.
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class UnparryableMarkMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsCompat$markUnparryable(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        UnparryableDamage.mark((LivingEntity) (Object) this, source);
    }
}
