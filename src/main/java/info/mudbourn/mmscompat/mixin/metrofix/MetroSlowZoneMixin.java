package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.MetroMod;
import com.example.modmetro.config.MetroConfig;

/**
 * Gives ModMetro's otherwise functionless metro_model_block a job: a
 * speed-restriction marker.
 *
 * When a LEAD cart passes over one (1-2 blocks beneath the rail, the same
 * scan depth ModMetro uses for station blocks), the train's speed is capped
 * at 25% of the configured line speed. The cap lifts the moment the train
 * pulls into its next station. Followers pace off the lead cart's velocity,
 * so the whole train slows as one.
 *
 * The flag is transient (not saved to NBT): a chunk reload mid-zone drops
 * the restriction until the next marker. Markers are cheap — place one
 * after any junction/curve you want taken slowly and the next station
 * resets the line to full speed.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroSlowZoneMixin {

    @Unique
    private static final double SLOW_FACTOR = 0.25;

    @Unique
    private boolean mmsCompat$speedRestricted = false;

    @Inject(method = "tickLeadCart", at = @At("TAIL"))
    private void mmsCompat$slowZone(Level world, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;

        if (self.isWaitingAtStation()) {
            // Landed at a stop: restriction ends.
            this.mmsCompat$speedRestricted = false;
            return;
        }

        if (!this.mmsCompat$speedRestricted) {
            BlockPos pos = self.blockPosition();
            for (int y = 1; y <= 2; y++) {
                if (world.getBlockState(pos.below(y)).getBlock() == MetroMod.METRO_MODEL_BLOCK) {
                    this.mmsCompat$speedRestricted = true;
                    break;
                }
            }
        }

        if (this.mmsCompat$speedRestricted) {
            double cap = MetroConfig.speed * SLOW_FACTOR;
            Vec3 vel = self.getDeltaMovement();
            double speed = vel.horizontalDistance();
            if (speed > cap) {
                double s = cap / speed;
                self.setDeltaMovement(vel.x * s, vel.y, vel.z * s);
            }
        }
    }
}
