package info.mudbourn.mmscompat.mixin.chatpatches;

import info.mudbourn.mmscompat.client.ChatBoundaryReveal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat Patches mixin: notes when the join boundary line has just been added to chat.
 */
@Mixin(targets = "obro1961.chatpatches.config.Config")
public class ConfigBoundaryMixin {

    @Inject(
            method = "sendBoundaryLine",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V",
                    shift = At.Shift.AFTER))
    private void mmsCompat$markBoundary(CallbackInfo ci) {
        ChatBoundaryReveal.markSent();
    }
}
