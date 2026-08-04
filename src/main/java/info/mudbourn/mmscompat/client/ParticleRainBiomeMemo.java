package info.mudbourn.mmscompat.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/**
 * One-entry memo for the biome lookup inside Particle Rain's
 * {@code ParticleSpawner.tickBlockFX}.
 *
 * <p>Lives outside {@code ...mixin.particlerain} on purpose: everything in a
 * mixin package is treated as a mixin and fails to load as an ordinary class
 * at runtime.</p>
 *
 * <p>Deliberately a single slot rather than a map. The call being memoised is
 * loop-invariant <em>within one invocation</em> of {@code tickBlockFX} -- the
 * same {@code sourcePos} is queried once per enabled particle definition -- so
 * consecutive hits always share a position and a one-slot memo captures the
 * entire win with no allocation and nothing to evict. Across the ~667
 * {@code animateTick} positions per tick the slot simply misses and refills,
 * which is exactly the unpatched cost.</p>
 *
 * <p>Keyed on game time as well as packed position so a stale holder can never
 * outlive the tick it was read in; chunk swaps and dimension changes both
 * advance the clock. {@code Long.MIN_VALUE} is the "empty" marker for the
 * position because {@link BlockPos#asLong()} never produces it.</p>
 */
public final class ParticleRainBiomeMemo {

    private static final long EMPTY = Long.MIN_VALUE;

    private static long lastPos = EMPTY;
    private static long lastTick = Long.MIN_VALUE;
    private static Holder<Biome> lastBiome = null;

    private ParticleRainBiomeMemo() {}

    /**
     * Drop-in replacement for {@code level.getBiome(pos)}.
     *
     * <p>Returns exactly what {@link ClientLevel#getBiome(BlockPos)} would
     * return: on a miss it delegates, and a hit can only occur for the same
     * position in the same tick.</p>
     */
    public static Holder<Biome> getBiome(ClientLevel level, BlockPos pos) {
        long tick = level.getGameTime();
        long packed = pos.asLong();

        if (packed == lastPos && tick == lastTick && lastBiome != null) {
            return lastBiome;
        }

        Holder<Biome> biome = level.getBiome(pos);
        lastPos = packed;
        lastTick = tick;
        lastBiome = biome;
        return biome;
    }

    /** Clears the slot. Called on disconnect so a holder cannot pin a dead level. */
    public static void reset() {
        lastPos = EMPTY;
        lastTick = Long.MIN_VALUE;
        lastBiome = null;
    }
}
