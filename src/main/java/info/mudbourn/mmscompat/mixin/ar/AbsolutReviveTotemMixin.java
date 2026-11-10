package info.mudbourn.mmscompat.mixin.ar;

import info.mudbourn.mmscompat.revive.HeldTotem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a totem save the player before AbsolutRevive downs them.
 *
 * AbsolutRevive downs a player from Fabric's ALLOW_DEATH, which runs before
 * the vanilla totem check. A totem the check then pops (a trinket slot one
 * through Friends and Foes) leaves the player both downed and dying, and the
 * vanilla death screen lands on top of the revive screen. Declining here lets
 * the totem fire; AbsolutRevive's own post-totem hook downs the player if the
 * totem did not save them.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveTotemMixin {

    @Inject(method = "handleFatalPlayerDamage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$deferToTotem(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (HeldTotem.has(player)) {
            cir.setReturnValue(false);
        }
    }
}
