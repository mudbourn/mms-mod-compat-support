package info.mudbourn.mmscompat.mixin.metrofix.client;

import com.example.modmetro.MetroClient;
import info.mudbourn.mmscompat.MetroText;
import info.mudbourn.mmscompat.client.MetroLineSyncClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
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

    /** HUD accent (top bar + title): stock orange 0xFFFF8800 → white, matching the station screen restyle. */
    @ModifyConstant(method = "renderMetroHUD", constant = @Constant(intValue = -30720))
    private int mmsCompat$hudAccent(int c) {
        return 0xFFFFFFFF;
    }

    /**
     * Drop the HUD from y=20 to y=60 so it clears WTHIT's top-center tooltip
     * — in tunnel sections the player is almost always looking at a block, so
     * at stock position the two overlap near-constantly. Every fill and text
     * offset derives from this one local, so a single shift moves the whole
     * box. ordinal 0 pins the y assignment; the other literal 20 in this
     * method is the ticks-per-second divisor in the wait countdown.
     */
    @ModifyConstant(method = "renderMetroHUD", constant = @Constant(intValue = 20, ordinal = 0))
    private int mmsCompat$hudY(int y) {
        return 60;
    }

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

    /**
     * Title line. Stock text is "🚇 METRO VILLA STARSHIP - Vagon #N"; we show
     * the line instead of the car number — riders care which line they're on,
     * not which car of the train they boarded. The line name comes from our
     * own sync (ModMetro never sends it to clients); until the first payload
     * lands (~1s after boarding) the title is just the brand.
     */
    @Redirect(
        method = "renderMetroHUD",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString"
                   + "(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
        )
    )
    private void mmsCompat$rewriteHudTitle(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        if (text != null && text.startsWith("🚇 METRO VILLA STARSHIP")) {
            String title = "🚇 " + MetroText.tr("mms_compat.metro.gui.brand");
            Entity vehicle = Minecraft.getInstance().player != null
                    ? Minecraft.getInstance().player.getVehicle() : null;
            String line = vehicle != null ? MetroLineSyncClient.lineFor(vehicle.getId()) : "";
            if (!line.isEmpty()) {
                title += " - " + MetroText.tr("mms_compat.metro.gui.line") + " " + line;
            }
            graphics.drawCenteredString(font, title, x, y, color);
            return;
        }
        graphics.drawCenteredString(font, MetroText.rewriteHud(text), x, y, color);
    }
}
