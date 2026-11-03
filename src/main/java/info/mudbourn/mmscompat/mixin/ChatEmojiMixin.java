package info.mudbourn.mmscompat.mixin;

import info.mudbourn.mmscompat.client.ChatEmoji;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Expands {@code :shortcode:} emoji in chat messages before they are sent.
 * Commands are left untouched.
 */
@Mixin(ChatScreen.class)
public class ChatEmojiMixin {

    @ModifyVariable(method = "handleChatInput", at = @At("HEAD"), argsOnly = true)
    private String mms$expandEmoji(String message) {
        return message.startsWith("/") ? message : ChatEmoji.replace(message);
    }
}
