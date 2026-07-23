package info.mudbourn.mmscompat.mixin.metrofix.client;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.MetroClient;
import info.mudbourn.mmscompat.MetroText;
import info.mudbourn.mmscompat.client.MetroLineSyncClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    /** Stock yellow of the "Prox:" line — identifies it among the drawString calls. */
    @Unique
    private static final int PROX_COLOR = 0xFFFF55;

    /** Vehicle the cached next-stop belongs to; a remount resets the cache. */
    @Unique
    private int mmsCompat$nextVehicleId = -1;
    @Unique
    private String mmsCompat$nextStop = "";

    /**
     * Persistent next-stop line. Stock only draws "Prox:" while the cart's
     * synced NEXT_STATION is non-empty — it blanks between a reverse and the
     * following stop, and reads "reverse" while turning around. We suppress
     * the stock line (matched by its yellow color) and always draw our own
     * from a cache of the last real station name, updated whenever the cart
     * syncs a new one (i.e. every stop it reaches).
     */
    @Redirect(
        method = "renderMetroHUD",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString"
                   + "(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"
        )
    )
    private void mmsCompat$rewriteHudLine(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        if (color == PROX_COLOR) {
            return; // stock Prox line — replaced by the persistent one below
        }
        graphics.drawString(font, MetroText.rewriteHud(text), x, y, color);
        // The white status line ("Station:"/"In transit...") draws every
        // frame, so it anchors our next-stop line 12px beneath it — the
        // stock Prox position.
        if (color == -1) {
            Entity vehicle = Minecraft.getInstance().player != null
                    ? Minecraft.getInstance().player.getVehicle() : null;
            if (vehicle instanceof MetroCartEntity cart) {
                if (vehicle.getId() != this.mmsCompat$nextVehicleId) {
                    this.mmsCompat$nextVehicleId = vehicle.getId();
                    this.mmsCompat$nextStop = "";
                }
                String next = cart.getNextStation();
                if (!next.isEmpty() && !next.equalsIgnoreCase("reverse")) {
                    this.mmsCompat$nextStop = next;
                }
                if (!this.mmsCompat$nextStop.isEmpty()) {
                    graphics.drawString(font,
                            MetroText.tr("mms_compat.metro.hud.next") + ": " + this.mmsCompat$nextStop,
                            x, y + 12, PROX_COLOR);
                }
            }
        }
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
                // The line NAME is builder-authored and usually already carries
                // its own label ("Line 1", "Línea 1"), so prefixing the
                // translated label unconditionally rendered "Line Line 1".
                // Only label a bare name.
                String label = MetroText.tr("mms_compat.metro.gui.line");
                title += " - " + (mmsCompat$alreadyLabelled(line, label) ? line : label + " " + line);
            }
            graphics.drawCenteredString(font, title, x, y, color);
            return;
        }
        graphics.drawCenteredString(font, MetroText.rewriteHud(text), x, y, color);
    }

    /**
     * True if a builder-authored line name already begins with its own label,
     * in either shipped language, so it should not be labelled again.
     */
    @Unique
    private static boolean mmsCompat$alreadyLabelled(String line, String label) {
        String l = line.trim().toLowerCase(java.util.Locale.ROOT);
        return l.startsWith(label.trim().toLowerCase(java.util.Locale.ROOT))
                || l.startsWith("line") || l.startsWith("l\u00ednea") || l.startsWith("linea");
    }
}
