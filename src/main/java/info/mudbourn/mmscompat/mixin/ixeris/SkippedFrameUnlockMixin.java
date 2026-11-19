package info.mudbourn.mmscompat.mixin.ixeris;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.CGL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

// Releases the macOS GL context lock Ixeris takes in runTick when renderFrame returns without reaching updateDisplay.
@Mixin(Minecraft.class)
public abstract class SkippedFrameUnlockMixin {
    @Unique
    private static final VarHandle mmsCompat$LOCKED_CONTEXT = mmsCompat$lockedContextHandle();

    @Unique
    private static VarHandle mmsCompat$lockedContextHandle() {
        try {
            Class<?> ixeris = Class.forName("me.decce.ixeris.IxerisMod");
            return MethodHandles.publicLookup().findStaticVarHandle(ixeris, "lockedContext", long.class);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Ixeris lockedContext not found", e);
        }
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V", shift = At.Shift.AFTER))
    private void mmsCompat$unlockSkippedFrame(boolean advanceGameTime, CallbackInfo ci) {
        long context = (long) mmsCompat$LOCKED_CONTEXT.getVolatile();
        if (context != 0L) {
            CGL.CGLUnlockContext(context);
            mmsCompat$LOCKED_CONTEXT.setVolatile(0L);
        }
    }
}
