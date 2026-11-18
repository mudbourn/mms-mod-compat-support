package info.mudbourn.mmscompat.mixin.mapsync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmscompat.mapsync.MapSyncChat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Drops MapSyncer's "Map is up-to-date" chat line sent on every login sync.
@Mixin(targets = "com.mapsyncer.server.ServerSyncHandlerLogic", remap = false)
public class UpToDateChatMuteMixin {

    @WrapOperation(
        method = "/lambda\\$processSyncAsync\\$\\d+.*/",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
        )
    )
    private static void mmsCompat$muteUpToDate(ServerPlayer player, Component message, Operation<Void> original) {
        if (!MapSyncChat.hasKey(message, "mapsyncer.server.map_uptodate")) {
            original.call(player, message);
        }
    }
}
