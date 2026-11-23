package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.ParryStreak;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// During a parry streak no cooldown goes on a held shield or blocking weapon, on either side, so
// lowering the guard (Combat Amenities, LoweredGuardCooldownMixin) never stops the next raise.
@Mixin(ItemCooldowns.class)
public abstract class ParryStreakCooldownMixin {

    @Inject(method = "addCooldown(Lnet/minecraft/resources/Identifier;I)V", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$noGuardCooldownInStreak(Identifier group, int ticks, CallbackInfo ci) {
        Player owner = (Object) this instanceof ServerItemCooldowns server
            ? ((ServerItemCooldownsAccessor) server).mmsCompat$player()
            : ParryStreak.clientPlayer();
        if (owner != null && ParryStreak.active(owner) && ParryStreak.isGuardGroup(owner, group)) {
            ci.cancel();
        }
    }
}
