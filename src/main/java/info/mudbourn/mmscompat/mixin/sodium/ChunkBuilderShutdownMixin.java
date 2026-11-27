package info.mudbourn.mmscompat.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.executor.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;

// Skips a second shutdown of Sodium's chunk builder, which happens when the game closes after leaving a world.
@Mixin(value = ChunkBuilder.class, remap = false)
public abstract class ChunkBuilderShutdownMixin {

    @WrapMethod(method = "shutdown")
    private void mmsCompat$skipRepeatShutdown(Operation<Void> original) {
        try {
            original.call();
        } catch (IllegalStateException stopped) {
            if (!"Worker threads are not running".equals(stopped.getMessage())) {
                throw stopped;
            }
        }
    }
}
