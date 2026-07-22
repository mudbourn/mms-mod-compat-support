package info.mudbourn.mmscompat.mixin.jeg;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses JEG's "[JEG] Commands" help text sent to players on join.
 */
@Mixin(targets = "ttv.migami.jeg.event.GunEvents", remap = false)
public class JegChatSpamMixin {

    @Inject(method = "sendAvailableCommands", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$suppressCommandHelp(ServerPlayer player, CallbackInfo ci) {
        ci.cancel();
    }
}
