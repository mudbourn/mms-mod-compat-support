package info.mudbourn.mmscompat.mixin.iris;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/irisshaders/iris/gui/FeatureMissingErrorScreen;"
                   + "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
        ),
        remap = false
    )
    private void mms$skipSecondBlur(@Coerce Screen self, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }
}
