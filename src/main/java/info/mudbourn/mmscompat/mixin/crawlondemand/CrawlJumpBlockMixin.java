package info.mudbourn.mmscompat.mixin.crawlondemand;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Stops a crawling player from jumping
@Mixin(LivingEntity.class)
public abstract class CrawlJumpBlockMixin {

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$blockCrawlJump(CallbackInfo ci) {
        if ((Object) this instanceof Player player
            && player.getPose() == Pose.SWIMMING
            && !player.isInWater()) {
            ci.cancel();
        }
    }
}
