package info.mudbourn.mmscompat.client;

import dzwdz.chat_heads.ChatHeads;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.util.List;

/**
 * Rebuilds the drunk chat line Brewin' And Chewin' adds by hand into the
 * vanilla {@code <name> text} shape, then hands it to Chat Heads with the
 * real sender, as the vanilla chat path would.
 */
public final class TipsyChatLine {

    private static final boolean CHAT_HEADS = FabricLoader.getInstance().isModLoaded("chat_heads");

    private TipsyChatLine() {}

    public static Component rebuild(Component line, ChatType.Bound bound, PlayerChatMessage message) {
        List<Component> parts = line.getSiblings();
        if (parts.isEmpty()) {
            return line;
        }

        Component rebuilt = bound.decorate(parts.getLast());
        return CHAT_HEADS ? ChatHeadsHandoff.apply(rebuilt, message) : rebuilt;
    }

    private static final class ChatHeadsHandoff {

        static Component apply(Component line, PlayerChatMessage message) {
            return ChatHeads.handleAddedMessage(line, ChatHeads.getOwner(message));
        }
    }
}
