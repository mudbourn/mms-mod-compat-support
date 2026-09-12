package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Stops a lost audio device from cascading into a client crash.
 *
 * <p>{@link Library#releaseChannel} throws {@code IllegalStateException("Tried
 * to release unknown channel")} when the channel is in neither the static nor
 * the streaming pool. That happens whenever the OpenAL device is lost mid-session
 * (on macOS, unplugging headphones or switching output device): the sound engine
 * rebuilds {@link Library} with fresh, empty pools, and every channel handle that
 * was live on the old device then fails to release against the new one.
 *
 * <p>On MMSLive01 this was observed as ~1400 of these thrown on the Sound engine
 * thread in the ~30s after a device loss, with Sound Physics Remastered's EFX
 * sources multiplying the count. The engine limps on in a degraded state, and the
 * next bulk sound teardown (leaving the world) rethrows on the render thread,
 * dumping the client to a crash screen where downstream tickers (CosyCritters)
 * then crash again on the now-null level.
 *
 * <p>The old channel's OpenAL source died with the old device and context, so
 * there is nothing left to release: swallowing the throw is exactly what a clean
 * device swap should do. Normal releases still run through the wrapped original
 * untouched, so channel accounting on the live device is unchanged.
 */
@Mixin(Library.class)
public class SoundLibraryReleaseGuardMixin {

    @WrapMethod(method = "releaseChannel")
    private void mms$ignoreChannelFromResetDevice(Channel channel, Operation<Void> original) {
        try {
            original.call(channel);
        } catch (IllegalStateException ignored) {
        }
    }
}
