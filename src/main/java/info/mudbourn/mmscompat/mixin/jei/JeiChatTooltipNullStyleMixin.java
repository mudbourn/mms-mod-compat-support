package info.mudbourn.mmscompat.mixin.jei;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Returns no tooltip when JEI is handed a null style by a screen that draws text without one. */
@Pseudo
@Mixin(targets = "mezz.jei.gui.chat.ChatIngredientTooltip", remap = false)
public class JeiChatTooltipNullStyleMixin {

    @Inject(
        method = "setTooltipForHoveredText",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void mmsCompat$skipNullStyle(GuiGraphicsExtractor context, Style style, int x, int y, CallbackInfoReturnable<Boolean> cir) {
        if (style == null) {
            cir.setReturnValue(false);
        }
    }
}
