package info.mudbourn.mmscompat.mixin.iris;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

// Skips the repeated background blur in Iris's feature-missing screen.
@Pseudo
@Mixin(targets = "net.irisshaders.iris.gui.FeatureMissingErrorScreen", remap = false)
public class IrisFeatureMissingBlurMixin {

    @Redirect(
        method = "method_25394",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/gui/FeatureMissingErrorScreen;"
                   + "method_25420(Lnet/minecraft/class_332;IIF)V"
        ),
        remap = false
    )
    private void mms$skipSecondBlur(@Coerce Screen self, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }
}
