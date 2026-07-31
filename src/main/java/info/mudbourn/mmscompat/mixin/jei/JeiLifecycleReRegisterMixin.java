package info.mudbourn.mmscompat.mixin.jei;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * JEI registers its resource reloaders ("jei:lifecycle", "jei:resources_reload")
 * from a CLIENT_STARTED listener, with no guard against running twice. Normally
 * it cannot: the client starts once. NotEnoughCrashes breaks that assumption —
 * after it recovers from a client crash it re-enters Minecraft#run, CLIENT_STARTED
 * fires again, and Fabric's ResourceLoader kills the game outright:
 *
 *   IllegalStateException: Tried to register resource reloader jei:lifecycle twice!
 *
 * The user-visible effect is that the *first* crash looks unrecoverable and the
 * reported error is this one, not the real cause — see the JEI null-style chat
 * tooltip NPE that produced it here. The reloaders and events from the first
 * pass are still live at that point, so the correct second-pass behaviour is to
 * do nothing.
 */
@Pseudo
@Mixin(targets = "mezz.jei.fabric.JustEnoughItemsClient", remap = false)
public class JeiLifecycleReRegisterMixin {

    private static boolean mmsCompat$lifecycleRegistered;

    @Inject(
        method = "lambda$onInitializeClient$1",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void mmsCompat$registerOnce(@Coerce Object lifecycleHandler, Minecraft client, CallbackInfo ci) {
        if (mmsCompat$lifecycleRegistered) {
            ci.cancel();
            return;
        }
        mmsCompat$lifecycleRegistered = true;
    }
}
