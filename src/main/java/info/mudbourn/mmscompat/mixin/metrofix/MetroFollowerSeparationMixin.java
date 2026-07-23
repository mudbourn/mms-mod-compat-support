package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.mudbourn.mmscompat.metro.MetroRailPath;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.config.MetroConfig;

/**
 * Rail-aware, occupancy-aware minimum-separation for follower carts.
 *
 * Two ModMetro bugs conspire to stack carts inside each other at stations:
 *
 *  1. The catch-up snap searches for a rail spacing..spacing+3 blocks behind
 *     the car ahead along a STRAIGHT line. On curved approaches no rail lies
 *     on that line, so the fallback teleports the cart to the raw
 *     straight-line point — off the rail, angled across the platform.
 *  2. Next tick the off-rail cart re-snaps via findNearestRail, which
 *     happily picks a rail block another cart already occupies. Two carts,
 *     one block.
 *
 * This mixin runs after tickFollowerCart each tick. If a follower ended up
 * closer than (spacing - 0.35) to the car ahead — the glide-overlap case —
 * or within 0.7 of ANY other metro cart — the stacked case — it is walked
 * back along its own travel direction to the first rail block at
 * spacing..spacing+3 behind the front car that no other cart occupies, and
 * given the front car's velocity. Rail-snapped, so curves are safe; skipping
 * occupied rails, so carts can no longer share a block.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroFollowerSeparationMixin {

    @Shadow
    private MetroCartEntity cachedFrontCart;

    @Shadow
    private Vec3 lastDirection;

    @Shadow
    protected abstract BlockPos findNearestRail(Level world, BlockPos center);

    @Unique
    private int mmsCompat$lastSnapTick = -100;

    @Inject(method = "tickFollowerCart", at = @At("TAIL"))
    private void mmsCompat$clampSeparation(Level world, CallbackInfo ci) {
        MetroCartEntity front = this.cachedFrontCart;
        if (front == null || front.isRemoved()) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        double spacing = MetroConfig.spacing;

        double dx = self.getX() - front.getX();
        double dz = self.getZ() - front.getZ();
        double distToFront = Math.sqrt(dx * dx + dz * dz);

        // Straight-line closeness is only evidence of overlap if the TRACK
        // agrees. Through a curve the two carts can be a block apart in the
        // air and a full correct spacing apart along the rail; treating that
        // as overlap used to run the follower at 0.9x the car ahead for the
        // whole curve, opening the gap until ModMetro's catch-up snap fired
        // and tore the train apart. See MetroCurveFollowMixin.
        boolean overlapping = distToFront < spacing - 0.35
                && mmsCompat$railGapBelow(world, self, front, spacing - 0.35);
        boolean stacked = !overlapping && mmsCompat$isStacked(world, self);
        Vec3 frontVel = front.getDeltaMovement();
        boolean frontMoving = frontVel.horizontalDistance() > 0.05;

        if (!overlapping && !stacked) {
            // Parked and already in position: freeze. ModMetro's creep
            // controller keeps micro-nudging parked followers around their
            // target (±0.3 error band), which alone reads as glitchy wobble —
            // and worse, it drags carts off any corrective snap, retriggering
            // it in a loop. A parked cart in position has no reason to move.
            if (!frontMoving && distToFront <= spacing + 0.5) {
                self.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        // Moving train: never teleport (snapping to block centers mid-motion
        // reads as bouncing). Ease off instead: run slightly slower than the
        // car ahead and let the gap re-open over a few ticks.
        if (frontMoving) {
            self.setDeltaMovement(frontVel.scale(0.9));
            return;
        }

        // Parked and out of position: hard un-stack, at most once every
        // 10 ticks per cart so a draining queue settles instead of churning.
        if (self.tickCount - this.mmsCompat$lastSnapTick < 10) {
            self.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.mmsCompat$lastSnapTick = self.tickCount;

        // Preferred: walk back along the actual rail spine from the car
        // ahead. Correct on curves as well as straight track.
        BlockPos myRail = this.findNearestRail(world, self.blockPosition());
        BlockPos frontRail = this.findNearestRail(world, front.blockPosition());
        if (myRail != null && frontRail != null) {
            java.util.List<BlockPos> spine = MetroRailPath.spineBehind(
                    world, myRail, frontRail, (int) Math.ceil(spacing) + 10, 4);
            if (spine != null) {
                int from = Math.max(1, (int) Math.round(spacing));
                for (int back = from; back <= from + 3 && back < spine.size(); back++) {
                    BlockPos rail = spine.get(back);
                    if (mmsCompat$railOccupied(world, rail, self)) {
                        continue;
                    }
                    self.teleportTo(rail.getX() + 0.5, rail.getY() + 0.5, rail.getZ() + 0.5);
                    self.setDeltaMovement(front.getDeltaMovement());
                    return;
                }
                return; // spine known but fully occupied: leave it be
            }
        }

        // Fallback (no rail connection found): walk back from the front car
        // along this cart's own travel heading (approach direction is noise
        // when already overlapped/stacked).
        Vec3 dir = this.lastDirection;
        double dirLen = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (dirLen < 0.001) {
            if (distToFront < 0.001) {
                return; // no usable heading at all; let the mod's own logic try
            }
            dir = new Vec3(dx / distToFront, 0.0, dz / distToFront).scale(-1.0);
            dirLen = 1.0;
        }
        double nx = dir.x / dirLen;
        double nz = dir.z / dirLen;

        for (double step = spacing; step <= spacing + 3.0; step += 1.0) {
            double tx = front.getX() - nx * step;
            double tz = front.getZ() - nz * step;
            BlockPos rail = this.findNearestRail(world, BlockPos.containing(tx, front.getY(), tz));
            if (rail == null || mmsCompat$railOccupied(world, rail, self)) {
                continue;
            }
            self.teleportTo(rail.getX() + 0.5, rail.getY() + 0.5, rail.getZ() + 0.5);
            self.setDeltaMovement(front.getDeltaMovement());
            return;
        }
        // No free rail found within range: leave position alone rather than
        // teleporting off-rail — the mod's own logic retries next tick.
    }

    /** True only if the carts really are closer than {@code limit} ALONG THE RAIL. */
    @Unique
    private boolean mmsCompat$railGapBelow(Level world, MetroCartEntity self,
                                           MetroCartEntity front, double limit) {
        BlockPos myRail = this.findNearestRail(world, self.blockPosition());
        BlockPos frontRail = this.findNearestRail(world, front.blockPosition());
        if (myRail == null || frontRail == null) {
            return true; // no rail reading: trust the straight-line result
        }
        int railDist = MetroRailPath.distance(world, myRail, frontRail,
                (int) Math.ceil(limit) + 10);
        return railDist < 0 || railDist < limit;
    }

    @Unique
    private boolean mmsCompat$isStacked(Level world, MetroCartEntity self) {
        for (MetroCartEntity other : world.getEntitiesOfClass(
                MetroCartEntity.class, self.getBoundingBox().inflate(0.7, 0.5, 0.7))) {
            if (other != self && self.distanceToSqr(other) < 0.49) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean mmsCompat$railOccupied(Level world, BlockPos rail, MetroCartEntity self) {
        AABB box = new AABB(rail).inflate(0.1, 1.0, 0.1);
        for (MetroCartEntity other : world.getEntitiesOfClass(MetroCartEntity.class, box)) {
            if (other != self) {
                return true;
            }
        }
        return false;
    }
}
