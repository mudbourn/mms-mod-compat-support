package info.mudbourn.mmscompat.mixin.metrofix;

import info.mudbourn.mmscompat.MetroSpeedScale;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Headway control: keeps whole trains from convoying nose-to-tail.
 *
 * ModMetro's proximity braking only acts inside brake_distance and ramps to
 * a stop — it prevents collisions but not bunching: once two trains close up
 * at a station they cruise at identical speed and stay glued around the loop
 * forever ("circulating" as one clump). With the occupancy gate removed
 * (v0.5.3) nothing else re-opens the gap.
 *
 * Fix: while any cart of another train is 2–24 blocks ahead on roughly the
 * same heading, the following train's lead cart is capped at 85% of the
 * effective line speed. The train ahead runs at 100%, so the gap grows
 * ~0.6 b/s until the follower drops out of headway range — trains spread
 * back out around the loop on their own. The scan runs every 10 ticks per
 * lead cart, same cadence class as the mod's own logic.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroHeadwayMixin {

    @Unique
    private static final double HEADWAY_RANGE = 24.0;

    @Unique
    private int mmsCompat$headwayCooldown;

    @Unique
    private boolean mmsCompat$headwayCapped;

    @Inject(method = "applyProximityBraking", at = @At("TAIL"))
    private void mmsCompat$applyHeadway(Level world, CallbackInfo ci) {
        if (!(world instanceof ServerLevel sw)) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;

        if (--this.mmsCompat$headwayCooldown <= 0) {
            this.mmsCompat$headwayCooldown = 10;
            this.mmsCompat$headwayCapped = false;
            Vec3 vel = self.getDeltaMovement();
            double sp = vel.horizontalDistance();
            if (sp > 0.01) {
                double dirX = vel.x / sp;
                double dirZ = vel.z / sp;
                for (MetroCartEntity other : sw.getEntitiesOfClass(
                        MetroCartEntity.class,
                        self.getBoundingBox().inflate(HEADWAY_RANGE, 4.0, HEADWAY_RANGE))) {
                    if (other == self || self.getUUID().equals(other.getLeadCartUuid())) {
                        continue; // self or a follower of this very train
                    }
                    double ox = other.getX() - self.getX();
                    double oz = other.getZ() - self.getZ();
                    double forward = ox * dirX + oz * dirZ;
                    if (forward < 2.0 || forward > HEADWAY_RANGE) {
                        continue;
                    }
                    double lateral = Math.abs(dirX * oz - dirZ * ox);
                    if (lateral > 2.0 || Math.abs(other.getY() - self.getY()) > 1.5) {
                        continue; // parallel track or different level
                    }
                    this.mmsCompat$headwayCapped = true;
                    break;
                }
            }
        }

        if (this.mmsCompat$headwayCapped) {
            double cap = 0.85 * MetroSpeedScale.effectiveMaxSpeed(self);
            Vec3 vel = self.getDeltaMovement();
            double sp = vel.horizontalDistance();
            if (sp > cap) {
                self.setDeltaMovement(vel.x / sp * cap, vel.y, vel.z / sp * cap);
            }
        }
    }
}
