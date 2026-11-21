package info.mudbourn.mmscompat.mixin.crawlondemand;

import me.thecraftycoder.minecraft.crawlondemand.platform.CrawlStateManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Keeps a Crawl on Demand crawl going in water one block deep.
@Mixin(Player.class)
public abstract class CrawlShallowWaterMixin {

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$crawlInShallowWater(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.isInWater()
            && !player.isSwimming()
            && !player.level().getFluidState(player.blockPosition().above()).is(FluidTags.WATER)
            && CrawlStateManager.ensureValidState(player)) {
            player.setPose(Pose.SWIMMING);
            ci.cancel();
        }
    }
}
