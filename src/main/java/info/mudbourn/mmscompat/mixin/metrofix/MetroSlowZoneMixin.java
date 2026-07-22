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
 * speed-ramp marker.
 *
 * The ramp is LINEAR in configured line speed (MetroConfig.speed): each
 * step adds/removes 15% OF TOP SPEED, one step every 10 ticks, 5 steps.
 * Cap sequence going down: 100% -> 85 -> 70 -> 55 -> 40 -> 25% (floor),
 * then holds so the train gracefully lands at the next stop. Departing
 * the stop climbs the same stairs back to 100%, after which the cap is
 * dropped and ModMetro's own acceleration (+10%/tick) rules as usual.
 *
 * When a LEAD cart passes over a marker (1-2 blocks beneath the rail,
 * same scan depth and sub-stepped path scan ModMetro uses for station
 * blocks, so markers register at any speed), the down-ramp starts.
 * Followers pace off the lead cart's velocity, so the whole train ramps
 * as one.
 *
 * The up-ramp arms when the train is waiting at a station OR simply
 * stopped (belt and suspenders: ModMetro's waiting flag is synced a tick
 * late on arrival and an early return in the occupancy re-check can skip
 * our TAIL injection, which previously left the cap stuck low forever).
 *
 * State is transient (not saved to NBT): a chunk reload mid-ramp drops
 * the restriction until the next marker.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroSlowZoneMixin {

    @Unique
    private static final double STEP_FRACTION = 0.15; // of top speed, per step
    @Unique
    private static final int RAMP_STEPS = 5;          // floor = 1 - 5*0.15 = 25%
    @Unique
    private static final int TICKS_PER_STEP = 10;
    @Unique
    private static final double STOPPED_EPSILON = 0.01;

    /** Ramp phases: NONE -> DOWN -> HOLD -> (at station) ARMED_UP -> UP -> NONE */
    @Unique
    private static final int PHASE_NONE = 0;
    @Unique
    private static final int PHASE_DOWN = 1;
    @Unique
    private static final int PHASE_HOLD = 2;
    @Unique
    private static final int PHASE_ARMED_UP = 3;
    @Unique
    private static final int PHASE_UP = 4;

    @Unique
    private int mmsCompat$rampPhase = PHASE_NONE;
    /** Steps currently subtracted from the cap: 0 (full speed) .. RAMP_STEPS (floor). */
    @Unique
    private int mmsCompat$rampSteps = 0;
    @Unique
    private int mmsCompat$rampTicks = 0;

    @Inject(method = "tickLeadCart", at = @At("TAIL"))
    private void mmsCompat$slowZone(Level world, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        Vec3 vel = self.getDeltaMovement();
        double speed = vel.horizontalDistance();

        boolean atRest = self.isWaitingAtStation() || speed < STOPPED_EPSILON;

        if (this.mmsCompat$rampPhase == PHASE_DOWN || this.mmsCompat$rampPhase == PHASE_HOLD) {
            if (atRest) {
                // Landed at a stop: arm the inverse ramp for departure. Steps
                // stay where the down-ramp left them so the train pulls out
                // slowly rather than lurching to full speed.
                this.mmsCompat$rampPhase = PHASE_ARMED_UP;
                this.mmsCompat$rampTicks = 0;
            }
        } else if (this.mmsCompat$rampPhase == PHASE_ARMED_UP) {
            if (!atRest) {
                // Permitted to move again: climb back up.
                this.mmsCompat$rampPhase = PHASE_UP;
                this.mmsCompat$rampTicks = 0;
            }
        } else if (this.mmsCompat$rampPhase == PHASE_NONE) {
            // Sub-step along the path covered this tick — ModMetro's station
            // scan does exactly this, which is why stations never get skipped
            // at speed. One check per block of travel, prev position -> now.
            Vec3 now = new Vec3(self.getX(), self.getY(), self.getZ());
            Vec3 prev = now.subtract(vel);
            int steps = Math.max(1, (int) Math.ceil(speed));

            outer:
            for (int i = 0; i <= steps; i++) {
                double fraction = steps == 1 ? 1.0 : (double) i / steps;
                BlockPos checkPos = BlockPos.containing(prev.lerp(now, fraction));
                for (int y = 1; y <= 2; y++) {
                    if (world.getBlockState(checkPos.below(y)).getBlock() == MetroMod.METRO_MODEL_BLOCK) {
                        this.mmsCompat$rampPhase = PHASE_DOWN;
                        this.mmsCompat$rampSteps = 0;
                        this.mmsCompat$rampTicks = 0;
                        break outer;
                    }
                }
            }
        }

        // Advance the active ramp one step every TICKS_PER_STEP ticks.
        if (this.mmsCompat$rampPhase == PHASE_DOWN) {
            if (++this.mmsCompat$rampTicks >= TICKS_PER_STEP) {
                this.mmsCompat$rampTicks = 0;
                if (++this.mmsCompat$rampSteps >= RAMP_STEPS) {
                    this.mmsCompat$rampSteps = RAMP_STEPS;
                    this.mmsCompat$rampPhase = PHASE_HOLD;
                }
            }
        } else if (this.mmsCompat$rampPhase == PHASE_UP) {
            if (++this.mmsCompat$rampTicks >= TICKS_PER_STEP) {
                this.mmsCompat$rampTicks = 0;
                if (--this.mmsCompat$rampSteps <= 0) {
                    // Fully unwound: drop the cap, back to normal running.
                    this.mmsCompat$rampSteps = 0;
                    this.mmsCompat$rampPhase = PHASE_NONE;
                }
            }
        }

        // Enforce the current cap: top speed minus 15% of top per step.
        if (this.mmsCompat$rampPhase != PHASE_NONE && this.mmsCompat$rampSteps > 0) {
            double cap = MetroConfig.speed * (1.0 - STEP_FRACTION * this.mmsCompat$rampSteps);
            if (speed > cap) {
                double s = cap / speed;
                self.setDeltaMovement(vel.x * s, vel.y, vel.z * s);
            }
        }
    }
}
