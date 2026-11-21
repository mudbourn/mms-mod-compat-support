package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.PerfectBlockCooldown;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Runs ahead of Combat Amenities' hurtServer head hook, which cancels the call on a parry,
// so a perfect block is seen before its shield cooldown goes on.
@Mixin(value = LivingEntity.class, priority = 900)
public abstract class PerfectBlockCooldownMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsCompat$notePerfectBlock(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player) {
            PerfectBlockCooldown.onHurt(player, source);
        }
    }
}
