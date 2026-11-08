package info.mudbourn.mmscompat.mixin.mapsync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Drops MapSyncer's login and auto-sync chat lines: the "Auto-sync: Daily"
 * status on join, "Auto-syncing maps...", and "Auto-sync complete". Manual
 * sync feedback and the sync timeout error still reach chat.
 */
@Mixin(targets = "com.mapsyncer.client.MapPacketHandler", remap = false)
public class AutoSyncChatMuteMixin {

    @WrapOperation(
        method = {
            "lambda$registerHandlers$3",
            "lambda$registerHandlers$1"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V",
            remap = true
        )
    )
    private static void mmsCompat$muteAutoSyncStatus(LocalPlayer player, Component message, boolean overlay, Operation<Void> original) {
    }

    @WrapOperation(
        method = "lambda$handleSyncResponse$7",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V",
            ordinal = 1,
            remap = true
        )
    )
    private static void mmsCompat$muteAutoSyncComplete(LocalPlayer player, Component message, boolean overlay, Operation<Void> original) {
    }
}
