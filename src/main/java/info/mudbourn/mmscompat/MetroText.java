package info.mudbourn.mmscompat;

import net.minecraft.network.chat.Component;

/**
 * Translation shim for ModMetro's hardcoded Spanish display strings.
 *
 * ModMetro ships no translation keys for its UI text — every label is a
 * literal baked into the class files (see MetroTextMixin family). Rather
 * than hardcoding English replacements, we route through the vanilla
 * translation system against our own lang files, so Spanish-language
 * clients keep the original wording.
 *
 * Client-side callers resolve in the player's locale. Server-side callers
 * (command feedback) resolve in the server's locale, which is en_us.
 */
public final class MetroText {

    private MetroText() {}

    public static String tr(String key) {
        return Component.translatable(key).getString();
    }

    /**
     * Rewrites HUD strings that ModMetro assembles via string concatenation.
     *
     * These are {@code invokedynamic makeConcatWithConstants} recipes, not
     * {@code ldc} constants, so @ModifyConstant cannot reach them. We instead
     * intercept at the draw call and rewrite by prefix. Display-only: the
     * underlying station/line data is never touched.
     */
    public static String rewriteHud(String text) {
        if (text == null || text.isEmpty()) return text;

        String p = "🚇 METRO VILLA STARSHIP - Vagon #";
        if (text.startsWith(p)) {
            return "🚇 " + tr("mms_compat.metro.gui.brand") + " - "
                    + tr("mms_compat.metro.hud.car") + " #" + text.substring(p.length());
        }
        if (text.startsWith("Estacion: ")) {
            return tr("mms_compat.metro.hud.station") + ": " + text.substring("Estacion: ".length());
        }
        if (text.startsWith("Prox: ")) {
            return tr("mms_compat.metro.hud.next") + ": " + text.substring("Prox: ".length());
        }
        if (text.startsWith("⌛ Espera: ") && text.endsWith("s")) {
            String secs = text.substring("⌛ Espera: ".length(), text.length() - 1);
            return "⌛ " + tr("mms_compat.metro.hud.waiting") + ": " + secs + "s";
        }
        return text;
    }
}
