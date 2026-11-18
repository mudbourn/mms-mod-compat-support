package info.mudbourn.mmscompat.mixin.mapsync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmscompat.mapsync.MapSyncChat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Drops MapSyncer's auto-sync status, start and complete chat lines; manual sync feedback and the timeout error stay.
@Mixin(targets = "com.mapsyncer.client.MapPacketHandler", remap = false)
public class AutoSyncChatMuteMixin {

    @WrapOperation(
        method = "/lambda\\$(registerHandlers|handleSyncResponse)\\$\\d+.*/",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
        )
    )
    private static void mmsCompat$muteAutoSync(LocalPlayer player, Component message, Operation<Void> original) {
        if (!MapSyncChat.hasKey(message, "mapsyncer.autosync.")) {
            original.call(player, message);
        }
    }
}
