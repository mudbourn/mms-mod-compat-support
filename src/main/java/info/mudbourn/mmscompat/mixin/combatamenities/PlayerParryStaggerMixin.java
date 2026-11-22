package info.mudbourn.mmscompat.mixin.combatamenities;

import info.mudbourn.mmscompat.ca.ParryStagger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A staggered player's attacks land nothing until the stagger wears off.
@Mixin(Player.class)
public abstract class PlayerParryStaggerMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$staggered(Entity target, CallbackInfo ci) {
        if (ParryStagger.staggered((Player) (Object) this)) {
            ci.cancel();
        }
    }
}
