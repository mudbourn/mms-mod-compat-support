package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.mixin.chatpatches.ChatComponentLinesAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Shows Chat Patches' join boundary line on the HUD once the player is in the world.
 */
public final class ChatBoundaryReveal {

    private static final Set<GuiMessage.Line> PENDING = Collections.newSetFromMap(new IdentityHashMap<>());

    private static boolean sent;

    private ChatBoundaryReveal() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ChatBoundaryReveal::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PENDING.clear());
    }

    public static void markSent() {
        sent = true;
    }

    public static void capture(List<GuiMessage.Line> lines) {
        PENDING.clear();
        boolean fresh = sent;
        sent = false;
        if (!fresh || lines.isEmpty()) {
            return;
        }
        PENDING.add(lines.getFirst());
        for (int i = 1; i < lines.size() && !lines.get(i).endOfEntry(); i++) {
            PENDING.add(lines.get(i));
        }
    }

    private static void tick(Minecraft client) {
        if (PENDING.isEmpty() || client.player == null || client.gui.screen() != null) {
            return;
        }
        List<GuiMessage.Line> lines = ((ChatComponentLinesAccessor) client.gui.hud.getChat()).mmsCompat$trimmedMessages();
        int now = client.gui.hud.getGuiTicks();
        for (int i = 0; i < lines.size(); i++) {
            GuiMessage.Line line = lines.get(i);
            if (PENDING.contains(line)) {
                GuiMessage parent = line.parent();
                GuiMessage fresh = new GuiMessage(now, parent.content(), parent.signature(), parent.source(), parent.tag());
                lines.set(i, new GuiMessage.Line(fresh, line.content(), line.endOfEntry()));
            }
        }
        PENDING.clear();
    }
}
