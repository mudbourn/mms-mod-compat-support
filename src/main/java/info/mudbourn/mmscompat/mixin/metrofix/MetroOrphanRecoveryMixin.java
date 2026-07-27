package info.mudbourn.mmscompat.mixin.metrofix;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import com.example.modmetro.MetroCartEntity;

import info.mudbourn.mmscompat.metro.MetroTrainDespawn;
import info.mudbourn.mmscompat.metro.MetroTuning;

/**
 * Fixes the terminal choke: a couple of orphaned carts stranding at the same
 * terminal and permanently braking every arrival behind them.
 *
 * Two ModMetro defects compound into this:
 *
 * <ol>
 * <li>{@code applyProximityBraking} identifies "my own train" by comparing
 * {@code this.getUUID()} (this cart's own identity) against the candidate's
 * {@code leadCartUuid}. That only ever matches for an intact train's lead
 * cart. Every follower — and any cart after a reversal renumbers the
 * consist, see {@link MetroReverseConsistMixin} — brakes for its own
 * train as if it were a foreign obstacle. Once a cart is orphaned it is
 * stationary, so it drives {@code brakeFactor} to zero for anything behind
 * it, forever.</li>
 * <li>{@code tickFollowerCart} increments {@code leadSearchRetries} on every
 * failed {@code findFrontCart} lookup and never reads it back. One missed
 * tick at a terminal — exactly where reversals renumber consists — orphans
 * a cart with no recovery path at all.</li>
 * </ol>
 *
 * Fix for (1): redirect the {@code UUID.equals} identity check to compare
 * {@code leadCartUuid} against {@code leadCartUuid} (this cart's own lead id,
 * falling back to its own UUID only if somehow unset), and additionally
 * treat a stationary orphan as never a real obstacle — it cannot be braking
 * for anything, so it should not gate the line either.
 *
 * Fix for (2): once {@code leadSearchRetries} crosses
 * {@link MetroTuning#orphan_recovery_ticks}, escalate: re-link by
 * {@code leadCartUuid} against all loaded carts (covers the renumbered-index
 * case {@link MetroReverseConsistMixin} did not already fix), else re-index
 * onto the nearest consist sharing this cart's line, else despawn via
 * {@link MetroTrainDespawn#despawnTrain}.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroOrphanRecoveryMixin {

    /** Horizontal half-extent of the consist / rejoin search. Matches {@link MetroReverseConsistMixin}. */
    @Unique
    private static final double SEARCH_RANGE = 96.0;

    @Shadow
    private UUID leadCartUuid;

    @Shadow
    private int trainIndex;

    @Shadow
    private int leadSearchRetries;

    @Shadow
    private MetroCartEntity cachedFrontCart;

    /**
     * Game time at which this cart was last (re)loaded from NBT, or
     * {@code Long.MIN_VALUE} before the first load. Anchors the post-reload
     * grace window in {@link MetroTuning#post_reload_grace_ticks} — the
     * window that stops {@code MetroTrainIndexSyncMixin}'s resync from being
     * outraced by orphan despawn on every server restart.
     */
    @Unique
    private long mmsCompat$loadGameTime = Long.MIN_VALUE;

    @Inject(method = "load", at = @At("TAIL"))
    private void mmsCompat$recordLoadTime(ValueInput view, CallbackInfo ci) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        if (self.level() instanceof ServerLevel sw) {
            this.mmsCompat$loadGameTime = sw.getGameTime();
        }
    }

    // --- Fix 1: consist identity in proximity braking -----------------------

    @Redirect(method = "applyProximityBraking",
            at = @At(value = "INVOKE", target = "Ljava/util/UUID;equals(Ljava/lang/Object;)Z"))
    private boolean mmsCompat$sameConsistOrIgnorableOrphan(UUID unusedMyTrainId, Object otherLeadIdObj,
            @Local MetroCartEntity other) {
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        UUID myConsistId = self.getLeadCartUuid() != null ? self.getLeadCartUuid() : self.getUUID();
        if (myConsistId.equals(otherLeadIdObj)) {
            return true; // same train: never treat it as a braking obstacle
        }
        // Foreign, but if it's a cart that has been failing its own
        // findFrontCart lookups for a while and isn't moving, it is an
        // orphan rather than a real train ahead — do not gate the line on it.
        return other.getDeltaMovement().horizontalDistance() < 0.02
                && ((MetroCartStateAccessor) other).mmsCompat$getLeadSearchRetries() >= MetroTuning.orphan_recovery_ticks;
    }

    // --- Fix 2: consume leadSearchRetries with real recovery -----------------

    @Inject(method = "tickFollowerCart", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$orphanRecovery(Level world, CallbackInfo ci) {
        if (this.leadCartUuid == null || this.leadSearchRetries < MetroTuning.orphan_recovery_ticks) {
            return;
        }
        if (!(world instanceof ServerLevel sw)) {
            return;
        }

        MetroCartEntity self = (MetroCartEntity) (Object) this;

        // Precondition: the synched index must agree with the restored field
        // before this cart is judged orphaned at all. Disagreement means
        // MetroTrainIndexSyncMixin hasn't resynced it yet (or a future
        // ModMetro update reintroduced the desync) — findFrontCart failures
        // caused by a stale synched value are not evidence of a real orphan.
        // Give it another cycle instead of escalating on bad data.
        if (self.getTrainIndex() != this.trainIndex) {
            this.leadSearchRetries = 0;
            return;
        }

        // (a) Re-link by leadCartUuid against every loaded cart. Covers a
        // consist whose indices survived a reversal but temporarily fell out
        // of load order — MetroReverseConsistMixin already keeps leadCartUuid
        // correct, this just re-resolves the pointer.
        int targetIndex = this.trainIndex - 1;
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                self.getBoundingBox().inflate(SEARCH_RANGE, 16.0, SEARCH_RANGE))) {
            if (c != self && c.getTrainIndex() == targetIndex && this.leadCartUuid.equals(c.getLeadCartUuid())) {
                this.cachedFrontCart = c;
                this.leadSearchRetries = 0;
                return; // let the rest of tickFollowerCart run this tick using the relink
            }
        }

        // (b) Re-index onto the nearest consist sharing this cart's line: its
        // own train is unrecoverable (no matching front cart anywhere
        // loaded), so treat it as a new tail car on a live train instead of
        // leaving it to strand.
        String myLine = ((MetroCartStateAccessor) self).mmsCompat$getLine();
        if (myLine != null && !myLine.isBlank()) {
            MetroCartEntity newLead = mmsCompat$findConsistLead(sw, self, myLine);
            if (newLead != null) {
                int newIndex = mmsCompat$consistSize(sw, newLead);
                self.setTrainData(newLead.getUUID(), newIndex);
                this.cachedFrontCart = null;
                this.leadSearchRetries = 0;
                ci.cancel(); // re-linked cold; resolve the new front cart next tick
                return;
            }
        }

        // (c) Nothing to rejoin: this cart is permanently orphaned. Despawning
        // it beats leaving a stationary obstacle that (per Fix 1) no longer
        // even blocks arrivals but also never carries anyone again — unless
        // it was loaded within the post-reload grace window, in which case
        // this could just be every cart on the map racing the same restart;
        // wait it out instead of deleting the fleet.
        boolean withinGrace = this.mmsCompat$loadGameTime != Long.MIN_VALUE
                && sw.getGameTime() - this.mmsCompat$loadGameTime < MetroTuning.post_reload_grace_ticks;
        if (withinGrace) {
            this.leadSearchRetries = MetroTuning.orphan_recovery_ticks;
            ci.cancel();
            return;
        }
        if (MetroTuning.orphan_despawn_enabled) {
            MetroTrainDespawn.despawnTrain(sw, this.leadCartUuid);
            if (!self.isRemoved()) {
                self.ejectPassengers();
                self.discard();
            }
        }
        ci.cancel();
    }

    /** Nearest OTHER train's lead cart (leadCartUuid == own UUID) sharing {@code line}, within {@link #SEARCH_RANGE}. */
    @Unique
    private MetroCartEntity mmsCompat$findConsistLead(ServerLevel sw, MetroCartEntity self, String line) {
        MetroCartEntity best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                self.getBoundingBox().inflate(SEARCH_RANGE, 16.0, SEARCH_RANGE))) {
            if (c == self || c.getTrainIndex() != 0 || !c.getUUID().equals(c.getLeadCartUuid())) {
                continue;
            }
            if (!line.equals(((MetroCartStateAccessor) c).mmsCompat$getLine())) {
                continue;
            }
            double d = self.distanceToSqr(c);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = c;
            }
        }
        return best;
    }

    /** Number of loaded carts already in {@code lead}'s train — the index a new tail car should take. */
    @Unique
    private int mmsCompat$consistSize(ServerLevel sw, MetroCartEntity lead) {
        UUID trainId = lead.getLeadCartUuid();
        int count = 0;
        for (MetroCartEntity c : sw.getEntitiesOfClass(MetroCartEntity.class,
                lead.getBoundingBox().inflate(SEARCH_RANGE, 16.0, SEARCH_RANGE))) {
            if (trainId.equals(c.getLeadCartUuid())) {
                count++;
            }
        }
        return count;
    }
}
