package info.mudbourn.mmscompat.mixin.chatpatches;

import info.mudbourn.mmscompat.client.ChatBoundaryReveal;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat Patches mixin: keeps the join boundary line out of the restored-history hide so it shows on the HUD.
 */
@Mixin(targets = "obro1961.chatpatches.ChatLog")
public class ChatLogBoundaryMixin {

    @Inject(method = "hideRecentMessages", at = @At("TAIL"))
    private static void mmsCompat$keepBoundary(CallbackInfo ci) {
        ChatComponentLinesAccessor chat = (ChatComponentLinesAccessor) Minecraft.getInstance().gui.getChat();
        ChatBoundaryReveal.capture(chat.mmsCompat$trimmedMessages());
    }
}
