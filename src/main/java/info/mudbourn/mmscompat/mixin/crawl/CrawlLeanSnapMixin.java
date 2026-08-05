package info.mudbourn.mmscompat.mixin.crawl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes going prone instant instead of easing in over half a second.
 *
 * <h2>The behaviour</h2>
 *
 * <p>Crawl on Demand snaps the player's pose straight to {@code SWIMMING} the frame the
 * key is pressed, so the hitbox and eye height change immediately. The <em>model</em>
 * does not follow, because the rotation from upright to horizontal is not driven by the
 * pose at all — it is driven by {@code LivingEntity.swimAmount}, a separate scalar that
 * {@link LivingEntity} ramps in {@code updateSwimAmount}:
 *
 * <pre>
 *   swimAmountO = swimAmount;
 *   swimAmount = isVisuallySwimming() ? min(1, swimAmount + 0.09) : max(0, swimAmount - 0.09);
 * </pre>
 *
 * <p>0.09 per tick is roughly eleven ticks, a little over half a second, and the renderer
 * lerps across it — so the player visibly tips forward into the crawl long after the
 * hitbox already changed. That ramp is the "slow ease", and no amount of work on the mod
 * side reaches it, because the mod never touches this field.
 *
 * <h2>The fix</h2>
 *
 * <p>While prone, write both ends of the lerp to 1 and cancel — {@code swimAmountO}
 * matters as much as {@code swimAmount}, since leaving it behind means the renderer
 * interpolates from the old value and the snap becomes a one-tick ease instead of none.
 *
 * <p>"Prone" is deliberately read off vanilla state — {@code SWIMMING} pose while not in
 * water — rather than from Crawl on Demand's own crawl flag. That state is exactly what
 * the mod produces, it needs no compile-time or runtime dependency on an
 * All-Rights-Reserved jar, and it covers vanilla's own crawl (squeezing under a
 * one-block gap) for free, which is the same visual situation and wants the same snap.
 *
 * <h2>Why the exit path is asymmetric</h2>
 *
 * <p>Standing up snaps to 0 the same way, but only when the player is leaving the pose
 * entirely. Crawling forward into deep water hands off to real swimming, which shares
 * this field: snapping there would drop the lean to 0 and make vanilla ramp it back up
 * from scratch, a visible hitch on a transition that should be seamless. The
 * {@code wasProne} latch exists solely to distinguish those two exits — without it this
 * mixin would also snap the lean-out of ordinary swimming, which was explicitly not
 * wanted.
 */
@Mixin(LivingEntity.class)
public abstract class CrawlLeanSnapMixin {

    @Shadow private float swimAmount;
    @Shadow private float swimAmountO;

    @Unique private boolean mms$wasProne = false;

    @Inject(method = "updateSwimAmount", at = @At("HEAD"), cancellable = true)
    private void mms$snapCrawlLean(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.hasPose(Pose.SWIMMING) && !self.isInWater()) {
            this.swimAmountO = 1.0F;
            this.swimAmount = 1.0F;
            this.mms$wasProne = true;
            ci.cancel();
            return;
        }

        if (this.mms$wasProne) {
            this.mms$wasProne = false;
            if (!self.isVisuallySwimming()) {
                this.swimAmountO = 0.0F;
                this.swimAmount = 0.0F;
                ci.cancel();
            }
        }
    }
}
