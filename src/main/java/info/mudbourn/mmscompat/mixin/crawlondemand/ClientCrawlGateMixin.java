package info.mudbourn.mmscompat.mixin.crawlondemand;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restricts Crawl on Demand so a crawl can only begin from a settled stance.
 *
 * <p>The mod's {@code ClientCrawlHandler} otherwise lets the player drop prone
 * from any state its {@code onlyAllowCrawlingOnGround} option permits, so a
 * running or crouching player, or one on a mount, snaps flat mid-motion. This
 * gate cancels the two client entry points, {@code crawl} and
 * {@code toggleCrawl}, whenever the start conditions are not met.</p>
 *
 * <p>A crawl may only start when the player is on the ground, standing still,
 * not crouching, and not riding anything. Stopping an existing crawl is never
 * gated: a crawling player is already in {@link Pose#SWIMMING}, so a toggle in
 * that pose falls through to let them stand back up.</p>
 *
 * <p>{@code ClientCrawlHandler} is targeted by name because Crawl on Demand is
 * All Rights Reserved and must not be a compile dependency. Both method targets
 * are unique by name, so no descriptor is written.</p>
 */
@Mixin(targets = "me.talilon.minecraft.crawlondemand.client.ClientCrawlHandler")
public abstract class ClientCrawlGateMixin {

    /** Squared horizontal speed below which the player counts as standing still. */
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
