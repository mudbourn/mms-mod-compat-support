package info.mudbourn.mmscompat.mixin.scorchful;

import com.github.thedeathlycow.scorchful.server.SandAccumulation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves Scorchful's one-in-sixteen sand-pile roll to the top of tickChunk.
 *
 * <p>Scorchful runs {@code SandAccumulation.tickChunk} for every chunk on every
 * chunk tick, and the method's own order of operations is:
 *
 * <pre>
 *   1. getBlockRandomPos + ServerLevel#getHeightmapPos   &lt;-- expensive
 *   2. Sandstorms.getCurrentSandStorm  -> return if NONE
 *   3. level.random.nextInt(16) != 0   -> return
 *   4. read config, place the pile, maybe fill a cauldron
 * </pre>
 *
 * <p>Step 1 is a world-level heightmap query, so it resolves the chunk through
 * {@code ServerChunkCache#getChunk} rather than reading the {@link LevelChunk}
 * it was already handed, and lands in {@code getChunkBlocking} plus the C2ME
 * scheduler. On MMSLive01 that was ~6.1s of a 7.2s subtree, or roughly 18% of
 * all non-idle server-thread time, measured across two spark captures.
 *
 * <p>Two things make this worse than it looks. The cost is paid before the
 * sandstorm check, so it is not storm-conditional — it is a permanent tax on
 * every loaded chunk. And it is paid before the config check: Scorchful only
 * consults {@code isSandPileAccumulationEnabled()} inside {@code placeSandPile},
 * so setting {@code doSandPileAccumulation: false} does not remove any of it.
 *
 * <p>Steps 2-4 are already reached only one time in sixteen, and the roll at
 * step 3 depends on nothing above it, so hoisting it to HEAD is free: the
 * heightmap query then runs sixteen times less often for an identical outcome.
 *
 * <p>The roll is drawn from this mixin's own {@link RandomSource} rather than
 * from {@code level.random}, so the shared world RNG stream is left exactly as
 * it was, and Scorchful's own {@code nextInt(16)} is redirected to zero so the
 * two gates do not compound into a one-in-256 rate. Net sand-pile frequency is
 * unchanged.
 */
@Mixin(value = SandAccumulation.class, remap = false)
public abstract class SandPileGateMixin {

    private static final RandomSource mms$gateRandom = RandomSource.create();

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private static void mms$rollBeforeHeightmapLookup(
            ServerLevel level, LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (mms$gateRandom.nextInt(16) != 0) {
            ci.cancel();
        }
    }

    /**
     * The surviving one call in sixteen has already passed the gate above, so
     * Scorchful's own roll must always succeed. Without this the two rolls
     * multiply and sand piles form sixteen times slower than they used to.
     */
    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I",
                    ordinal = 0
            )
    )
    private static int mms$alwaysPassOriginalRoll(RandomSource random, int bound) {
        return 0;
    }
}
