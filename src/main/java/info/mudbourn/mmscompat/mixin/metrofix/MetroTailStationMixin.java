package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.StationBlockEntity;
import com.example.modmetro.config.MetroConfig;

/**
 * Lets EITHER end of the train activate a station — the last cart as well as
 * the first.
 *
 * ModMetro scans for station blocks only under the lead cart's swept path, so
 * a stop is only ever recognised when cart 0 is standing on it. At a terminal
 * where the platform block does not line up under cart 0 — the usual case once
 * a reversing layout puts the train's other end at the platform — the stop is
 * simply never seen and the train runs straight through.
 *
 * This adds the symmetric case: the lead cart also scans the path swept by the
 * REAR cart of its own consist, and enters the identical waiting state if a
 * station passes beneath it. Everything is driven from the lead cart's own
 * fields — the rear cart is only read for its position — so the wait, the
 * next-station display, and the reverse handoff all work exactly as they do
 * for a normal stop, including {@link MetroReverseConsistMixin} picking up a
 * {@code reverse} action.
 *
 * Deliberately no teleport. ModMetro snaps the LEAD to the station block
 * centre on a normal stop; doing that for a rear-cart trigger would drag the
 * whole train forward onto the platform it is already standing at. The train
 * stops where it is and the follower controller settles the spacing.
 *
 * The re-trigger trap
 * -------------------
 * The rear cart crosses every station the lead has already served, on the way
 * out of it. Left unguarded that re-stops the train at the platform it is
 * departing, forever. Two guards, because neither is enough alone:
 *
 *  - {@code lastStationPos} — ModMetro's own per-cart guard, but it is cleared
 *    once the lead is ~10 blocks past the station, and a 4-car train at the
 *    default spacing is longer than that. It has usually expired by the time
 *    the rear cart reaches the platform.
 *  - {@link #mmsCompat$lastServed} — the station this train most recently
 *    waited at, remembered until it serves a DIFFERENT one. This is the guard
 *    that actually holds during departure.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroTailStationMixin {

    /** Re-resolve the rear cart at most this often (ticks); consists rarely change. */
    @Unique
    private static final int TAIL_RESCAN_INTERVAL = 20;

    @Shadow
    private UUID leadCartUuid;

    @Shadow
    private int trainIndex;

    @Shadow
    private boolean isWaiting;

    @Shadow
    private int stationWaitTimer;

    @Shadow
    private BlockPos lastStationPos;

    @Shadow
    private String lineName;

    @Shadow
    @Final
    private static EntityDataAccessor<String> CURRENT_STATION;

    @Shadow
    @Final
    private static EntityDataAccessor<String> NEXT_STATION;

    /** Station this train most recently waited at; blocks the departure re-trigger. */
    @Unique
    private BlockPos mmsCompat$lastServed;

    @Unique
    private MetroCartEntity mmsCompat$cachedTail;

    @Unique
    private int mmsCompat$tailRescanTick = -100;

    @Inject(method = "tickLeadCart", at = @At("RETURN"))
    private void mmsCompat$tailStationScan(Level world, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        if (!(world instanceof ServerLevel sw) || this.trainIndex != 0) {
            return;
        }

        // Remember what we're stopped at, for as long as we're stopped at it.
        if (this.isWaiting) {
            if (this.lastStationPos != null) {
                this.mmsCompat$lastServed = this.lastStationPos;
            }
            return; // already stopped; nothing for the rear cart to trigger
        }

        MetroCartEntity tail = mmsCompat$rearCart(sw, self);
        if (tail == null || tail == self) {
            return; // single-car train: the lead's own scan is the whole story
        }

        Vec3 vel = tail.getDeltaMovement();
        if (vel.horizontalDistance() < 0.01) {
            return; // parked; a stationary rear cart shouldn't arm anything
        }

        BlockPos station = mmsCompat$scanPath(world, tail, vel);
        if (station == null) {
            return;
        }
        if (station.equals(this.lastStationPos) || station.equals(this.mmsCompat$lastServed)) {
            return; // already served by the lead, or being departed right now
        }
        if (!(world.getBlockEntity(station) instanceof StationBlockEntity be)) {
            return;
        }

        // Enter the same waiting state a normal stop produces — minus the
        // teleport, which would drag the train onto the platform.
        this.isWaiting = true;
        this.stationWaitTimer = (int) (be.getWaitTime() * 20 * MetroConfig.station_wait_multiplier);
        this.lastStationPos = station;
        this.mmsCompat$lastServed = station;
        this.lineName = be.getLineName();
        if (be.getAction().equalsIgnoreCase("reverse")) {
            self.getEntityData().set(NEXT_STATION, "reverse");
        } else {
            self.getEntityData().set(NEXT_STATION, be.getNextStationName());
        }
        self.getEntityData().set(CURRENT_STATION, be.getStationName());
        self.setDeltaMovement(Vec3.ZERO);
        self.hurtMarked = true;
    }

    /**
     * Highest-index cart of this consist. Cached — this is a whole-level entity
     * query and the answer only changes when a train is built or reversed.
     */
    @Unique
    private MetroCartEntity mmsCompat$rearCart(ServerLevel sw, MetroCartEntity self) {
        MetroCartEntity cached = this.mmsCompat$cachedTail;
        if (cached != null && !cached.isRemoved()
                && self.tickCount - this.mmsCompat$tailRescanTick < TAIL_RESCAN_INTERVAL) {
            return cached;
        }
        this.mmsCompat$tailRescanTick = self.tickCount;

        MetroCartEntity best = null;
        int bestIdx = -1;
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                self.getBoundingBox().inflate(96.0, 8.0, 96.0))) {
            if (!this.leadCartUuid.equals(c.getLeadCartUuid())) {
                continue;
            }
            if (c.getTrainIndex() > bestIdx) {
                bestIdx = c.getTrainIndex();
                best = c;
            }
        }
        this.mmsCompat$cachedTail = best;
        return best;
    }

    /**
     * Station block beneath the path a cart swept this tick, using ModMetro's
     * own scan shape (sub-stepped prev->now, 1-2 blocks down) so stations
     * register at line speed instead of being stepped over.
     */
    @Unique
    private BlockPos mmsCompat$scanPath(Level world, MetroCartEntity cart, Vec3 vel) {
        Vec3 now = new Vec3(cart.getX(), cart.getY(), cart.getZ());
        Vec3 prev = now.subtract(vel);
        int steps = Math.max(1, (int) Math.ceil(vel.horizontalDistance()));
        for (int i = 0; i <= steps; i++) {
            double fraction = steps == 1 ? 1.0 : (double) i / steps;
            BlockPos checkPos = BlockPos.containing(prev.lerp(now, fraction));
            for (int y = 1; y <= 2; y++) {
                BlockPos pos = checkPos.below(y);
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof StationBlockEntity) {
                    return pos;
                }
            }
        }
        return null;
    }
}
