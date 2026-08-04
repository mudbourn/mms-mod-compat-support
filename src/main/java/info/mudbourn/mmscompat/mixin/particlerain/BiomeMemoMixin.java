package info.mudbourn.mmscompat.mixin.particlerain;

import info.mudbourn.mmscompat.client.ParticleRainBiomeMemo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pigcart.particlerain.ParticleSpawner;

/**
 * Memoises the loop-invariant biome lookup in Particle Rain's
 * {@code ParticleSpawner.tickBlockFX}.
 *
 * <p>The method is called by vanilla {@code ClientLevel.animateTick} for each
 * of its ~667 random positions per tick, and for each one it loops over every
 * particle definition:</p>
 *
 * <pre>
 *   for (ParticleData opts : ParticleLoader.particles.values()) {
 *       if (opts.enabled &amp;&amp; opts.weather.isCurrent(level)) {
 *           Holder&lt;Biome&gt; biome = level.getBiome(sourcePos);   // &lt;-- inside the loop
 *           ...
 * </pre>
 *
 * <p>{@code sourcePos} does not change across that loop, so the lookup is
 * repeated once per enabled definition for an identical answer. The shipped
 * {@code particles.json} defines <b>14</b>, and {@code ClientLevel.getBiome}
 * is not cheap: it goes through {@code BiomeManager} to
 * {@code ClientChunkCache$Storage.getChunk}, an uncached chunk fetch.</p>
 *
 * <p>Nor is the loop rare. It looks throttled --
 * {@code if (spawnAttemptsUntilBlockFXIdle > 0 || random.nextFloat() >= 0.9F)}
 * -- but the moment any particle actually spawns the method sets
 * {@code spawnAttemptsUntilBlockFXIdle = 10000}, so for the next 10000 calls
 * the first half of that disjunction is true and every position runs the full
 * loop. During weather that is the normal state.</p>
 *
 * <p>Measured on a 51.6 s client render-thread spark profile (2026-08-04):
 * {@code ClientChunkCache$Storage.getChunk} was <b>2576 ms, 4.99 % self time
 * on the render thread</b> -- the heaviest non-native frame in the entire
 * profile -- and 3.42 % of the thread was reached through Particle Rain
 * specifically. {@code tickBlockFX}'s subtree was 2132 ms, which is 70 % of
 * everything {@code ClientLevel.animateTick} costs.</p>
 *
 * <p><b>Why a redirect and not a config change.</b> Neither
 * {@code perf.particleDensity} nor {@code perf.maxParticleAmount} touches
 * this: {@code opts.density > random.nextFloat()} and the particle cap are
 * both evaluated <em>after</em> {@code getBiome}. Turning density down reduces
 * particles drawn, not lookups performed. Only {@code opts.enabled} short-
 * circuits ahead of the call, so short of deleting particle types the cost is
 * not reachable from configuration at all.</p>
 *
 * <p>The redirect is behaviour-identical rather than approximate. Quantising
 * the key to quart (4x4x4) resolution would cache far better and would also
 * help {@link pigcart.particlerain.ParticleSpawner#tickSkyFX} and Cosy
 * Critters, but {@code BiomeManager} applies a per-block fuzz offset before
 * resolving, so quart keys change which biome edge blocks report. That is a
 * visible difference at biome borders, so it is not done here.</p>
 *
 * <p>Scoped to {@code tickBlockFX} alone. {@code tickSkyFX} and
 * {@code tickSurfaceFX} contain the same call but run once per tick each, not
 * once per definition per position, and have nothing to memoise.</p>
 */
@Mixin(value = ParticleSpawner.class, remap = false)
public abstract class BiomeMemoMixin {

    @Redirect(
        method = "tickBlockFX",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;",
            remap = true
        )
    )
    private static Holder<Biome> mmscompat$memoiseBiome(ClientLevel level, BlockPos pos) {
        return ParticleRainBiomeMemo.getBiome(level, pos);
    }
}
