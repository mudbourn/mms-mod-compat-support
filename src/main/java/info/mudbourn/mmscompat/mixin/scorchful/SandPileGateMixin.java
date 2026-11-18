package info.mudbourn.mmscompat.mixin.scorchful;

import com.github.thedeathlycow.scorchful.world.SandAccumulation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Moves the one-in-sixteen sand-pile roll ahead of the heightmap lookup in tickChunk
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

    // Makes the original roll always pass
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
