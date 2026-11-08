package info.mudbourn.mmscompat.mixin.mapsync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops MapSyncer's "Map is up-to-date" chat line, sent on every login sync
 * where nothing changed. The client still receives the uptodate status payload.
 */
@Mixin(targets = "com.mapsyncer.server.ServerSyncHandlerLogic", remap = false)
public class UpToDateChatMuteMixin {

    @WrapOperation(
        method = "lambda$processSyncAsync$13",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V",
            remap = true
        )
    )
    private static void mmsCompat$muteUpToDate(ServerPlayer player, Component message, Operation<Void> original) {
    }
}
