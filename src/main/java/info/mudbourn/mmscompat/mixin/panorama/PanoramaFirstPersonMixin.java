package info.mudbourn.mmscompat.mixin.panorama;

import java.io.File;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Turns Perspective API off while a panorama is captured, so it is taken from the player's eyes.
@Mixin(Minecraft.class)
public abstract class PanoramaFirstPersonMixin {

    @Unique
    private static final String MMS_COMPAT$API = "io.github.leawind.perspectiveapi.api.PerspectiveAPI";

    @Unique
    private boolean mmsCompat$restorePerspective;

    @Inject(method = "grabPanoramixScreenshot", at = @At("HEAD"))
    private void mmsCompat$disablePerspective(File folder, CallbackInfoReturnable<Component> info) {
        mmsCompat$restorePerspective = mmsCompat$isEnabled();
        if (mmsCompat$restorePerspective) {
            mmsCompat$setEnabled(false);
        }
    }

    @Inject(method = "grabPanoramixScreenshot", at = @At("RETURN"))
    private void mmsCompat$restorePerspective(File folder, CallbackInfoReturnable<Component> info) {
        if (mmsCompat$restorePerspective) {
            mmsCompat$restorePerspective = false;
            mmsCompat$setEnabled(true);
        }
    }

    @Unique
    private static boolean mmsCompat$isEnabled() {
        try {
            return (boolean) Class.forName(MMS_COMPAT$API).getMethod("isEnabled").invoke(null);
        } catch (ReflectiveOperationException | LinkageError error) {
            return false;
        }
    }

    @Unique
    private static void mmsCompat$setEnabled(boolean enabled) {
        try {
            Method setter = Class.forName(MMS_COMPAT$API).getMethod("setEnabled", boolean.class);
            setter.invoke(null, enabled);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Perspective API without this switch: leave it as it is.
        }
    }
}
