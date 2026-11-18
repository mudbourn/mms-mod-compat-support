package info.mudbourn.mmscompat.mixin.crawlondemand;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Lets a crawling player step up a full block
@Mixin(LivingEntity.class)
public abstract class CrawlStepUpMixin {

    /** Step height granted while crawling. */
    private static final float CRAWL_STEP_HEIGHT = 1.0F;

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void mmsCompat$crawlStepUp(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Player player
            && player.getPose() == Pose.SWIMMING
            && !player.isInWater()
            && cir.getReturnValueF() < CRAWL_STEP_HEIGHT) {
            cir.setReturnValue(CRAWL_STEP_HEIGHT);
        }
    }
}
