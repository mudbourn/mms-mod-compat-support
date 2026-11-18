package info.mudbourn.mmscompat.mixin.crawlondemand;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lets a crawl start only from a settled stance: on the ground, still, not crouching, not riding.
@Mixin(targets = "me.thecraftycoder.minecraft.crawlondemand.client.ClientCrawlHandler")
public abstract class ClientCrawlGateMixin {

    private static final double STILL_SPEED_SQR = 1.0E-3;

    @Inject(method = "crawl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$gateHoldCrawl(Player player, int action, CallbackInfo ci) {
        if (action == 1 && !mmsCompat$canStartCrawl(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "toggleCrawl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$gateToggleCrawl(Player player, CallbackInfo ci) {
        if (player.getPose() != Pose.SWIMMING && !mmsCompat$canStartCrawl(player)) {
            ci.cancel();
        }
    }

    private static boolean mmsCompat$canStartCrawl(Player player) {
        return player != null
            && player.onGround()
            && !player.isShiftKeyDown()
            && !player.isPassenger()
            && player.getDeltaMovement().horizontalDistanceSqr() < STILL_SPEED_SQR;
    }
}
