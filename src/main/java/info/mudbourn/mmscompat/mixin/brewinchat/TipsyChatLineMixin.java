package info.mudbourn.mmscompat.mixin.brewinchat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import info.mudbourn.mmscompat.client.TipsyChatLine;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.fabric.client.BrewinAndChewinFabricClient;

/**
 * Brewin' And Chewin' adds drunk chat lines straight to the chat component,
 * skipping the vanilla line shape that Chat Patches parses and the hook Chat
 * Heads reads the sender from. This rebuilds the line before it is added.
 */
@Mixin(BrewinAndChewinFabricClient.class)
public class TipsyChatLineMixin {

    @WrapOperation(
        method = "lambda$onInitializeClient$8",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
        )
    )
    private static void mms$rebuildTipsyLine(
        ChatComponent chat,
        Component line,
        MessageSignature signature,
        GuiMessageTag tag,
        Operation<Void> original,
        @Local(argsOnly = true) PlayerChatMessage message,
        @Local(argsOnly = true) ChatType.Bound bound
    ) {
        original.call(chat, TipsyChatLine.rebuild(line, bound, message), signature, tag);
    }
}
