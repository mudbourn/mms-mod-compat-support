package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.mudbourn.mmscompat.metro.MetroCruiseConfig;

import com.example.modmetro.MetroCartEntity;

/**
 * Cruise zones: a stretch of track that holds carts at a switching-safe speed
 * for as long as they are on it.
 *
 * The problem it solves is a speed/tick-rate one, not a logic one. The MMS line
 * runs at {@code speed = 3.4} blocks per tick, so a cart advances more than
 * three blocks between ticks and can land clean PAST a one-block detector rail
 * without ever occupying it. The detector never fires, the switch never throws,
 * and the train darts through the junction on whatever the rail happened to be
 * set to. Below 1.0 b/t skipping a block is impossible.
 *
 * Why the existing slow-zone markers could not do this
 * ----------------------------------------------------
 * {@link MetroSlowZoneMixin} is EDGE-triggered: a marker starts a staged ramp,
 * and after leaving a stop it blacks markers out for 40 ticks so a reversing
 * train does not re-trigger its own approach. At 3.4 b/t that blackout spans
 * ~136 blocks — with stations 140-260 blocks apart, essentially the whole
 * corridor out of a terminal ignores markers outright. A solid row of them
 * changes nothing, because none of them are counted.
 *
 * This governor is LEVEL-triggered instead: every tick, every cart, no
 * debounce, no blackout, no state to get stuck in. If the cart is over a marker
 * it is clamped, and if it is not, it is not.
 *
 * Clamp only, never accelerate. Forcing carts UP to cruise speed would fight
 * station stops, proximity braking and the slow-zone ramp — a train stopping
 * inside a cruise zone must still be allowed to stop. Since ModMetro
 * accelerates by 10% a tick on its own, a clamp is all that is needed to make
 * the train settle at exactly cruise speed through the zone.
 *
 * Runs at RETURN of {@code tick}, after both the lead and follower controllers
 * have had their say, so it is the last word on velocity. It only ever lowers
 * speed, so a tighter cap set by the slow-zone ramp still wins.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroCruiseZoneMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void mmsCompat$cruiseClamp(CallbackInfo ci) {
        if (!MetroCruiseConfig.cruise_enabled) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        Level world = self.level();
        if (!(world instanceof ServerLevel)) {
            return; // server-only, and covers tick()'s early client return
        }

        Vec3 vel = self.getDeltaMovement();
        double speed = vel.horizontalDistance();
        double cruise = MetroCruiseConfig.cruise_speed;
        if (speed <= cruise || cruise <= 0.0) {
            return; // already at or below cruise: nothing to do, and never speed up
        }

        Block marker = MetroCruiseConfig.marker();
        if (marker == null) {
            return; // unresolvable marker id: stay out of the way entirely
        }
        if (!mmsCompat$overMarker(world, self, vel, speed, marker)) {
            return;
        }

        double s = cruise / speed;
        self.setDeltaMovement(vel.x * s, vel.y, vel.z * s);
        self.hurtMarked = true;
    }

    /**
     * Marker beneath the path swept this tick. Sub-stepped prev->now, one check
     * per block of travel, the same scan shape ModMetro uses for station blocks
     * — without it a cart doing 3.4 b/t would step straight over a zone it is
     * supposed to be slowed by, which is the very failure being fixed.
     */
    @Unique
    private boolean mmsCompat$overMarker(Level world, MetroCartEntity self, Vec3 vel,
                                         double speed, Block marker) {
        Vec3 now = new Vec3(self.getX(), self.getY(), self.getZ());
        Vec3 prev = now.subtract(vel);
        int steps = Math.max(1, (int) Math.ceil(speed));
        for (int i = 0; i <= steps; i++) {
            double fraction = steps == 1 ? 1.0 : (double) i / steps;
            BlockPos checkPos = BlockPos.containing(prev.lerp(now, fraction));
            for (int y = 1; y <= 2; y++) {
                if (world.getBlockState(checkPos.below(y)).getBlock() == marker) {
                    return true;
                }
            }
        }
        return false;
    }
}
