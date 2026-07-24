package info.mudbourn.mmscompat.duck;

import net.minecraft.core.BlockPos;

/**
 * Cross-instance access to the tail-station guard state that
 * {@code MetroTailStationMixin} keeps on a lead cart.
 *
 * Both fields live on whichever cart is currently index 0. A reversal moves
 * that role to a different entity, so the reversal has to carry the guard
 * across — see {@code MetroReverseConsistMixin}. Mixin {@code @Unique} fields
 * are not reachable from another instance, hence this duck.
 *
 * Lives outside the mixin package on purpose: every class in a mixin package
 * is treated as a mixin and blows up at runtime.
 */
public interface MetroTailStationDuck {

    /** Station this train most recently waited at, or null. */
    BlockPos mmsCompat$getLastServed();

    void mmsCompat$setLastServed(BlockPos pos);

    /** Drops the cached rear-cart reference, forcing a rescan next tick. */
    void mmsCompat$invalidateTailCache();
}
