package info.mudbourn.mmscompat.mixin.crawlondemand;

import me.thecraftycoder.minecraft.crawlondemand.platform.CrawlStateManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lets a crawl start on the ground at up to walking pace, or in water or lava at any speed; stopping is never gated.
@Mixin(targets = "me.thecraftycoder.minecraft.crawlondemand.client.ClientCrawlHandler")
public abstract class ClientCrawlGateMixin {

    // Walking pace squared, in blocks per tick; anything up to a walk may drop into a crawl.
    private static final double WALK_SPEED_SQR = 0.06;

    @Inject(method = "crawl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$gateHoldCrawl(Player player, int action, CallbackInfo ci) {
        if (action == 1 && !mmsCompat$canStartCrawl(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "toggleCrawl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$gateToggleCrawl(Player player, CallbackInfo ci) {
        if (player != null && !CrawlStateManager.isCrawling(player) && !mmsCompat$canStartCrawl(player)) {
            ci.cancel();
        }
    }

    private static boolean mmsCompat$canStartCrawl(Player player) {
        if (player == null || player.isPassenger()) {
            return false;
        }
        if (player.isInWater() || player.isInLava()) {
            return true;
        }
        return player.onGround()
            && !player.isShiftKeyDown()
            && !player.isSprinting()
            && player.getDeltaMovement().horizontalDistanceSqr() < WALK_SPEED_SQR;
    }
}
