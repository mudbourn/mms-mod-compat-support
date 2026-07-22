package info.mudbourn.mmscompat.mixin.ar;

import goetic.mods.absolutrevive.common.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * AbsolutRevive mixin — announces when a player goes down.
 *
 * The mod messages on rescue but never on the down itself, so nobody knows a
 * teammate is bleeding out until they die (which vanilla DOES announce). This
 * broadcasts a chat line at the moment of going down, matching the death
 * notification the server already produces.
 *
 * handleFatalPlayerDamage returns true when it prevents death and puts the
 * player under. It can be called again while already unconscious (further fatal
 * hits), so the prior state is captured at HEAD and the broadcast only fires on
 * the false→true transition — one message per down, no spam.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveNotifyMixin {

    private static boolean mmsCompat$wasUnconscious = false;

    @Inject(method = "handleFatalPlayerDamage", at = @At("HEAD"))
    private static void mmsCompat$recordPriorState(Player player, CallbackInfoReturnable<Boolean> cir) {
        mmsCompat$wasUnconscious = EventHandler.isUnconscious(player);
    }

    @Inject(method = "handleFatalPlayerDamage", at = @At("RETURN"))
    private static void mmsCompat$notifyDowned(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (mmsCompat$wasUnconscious || !Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        Component msg = Component.translatable("mms_compat.absolutrevive.downed", player.getDisplayName())
            .withStyle(ChatFormatting.RED);
        server.getPlayerList().broadcastSystemMessage(msg, false);
    }
}
