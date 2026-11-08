package info.mudbourn.mmscompat.mixin.chatpatches;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the chat's display line list.
 */
@Mixin(ChatComponent.class)
public interface ChatComponentLinesAccessor {

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> mmsCompat$trimmedMessages();
}
