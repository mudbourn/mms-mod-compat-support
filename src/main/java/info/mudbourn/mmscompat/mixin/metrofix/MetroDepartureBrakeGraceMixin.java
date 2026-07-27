package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.mudbourn.mmscompat.metro.MetroTuning;

import com.example.modmetro.MetroCartEntity;

/**
 * Task C: after a station departure, {@code tickLeadCart}'s 1.1x ramp and
 * {@code applyProximityBraking}'s distance-based brakeFactor both run every
 * tick with no coordination between them. A genuinely different train sitting
 * within {@code MetroConfig.brake_distance} on connected rail — common right
 * after departure, when the previous occupant of the block ahead hasn't
 * cleared yet — brakes the follower back down every tick the ramp pushes it
 * up, and the two can settle at a stable sub-top-speed equilibrium that never
 * resolves on its own (closing distance never grows once brake and ramp
 * balance).
 *
 * Fix: suppress {@code applyProximityBraking} entirely for
 * {@link MetroTuning#departure_brake_grace_ticks} ticks after a lead cart
 * leaves a station, so the ramp gets an uncontested run back to line speed.
 * Proximity braking still applies immediately once the grace window elapses,
 * so a train departing into real, close traffic still brakes for it — this
 * only removes the fight during the ramp itself.
 *
 * Departure is detected by edge-triggering {@code isWaitingAtStation()}
 * (true -> false) at RETURN of tickLeadCart, mirroring the multi-exit
 * reasoning documented on {@link MetroSlowZoneMixin} — TAIL only covers the
 * last of several return paths, RETURN covers all of them and the handler is
 * idempotent per tick.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroDepartureBrakeGraceMixin {

    @Unique
    private boolean mmsCompat$wasWaiting = false;

    @Unique
    private int mmsCompat$departureGraceTicks = 0;

    @Inject(method = "tickLeadCart", at = @At("RETURN"))
    private void mmsCompat$trackDeparture(Level world, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        boolean waiting = self.isWaitingAtStation();
        if (this.mmsCompat$wasWaiting && !waiting) {
            this.mmsCompat$departureGraceTicks = MetroTuning.departure_brake_grace_ticks;
        }
        this.mmsCompat$wasWaiting = waiting;
        if (this.mmsCompat$departureGraceTicks > 0) {
            this.mmsCompat$departureGraceTicks--;
        }
    }

    @Inject(method = "applyProximityBraking", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$suppressDuringGrace(Level world, CallbackInfo ci) {
        if (this.mmsCompat$departureGraceTicks > 0) {
            ci.cancel();
        }
    }
}
