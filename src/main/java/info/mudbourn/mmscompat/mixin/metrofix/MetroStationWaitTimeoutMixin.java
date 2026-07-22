package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.modmetro.MetroCartEntity;

/**
 * Bounds ModMetro's station-occupancy wait.
 *
 * ModMetro's {@code tickLeadCart} refuses to depart while
 * {@code isNextStationOccupied} is true, and on refusal simply re-arms
 * {@code stationWaitTimer = 20} and returns. There is no give-up path, so a
 * cycle of trains each blocked by the next (A waits on B, B on C, C on A)
 * deadlocks permanently. The same unbounded wait also traps a cart behind a
 * stale occupancy registration that nothing ever clears.
 *
 * This is a ceiling, not an added delay. Normal blocking is untouched: a cart
 * blocked for 3s departs at 3s, the moment the station frees. Only a cart that
 * has been continuously blocked past {@link #WAIT_TIMEOUT_TICKS} — far longer
 * than any legitimate queue — stops believing the block and proceeds.
 *
 * Deliberately does not touch speed, braking, or the occupancy check itself.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroStationWaitTimeoutMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("mms_compat/metro-wait");

    /**
     * Longest a cart will believe "next station occupied" before departing anyway.
     *
     * 300 ticks (15s). Sized against legitimate blocking: a train ahead dwelling
     * ~6s, plus up to 1s of latency from ModMetro's hardcoded 20-tick recheck
     * cadence, plus room for a two-deep cascade (~13s worst case). Raise this if
     * dwell times go up; lower it only if you are certain queues stay shallow.
     */
    @Unique
    private static final long WAIT_TIMEOUT_TICKS = 300L;

    /** Game time when the current unbroken run of "occupied" began; -1 when not blocked. */
    @Unique
    private long mmsCompat$blockedSince = -1L;

    /**
     * ModMetro only calls this once per ~20 ticks while blocked (it re-arms the
     * wait timer on each refusal), so elapsed game time is used rather than a
     * call counter — the threshold stays meaningful regardless of call cadence.
     */
    @Inject(method = "isNextStationOccupied", at = @At("RETURN"), cancellable = true)
    private void mmsCompat$boundStationWait(Level world, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            // Clear, or never blocked — reset and let ModMetro proceed normally.
            this.mmsCompat$blockedSince = -1L;
            return;
        }

        long now = world.getGameTime();

        if (this.mmsCompat$blockedSince < 0L) {
            this.mmsCompat$blockedSince = now;
            return;
        }

        long blockedFor = now - this.mmsCompat$blockedSince;
        if (blockedFor >= WAIT_TIMEOUT_TICKS) {
            MetroCartEntity self = (MetroCartEntity) (Object) this;
            LOGGER.info(
                "[metro-wait] Cart {} blocked {}s waiting on station '{}' — timeout reached, departing anyway.",
                self.getUUID(), blockedFor / 20L, self.getNextStation()
            );
            this.mmsCompat$blockedSince = -1L;
            cir.setReturnValue(false);
        }
    }
}
