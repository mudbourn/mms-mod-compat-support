package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
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
 * Rail-aware follower control for curved track. Fixes the U-turn breakup.
 *
 * ModMetro's follower controller is built entirely on straight-line distance
 * to the car ahead, which is only the same thing as track distance on
 * straight track. On a tight curve — worst of all a U-turn, where the two
 * legs run within a couple of blocks of each other — the following chain
 * fires, in order:
 *
 *  1. {@code dist = distanceTo(front)} reads far SHORTER than the real track
 *     gap, so {@code error = dist - spacing} goes strongly negative and
 *     {@code wantedSpeed} clamps to zero. The follower stalls mid-curve while
 *     the car ahead keeps going.
 *  2. With the train parked, the two creep branches steer by
 *     {@code front.position - self.position} — a straight line that, across a
 *     U-turn, points through the middle of the loop rather than along the
 *     rail. The "too close" branch inverts it and drives the cart BACKWARDS
 *     down its own track. This is the cart visibly setting off the wrong way.
 *  3. The gap now blows past {@code spacing + 4} and the catch-up snap runs.
 *     It looks for rail on a straight ray along {@code front.lastDirection};
 *     on a curve there is none, so it falls through to teleporting the cart
 *     to the raw point {@code front - dir * spacing} — which on a U-turn sits
 *     on top of the car ahead. This is the cart teleporting into cart 1.
 *  4. That cart is now off-rail, so next tick it snaps to
 *     {@code findNearestRail}, which can just as easily pick the OTHER leg of
 *     the U. Repeat until the geometry straightens out and the train
 *     "slowly fixes itself".
 *
 * This mixin takes the follower over for the duration of the curve. It runs
 * at HEAD and cancels ModMetro's version only when the track distance to the
 * car ahead exceeds the straight-line distance by more than
 * {@link #CURVE_SLACK} blocks — i.e. only where the mod's geometry is
 * actually wrong. On straight track nothing changes and the mod runs
 * untouched (as does {@link MetroFollowerSeparationMixin}, which is a TAIL
 * injection into the same method and therefore also skipped when we cancel).
 *
 * While we hold the follower it is driven along the rail spine: spacing is
 * measured in rail blocks, the creep direction is the NEXT RAIL along the
 * path rather than the bearing to the car ahead, and a moving cart is never
 * teleported — only a parked, badly-out-of-place one is re-seated, onto a
 * rail the spine actually contains.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroCurveFollowMixin {

    /**
     * How much longer the track route must be than the crow flight before we
     * call it a curve and take over. Block-centre quantisation alone accounts
     * for well under a block on straight track; a U-turn at the default
     * 4-block spacing shows 2.5+.
     */
    @Unique
    private static final double CURVE_SLACK = 1.25;

    /** Bound on the rail BFS: spacing plus enough slack to find a stretched gap. */
    @Unique
    private static final int SEARCH_SLACK = 10;

    /** Creep speed ceiling while the train is parked and repositioning. */
    @Unique
    private static final double CREEP_CAP = 0.1;

    /** Track gap beyond which a PARKED follower is re-seated rather than driven. */
    @Unique
    private static final double RESEAT_ERROR = 8.0;

    @Shadow
    private UUID leadCartUuid;

    @Shadow
    private MetroCartEntity cachedFrontCart;

    @Shadow
    private MetroCartEntity cachedLeadCart;

    @Shadow
    private Vec3 lastDirection;

    @Shadow
    private int leadSearchRetries;

    @Shadow
    @Final
    private static EntityDataAccessor<String> CURRENT_STATION;

    @Shadow
    @Final
    private static EntityDataAccessor<String> NEXT_STATION;

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> IS_WAITING;

    @Shadow
    @Final
    private static EntityDataAccessor<Integer> WAIT_TIME;

    @Shadow
    protected abstract MetroCartEntity findFrontCart(Level world);

    @Shadow
    protected abstract BlockPos findNearestRail(Level world, BlockPos center);

    @Unique
    private int mmsCompat$lastReseatTick = -100;

    @Inject(method = "tickFollowerCart", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$curveFollow(Level world, CallbackInfo ci) {
        if (this.leadCartUuid == null) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;

        // Front-cart resolution, mirroring ModMetro's own: we may be about to
        // cancel the method that normally does this.
        if (this.cachedFrontCart == null || this.cachedFrontCart.isRemoved()) {
            this.cachedFrontCart = this.findFrontCart(world);
            if (this.cachedFrontCart == null) {
                this.leadSearchRetries++;
                return; // let the mod handle the missing-front case
            }
            this.leadSearchRetries = 0;
        }
        MetroCartEntity front = this.cachedFrontCart;

        // Same for the lead cart: ModMetro resolves it inside the method we
        // may cancel, and on a long curve we would cancel every tick, leaving
        // the cache permanently null and the HUD sync dead.
        if ((this.cachedLeadCart == null || this.cachedLeadCart.isRemoved())
                && world instanceof ServerLevel sw
                && sw.getEntity(this.leadCartUuid) instanceof MetroCartEntity resolved) {
            this.cachedLeadCart = resolved;
        }

        BlockPos myRail = this.findNearestRail(world, self.blockPosition());
        BlockPos frontRail = this.findNearestRail(world, front.blockPosition());
        if (myRail == null || frontRail == null) {
            return; // off-rail entirely: the mod's re-snap is the right move
        }

        double spacing = MetroConfig.spacing;
        int maxSteps = (int) Math.ceil(spacing) + SEARCH_SLACK;
        List<BlockPos> spine = MetroRailPath.spineBehind(world, myRail, frontRail, maxSteps, 4);
        if (spine == null) {
            return; // not rail-connected within range; don't guess
        }

        // The follower's own rail sits at index == its track distance from the
        // front cart; the tail extension is appended after it and never
        // repeats a spine block, so indexOf is exact and saves a second BFS.
        int railIdx = spine.indexOf(myRail);
        if (railIdx <= 0) {
            return;
        }
        double railDist = railIdx;
        double euclid = Math.sqrt(
                (self.getX() - front.getX()) * (self.getX() - front.getX())
                        + (self.getZ() - front.getZ()) * (self.getZ() - front.getZ()));

        if (railDist - euclid <= CURVE_SLACK) {
            return; // straight enough that ModMetro's own maths is sound
        }

        // --- Curve: we drive this cart. ---
        ci.cancel();

        Vec3 frontVel = front.getDeltaMovement();
        MetroCartEntity lead = this.cachedLeadCart;
        double baseSpeed = lead != null && !lead.isRemoved()
                ? lead.getDeltaMovement().horizontalDistance()
                : frontVel.horizontalDistance();
        double error = railDist - spacing;

        if (baseSpeed < 0.01) {
            mmsCompat$driveParked(world, self, spine, spacing, error);
        } else {
            mmsCompat$driveMoving(self, spine, baseSpeed, error);
        }

        mmsCompat$syncStationData(self);
    }

    /**
     * Moving train. Speed is corrected against the RAIL gap; heading comes
     * from the cart's own motion (vanilla rail physics is already steering
     * it) or, from a standstill, from the next rail along the spine. No
     * teleporting at speed — snapping to block centres mid-curve is what
     * reads as the train tearing itself apart.
     */
    @Unique
    private void mmsCompat$driveMoving(MetroCartEntity self, List<BlockPos> spine,
                                       double baseSpeed, double error) {
        double maxFollower = Math.max(2.0, MetroConfig.speed * 1.5);
        double wanted = Math.max(0.0, Math.min(baseSpeed + error * 0.4, maxFollower));

        Vec3 myVel = self.getDeltaMovement();
        double mySpeed = myVel.horizontalDistance();
        Vec3 dir;
        if (mySpeed > 0.001) {
            dir = new Vec3(myVel.x / mySpeed, 0.0, myVel.z / mySpeed);
        } else {
            dir = mmsCompat$towardFront(self, spine);
            if (dir == null) {
                return;
            }
        }
        self.setDeltaMovement(dir.x * wanted, myVel.y, dir.z * wanted);
        self.hurtMarked = true;
    }

    /**
     * Parked train. Nudge along the rail into position, or — if the cart is
     * grossly out of place, the state the old catch-up snap used to create —
     * re-seat it onto the spine rail that actually sits {@code spacing} back
     * from the car ahead, at most once every 10 ticks.
     */
    @Unique
    private void mmsCompat$driveParked(Level world, MetroCartEntity self, List<BlockPos> spine,
                                       double spacing, double error) {
        if (Math.abs(error) <= 0.3) {
            self.setDeltaMovement(Vec3.ZERO);
            self.hurtMarked = true;
            return;
        }

        if (Math.abs(error) > RESEAT_ERROR) {
            if (self.tickCount - this.mmsCompat$lastReseatTick >= 10) {
                this.mmsCompat$lastReseatTick = self.tickCount;
                BlockPos target = mmsCompat$freeSpineRail(world, self, spine, spacing);
                if (target != null) {
                    self.teleportTo(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
                }
            }
            self.setDeltaMovement(Vec3.ZERO);
            self.hurtMarked = true;
            return;
        }

        Vec3 dir = mmsCompat$towardFront(self, spine);
        if (dir == null) {
            self.setDeltaMovement(Vec3.ZERO);
            self.hurtMarked = true;
            return;
        }
        if (error < 0.0) {
            dir = dir.scale(-1.0); // too close: back off ALONG THE RAIL, not away in a straight line
        }
        double speed = Math.min(Math.abs(error) * 0.06, CREEP_CAP);
        self.setDeltaMovement(dir.x * speed, self.getDeltaMovement().y, dir.z * speed);
        self.hurtMarked = true;
    }

    /**
     * Unit heading from this cart towards the next rail along the spine
     * towards the car ahead. This is the whole point of the fix: on a curve
     * the bearing to the car ahead and the direction of the track are two
     * different things, and only the latter is safe to steer by.
     */
    @Unique
    private Vec3 mmsCompat$towardFront(MetroCartEntity self, List<BlockPos> spine) {
        BlockPos myRail = null;
        int myIdx = -1;
        for (int i = 0; i < spine.size(); i++) {
            BlockPos p = spine.get(i);
            if (Math.abs(p.getX() + 0.5 - self.getX()) < 1.0
                    && Math.abs(p.getZ() + 0.5 - self.getZ()) < 1.0) {
                myRail = p;
                myIdx = i;
                break;
            }
        }
        if (myRail == null || myIdx <= 0) {
            return null;
        }
        BlockPos next = spine.get(myIdx - 1); // one step closer to the front
        double dx = next.getX() + 0.5 - self.getX();
        double dz = next.getZ() + 0.5 - self.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) {
            return null;
        }
        return new Vec3(dx / len, 0.0, dz / len);
    }

    /**
     * The rail {@code spacing}..{@code spacing+3} steps behind the car ahead
     * ALONG THE TRACK that no other cart is sitting on.
     */
    @Unique
    private BlockPos mmsCompat$freeSpineRail(Level world, MetroCartEntity self,
                                             List<BlockPos> spine, double spacing) {
        int from = Math.max(1, (int) Math.round(spacing));
        for (int back = from; back <= from + 3 && back < spine.size(); back++) {
            BlockPos candidate = spine.get(back);
            boolean occupied = false;
            for (MetroCartEntity other : world.getEntitiesOfClass(MetroCartEntity.class,
                    new AABB(candidate).inflate(0.1, 1.0, 0.1))) {
                if (other != self) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Station/HUD fields normally copied from the lead cart at the end of
     * {@code tickFollowerCart}; replicated because we cancelled it.
     */
    @Unique
    private void mmsCompat$syncStationData(MetroCartEntity self) {
        MetroCartEntity lead = this.cachedLeadCart;
        if (lead == null || lead.isRemoved()) {
            return;
        }
        self.getEntityData().set(CURRENT_STATION, lead.getEntityData().get(CURRENT_STATION));
        self.getEntityData().set(NEXT_STATION, lead.getEntityData().get(NEXT_STATION));
        self.getEntityData().set(IS_WAITING, lead.getEntityData().get(IS_WAITING));
        self.getEntityData().set(WAIT_TIME, lead.getEntityData().get(WAIT_TIME));
    }
}
