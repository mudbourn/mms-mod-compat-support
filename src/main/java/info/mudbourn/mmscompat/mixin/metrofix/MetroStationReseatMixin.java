package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
 * Re-seats the whole consist onto the rail, at correct spacing, the moment a
 * train stops at a station.
 *
 * A stop is the only point in a run where the train is authoritatively at a
 * known place: ModMetro snaps the lead to the station block's centre and holds
 * it there for the whole wait. Everything behind the lead, though, keeps
 * whatever position the approach left it in — and a hard stop is exactly what
 * scrambles that. The lead brakes to zero in a tick or two while the followers
 * are still closing at line speed, so they pile in: carts inside each other,
 * carts shoved off the rail onto the platform, carts left at a spacing the
 * follower controller then spends the entire departure fighting. Those are the
 * positions that produce the through-the-train and stuck-cart reports.
 *
 * So the stop doubles as a resync. On the tick the wait begins, the rail spine
 * running back from the lead is walked with {@link MetroRailPath} and cart
 * {@code i} is placed at {@code i * spacing} blocks ALONG THE TRACK behind the
 * lead, velocity zeroed. Along the track, not through the air, because a train
 * standing in a terminal curve is the normal case and straight-line placement
 * is what pushes carts off the rail there in the first place.
 *
 * Carts already within {@link #RESEAT_TOLERANCE} of where they belong are left
 * untouched, so a clean arrival is not punished with a visible twitch; only
 * genuinely displaced carts move.
 *
 * Bails out — leaving ModMetro's own behaviour alone — when the consist is not
 * a clean {@code 0..n-1} run or the spine cannot be walked (unloaded chunk,
 * broken track). Guessing at positions in those cases is worse than the
 * displacement it would be fixing.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroStationReseatMixin {

    /** Horizontal half-extent of the consist search; matches MetroReverseConsistMixin. */
    @Unique
    private static final double CONSIST_RANGE = 96.0;

    /** How far off its mark a cart must be before it is worth teleporting, in blocks. */
    @Unique
    private static final double RESEAT_TOLERANCE = 0.6;

    /** Slack rails added to the spine walk beyond the train's own length. */
    @Unique
    private static final int SPINE_SLACK = 8;

    @Shadow
    private int trainIndex;

    @Shadow
    private boolean isWaiting;

    @Shadow
    private UUID leadCartUuid;

    @Shadow
    protected abstract BlockPos findNearestRail(Level world, BlockPos center);

    @Unique
    private boolean mmsCompat$wasWaiting;

    @Inject(method = "tickLeadCart", at = @At("RETURN"))
    private void mmsCompat$reseatOnArrival(Level world, CallbackInfo ci) {
        boolean waiting = this.isWaiting;
        boolean arrived = waiting && !this.mmsCompat$wasWaiting;
        this.mmsCompat$wasWaiting = waiting;

        if (!arrived || this.trainIndex != 0 || this.leadCartUuid == null
                || !(world instanceof ServerLevel sw)) {
            return;
        }
        mmsCompat$reseat(sw, (MetroCartEntity) (Object) this);
    }

    @Unique
    private void mmsCompat$reseat(ServerLevel sw, MetroCartEntity lead) {
        UUID trainId = lead.getLeadCartUuid();
        List<MetroCartEntity> train = new ArrayList<>();
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                lead.getBoundingBox().inflate(CONSIST_RANGE, 8.0, CONSIST_RANGE))) {
            if (trainId.equals(c.getLeadCartUuid())) {
                train.add(c);
            }
        }
        int n = train.size();
        if (n < 2) {
            return; // single cart: the mod's own station snap is the whole job
        }
        train.sort(Comparator.comparingInt(MetroCartEntity::getTrainIndex));
        for (int i = 0; i < n; i++) {
            if (train.get(i).getTrainIndex() != i) {
                return; // part of the train is missing or already inconsistent
            }
        }

        double spacing = MetroConfig.spacing;
        int length = (int) Math.ceil((n - 1) * spacing) + 2;

        BlockPos leadRail = this.findNearestRail(sw, lead.blockPosition());
        MetroCartEntity tail = train.get(n - 1);
        BlockPos tailRail = this.findNearestRail(sw, tail.blockPosition());
        if (leadRail == null || tailRail == null) {
            return;
        }

        // The tail is the far end of the walk, but a piled-up consist sits
        // SHORTER than its proper length — so the spine has to be extended
        // past the tail to have anywhere legal to put the back of the train.
        List<BlockPos> spine = MetroRailPath.spineBehind(sw, tailRail, leadRail,
                length + SPINE_SLACK, length + SPINE_SLACK);
        if (spine == null || spine.size() < 2) {
            return; // not rail-connected right now; don't guess
        }

        for (int i = 1; i < n; i++) {
            Vec3 target = mmsCompat$alongSpine(spine, i * spacing);
            MetroCartEntity cart = train.get(i);
            double dx = cart.getX() - target.x;
            double dz = cart.getZ() - target.z;
            boolean offMark = Math.sqrt(dx * dx + dz * dz) > RESEAT_TOLERANCE
                    || Math.abs(cart.getY() - target.y) > RESEAT_TOLERANCE;
            if (offMark) {
                cart.teleportTo(target.x, target.y, target.z);
            }
            cart.setDeltaMovement(Vec3.ZERO);
            cart.hurtMarked = true;
        }
    }

    /**
     * Point {@code distance} rail-blocks back along the spine, interpolated
     * between the two rails it falls between so the placement honours the
     * configured spacing instead of rounding it to whole blocks. Runs off the
     * end of the spine by clamping — better a slightly tight back of the train
     * than a cart dropped off the end of the track.
     */
    @Unique
    private Vec3 mmsCompat$alongSpine(List<BlockPos> spine, double distance) {
        int last = spine.size() - 1;
        int idx = (int) Math.floor(distance);
        if (idx >= last) {
            return mmsCompat$centre(spine.get(last));
        }
        Vec3 a = mmsCompat$centre(spine.get(idx));
        Vec3 b = mmsCompat$centre(spine.get(idx + 1));
        return a.lerp(b, distance - idx);
    }

    @Unique
    private Vec3 mmsCompat$centre(BlockPos rail) {
        return new Vec3(rail.getX() + 0.5, rail.getY() + 0.5, rail.getZ() + 0.5);
    }
}
