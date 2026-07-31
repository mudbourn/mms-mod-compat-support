package info.mudbourn.mmscompat.mixin.jei;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * JEI hooks GuiGraphics#renderComponentHoverEffect to hang its chat item-link
 * tooltip off hovered text, and walks straight into style.getHoverEvent().
 * Vanilla never passes a null style there, so JEI gets away with it — but
 * TRender (tr7zw's LibGui fork, the config screens of EntityCulling/MoreCulling/
 * FirstPerson) calls the same method from WLabel#paint with a null style, and
 * the client dies on hover: NPE in JeiChatItemLinkHover#getIngredientLink.
 *
 * A null style carries no hover event and therefore no item link, so returning
 * "no tooltip drawn" is exactly what JEI would have concluded.
 */
@Pseudo
@Mixin(targets = "mezz.jei.gui.chat.ChatIngredientTooltip", remap = false)
public class JeiChatTooltipNullStyleMixin {

    @Inject(
        method = "setTooltipForHoveredText",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void mmsCompat$skipNullStyle(GuiGraphics context, Style style, int x, int y, CallbackInfoReturnable<Boolean> cir) {
        if (style == null) {
            cir.setReturnValue(false);
        }
    }
}
