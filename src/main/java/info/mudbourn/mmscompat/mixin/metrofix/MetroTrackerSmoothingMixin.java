package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Sends metro cart position updates every tick instead of every 3.
 *
 * Metro carts register with the default MISC entity tracker settings —
 * position/velocity packets every 3 ticks, interpolated client-side. At
 * line speed that's most of a block between updates, which reads as the
 * carts jittering and rubber-banding against each other, worst inside a
 * train where neighboring cars get their updates on different ticks.
 *
 * ServerEntity's update cadence is per-tracked-entity state, so this is a
 * surgical bump for metro carts only; everything else keeps vanilla cadence.
 * Cost: ~3x move packets for the handful of carts in tracking range.
 */
@Mixin(ServerEntity.class)
public abstract class MetroTrackerSmoothingMixin {

    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    @Mutable
    private int updateInterval;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsCompat$fastMetroCartUpdates(CallbackInfo ci) {
        if (this.entity instanceof MetroCartEntity) {
            this.updateInterval = 1;
        }
    }
}
