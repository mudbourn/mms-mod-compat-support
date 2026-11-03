package info.mudbourn.mmscompat.mixin.crawlondemand;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes going prone and standing up instant instead of a half-second tip.
 *
 * <p>The model's lean is driven by {@code swimAmount}, which vanilla ramps by 0.09
 * per tick. While the player is in {@link Pose#SWIMMING} out of water both ends of
 * the lerp are pinned to 1, and on leaving that pose they drop to 0, unless the
 * player crawled into water and is now really swimming.
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
