package info.mudbourn.mmscompat.mixin.cosycritters;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pigcart.cosycritters.CosyCritters;

/**
 * Skips Cosy Critters' client tick while the level is unloaded.
 *
 * <p>{@code CosyCritters.onTick} guards only on {@code minecraft.player} being
 * non-null, then hands {@code minecraft.level} straight to {@code trySpawnBird},
 * which calls {@code Util.isDay(level)} and dereferences it. During a disconnect
 * or world unload the player object lingers for a tick after the level is cleared,
 * so {@code level} is null and {@code Util.isDay} throws a NullPointerException.
 *
 * <p>On MMSLive01 this fired as the second crash in a device-loss cascade: the
 * sound engine broke on an audio-device change, the client dropped toward the
 * title screen, and this ticker crashed on the null level on the way out. The
 * level side of that cascade is closed by {@code SoundLibraryReleaseGuardMixin};
 * this closes the ticker side so a null level on any unload path is harmless.
 *
 * <p>Cancelling the whole tick when the level is null is safe: every branch it
 * reaches (bird and hatman spawning) needs a loaded level to do anything.
 */
@Mixin(value = CosyCritters.class, remap = false)
public class CosyCrittersTickLevelGuardMixin {

    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void mms$skipWhenLevelNull(Minecraft minecraft, CallbackInfo ci) {
        if (minecraft.level == null) {
            ci.cancel();
        }
    }
}
