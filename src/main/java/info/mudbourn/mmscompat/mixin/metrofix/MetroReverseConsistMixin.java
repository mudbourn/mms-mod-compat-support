package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;

/**
 * Makes a {@code reverse} station actually reverse the TRAIN, not just the
 * lead cart's heading.
 *
 * ModMetro's entire reversal is three lines at the end of the station wait:
 * the lead cart flips its own {@code lastDirection}, clears the next-station
 * field, and accelerates. {@code trainIndex} is never touched. Cart 0 is now
 * physically at the BACK of the direction of travel and drives forward
 * straight through carts 1, 2, 3…, while every follower still chases
 * {@code trainIndex - 1} — which now sits ahead of it in the wrong
 * direction, so the whole consist gets dragged through itself. This is the
 * "cart 1 drives through the entire train" behaviour.
 *
 * A reversal is an order swap: the rearmost cart becomes the new cart 0 and
 * everything renumbers {@code i -> n-1-i}. Because ModMetro identifies a
 * train by its lead cart's UUID ({@code findFrontCart} matches on
 * {@code leadCartUuid}, and {@code applyProximityBraking} recognises its own
 * followers by comparing the lead's own UUID against theirs), the swap must
 * also re-point every cart's {@code leadCartUuid} at the new head —
 * {@code setTrainData} does both in one call. Lead-only state (line name,
 * last station, the station display fields) transfers to the new head so it
 * does not come up blank and re-trigger the terminal it is standing on.
 *
 * Detection is a small state machine on {@code tickLeadCart}'s exit rather
 * than a bytecode injection point inside the wait branch: arm when the lead
 * is waiting with next-station {@code reverse}, fire on the tick it stops
 * waiting. Same approach, and same reason, as {@link MetroSlowZoneMixin}'s
 * use of {@code @At("RETURN")}.
 *
 * Consists whose indices are not a clean {@code 0..n-1} run — part of the
 * train sitting in an unloaded chunk, say — are left alone: ModMetro's known
 * bad reversal beats scrambling train identity irrecoverably.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroReverseConsistMixin {

    /** Horizontal half-extent of the consist search. Covers spacing x wagons with room to spare. */
    @Unique
    private static final double CONSIST_RANGE = 96.0;

    /** Departure impulse ModMetro gives a reversing lead cart. */
    @Unique
    private static final double DEPART_IMPULSE = 0.4;

    @Shadow
    @Final
    private static EntityDataAccessor<String> CURRENT_STATION;

    @Shadow
    @Final
    private static EntityDataAccessor<String> NEXT_STATION;

    @Unique
    private boolean mmsCompat$reverseArmed;

    @Inject(method = "tickLeadCart", at = @At("RETURN"))
    private void mmsCompat$reverseOrderSwap(Level world, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;

        if (self.isWaitingAtStation()) {
            if ("reverse".equalsIgnoreCase(self.getNextStation())) {
                this.mmsCompat$reverseArmed = true;
            }
            return;
        }
        if (!this.mmsCompat$reverseArmed) {
            return;
        }
        // The wait just ended on a reverse station: ModMetro has flipped this
        // cart's lastDirection and started it moving. Swap the order to match.
        this.mmsCompat$reverseArmed = false;
        mmsCompat$swapConsist(world, self);
    }

    @Unique
    private void mmsCompat$swapConsist(Level world, MetroCartEntity oldLead) {
        if (!(world instanceof ServerLevel sw)) {
            return;
        }
        UUID trainId = oldLead.getLeadCartUuid();
        if (trainId == null) {
            return;
        }

        List<MetroCartEntity> train = new ArrayList<>();
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                oldLead.getBoundingBox().inflate(CONSIST_RANGE, 8.0, CONSIST_RANGE))) {
            if (trainId.equals(c.getLeadCartUuid())) {
                train.add(c);
            }
        }
        int n = train.size();
        if (n < 2) {
            return; // single cart: the mod's own heading flip is the whole job
        }
        train.sort(Comparator.comparingInt(MetroCartEntity::getTrainIndex));
        for (int i = 0; i < n; i++) {
            if (train.get(i).getTrainIndex() != i) {
                return; // incomplete or already-inconsistent consist; don't touch it
            }
        }

        MetroCartEntity newHead = train.get(n - 1);
        // Already flipped by ModMetro on the old lead — adopt it train-wide so
        // no cart is left believing in the pre-reversal heading.
        Vec3 dir = ((MetroCartStateAccessor) oldLead).mmsCompat$getLastDirection();

        MetroCartStateAccessor headAcc = (MetroCartStateAccessor) newHead;
        MetroCartStateAccessor leadAcc = (MetroCartStateAccessor) oldLead;
        headAcc.mmsCompat$setLine(leadAcc.mmsCompat$getLine());
        headAcc.mmsCompat$setLastStationPos(leadAcc.mmsCompat$getLastStationPos());
        headAcc.mmsCompat$setWaiting(false);
        headAcc.mmsCompat$setStationWaitTimer(0);
        newHead.getEntityData().set(CURRENT_STATION, oldLead.getEntityData().get(CURRENT_STATION));
        newHead.getEntityData().set(NEXT_STATION, oldLead.getEntityData().get(NEXT_STATION));

        UUID newTrainId = newHead.getUUID();
        for (int i = 0; i < n; i++) {
            MetroCartEntity cart = train.get(i);
            cart.setTrainData(newTrainId, n - 1 - i);
            MetroCartStateAccessor acc = (MetroCartStateAccessor) cart;
            acc.mmsCompat$setLastDirection(dir);
            acc.mmsCompat$setCachedFrontCart(null);
            acc.mmsCompat$setCachedLeadCart(null);
            cart.setDeltaMovement(Vec3.ZERO);
            cart.hurtMarked = true;
        }

        // Only the new head gets the departure impulse; the followers pick up
        // from rest through the normal (now rail-aware) follower controller.
        newHead.setDeltaMovement(dir.x * DEPART_IMPULSE, 0.0, dir.z * DEPART_IMPULSE);
        newHead.hurtMarked = true;
    }
}
