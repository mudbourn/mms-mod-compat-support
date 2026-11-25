package info.mudbourn.mmscompat.mixin.crawlondemand;

import me.thecraftycoder.minecraft.crawlondemand.platform.CrawlStateManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import me.thecraftycoder.minecraft.crawlondemand.client.CrawlHoldInputState;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Lets a crawl start on the ground at up to walking pace, or in water or lava at any speed; stopping is never gated, and releasing the hold key always stands up.
// Holding the key while too fast waits and drops into the crawl once the player slows down, instead of needing a re-tap.
@Mixin(targets = "me.thecraftycoder.minecraft.crawlondemand.client.ClientCrawlHandler")
public abstract class ClientCrawlGateMixin {

    // Walking pace squared, in blocks per tick; anything up to a walk may drop into a crawl.
    private static final double WALK_SPEED_SQR = 0.06;

    // True when the hold key started the current crawl; release only stands up while it is set.
    @Shadow(remap = false) private static boolean holdCrawlActive;
    @Shadow(remap = false) private static CrawlHoldInputState holdInputState;
    @Shadow(remap = false) private static Player player;

    @Shadow(remap = false)
    private static void crawl(Player player, int action) {
        throw new AssertionError();
    }

    // Set when a held press was refused for speed; retried each tick until the crawl starts or the key is let go.
    @Unique private static boolean mmsCompat$pendingHoldCrawl;

    @Inject(method = "crawl", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mmsCompat$gateHoldCrawl(Player player, int action, CallbackInfo ci) {
        if (action == 1 && !mmsCompat$canStartCrawl(player)) {
            mmsCompat$pendingHoldCrawl = player != null;
            ci.cancel();
        } else if (action == 1) {
            mmsCompat$pendingHoldCrawl = false;
        } else if (action == 0) {
            mmsCompat$pendingHoldCrawl = false;
        }
        if (action == 0 && player != null && CrawlStateManager.isCrawling(player)) {
            // The flag is lost on a player reset (form swap, respawn, dimension change) or a press made while
            // already crawling, which left release doing nothing; releasing the key always stands up.
            holdCrawlActive = true;
        }
    }

    @Inject(method = "pollKeyMappings", at = @At("TAIL"), remap = false)
    private static void mmsCompat$retryHoldCrawl(KeyMapping toggleKey, KeyMapping holdKey, CallbackInfo ci) {
        if (!mmsCompat$pendingHoldCrawl) {
            return;
        }
        if (!holdKey.isDown() || player == null) {
            mmsCompat$pendingHoldCrawl = false;
        } else if (mmsCompat$canStartCrawl(player)) {
            crawl(player, 1);
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
