package info.mudbourn.mmscompat.mixin.metrofix.client;

import com.example.modmetro.MetroClient;
import info.mudbourn.mmscompat.MetroText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Localizes the in-cart HUD overlay.
 *
 * The HUD draws raw Strings (not Components) built by invokedynamic string
 * concatenation, so the prefixes "Estacion: ", "Prox: ", "⌛ Espera: " and
 * "🚇 ... Vagon #" live in StringConcatFactory recipes rather than ldc
 * instructions — @ModifyConstant cannot see them. We redirect at the draw
 * call instead and rewrite by prefix.
 *
 * "En transito..." IS a real ldc constant (it is concatenated in as the
 * empty-station fallback), so it gets the clean treatment; the surrounding
 * "Estacion: " prefix is then handled by the redirect.
 */
@Mixin(MetroClient.class)
public abstract class MetroHudTextMixin {

    @ModifyConstant(method = "renderMetroHUD", constant = @Constant(stringValue = "En transito..."))
    private String mmsCompat$inTransit(String original) {
        return MetroText.tr("mms_compat.metro.hud.in_transit");
    }

    @Redirect(
        method = "renderMetroHUD",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString"
                   + "(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
        )
    )
    private void mmsCompat$rewriteHudLine(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, MetroText.rewriteHud(text), x, y, color);
    }

    @Redirect(
        method = "renderMetroHUD",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString"
                   + "(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
        )
    )
    private void mmsCompat$rewriteHudTitle(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawCenteredString(font, MetroText.rewriteHud(text), x, y, color);
    }
}
