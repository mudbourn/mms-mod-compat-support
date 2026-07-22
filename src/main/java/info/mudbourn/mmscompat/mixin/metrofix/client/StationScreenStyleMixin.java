package info.mudbourn.mmscompat.mixin.metrofix.client;

import com.example.modmetro.client.StationScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Restyles the station configuration GUI to the MMS palette.
 *
 * ModMetro's stock palette is navy-purple panels (0xFF0D0D22 / 0xFF12122E)
 * with a saturated orange accent (0xFFFF8800) and green/red action-button
 * fills — jarring and nothing like vanilla. Replaced with the MMS deepslate
 * + copper scheme: near-neutral dark grays, one muted copper accent.
 *
 * StationScreen declares the palette as private static final int fields, but
 * javac inlines those into each call site, so the fields are unreachable and
 * every color is matched as an ldc constant per method (@ModifyConstant
 * rewrites all occurrences within the named method). Error red/white text
 * and the translucent backdrop are kept.
 *
 * Same build-pinning caveat as MetroCommandTextMixin: constants are tied to
 * modmetro-v1.jar (1.0.0, md5 28046740b19462b86441dbb44e25fe64);
 * defaultRequire=1 makes a mismatch fail loudly at load.
 */
@Mixin(StationScreen.class)
public abstract class StationScreenStyleMixin {

    // ── panels ──────────────────────────────────────────────────────────

    /** panel body: navy 0xFF0D0D22 → deepslate */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -15921886))
    private int mmsCompat$panel(int c) {
        return 0xFF1D1D21;
    }

    /** header band: navy 0xFF12122E → slightly lifted deepslate */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -15592914))
    private int mmsCompat$header(int c) {
        return 0xFF26262B;
    }

    /** dividers + inactive dots: purple-navy 0xFF2A2A55 → neutral */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -14013867))
    private int mmsCompat$divider(int c) {
        return 0xFF333338;
    }

    // ── accent (left bar, header rule, live dot, title, focus ring) ─────

    /** loud orange 0xFFFF8800 → muted copper */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -30720))
    private int mmsCompat$accentRender(int c) {
        return 0xFFC77B50;
    }

    @ModifyConstant(method = "drawField", constant = @Constant(intValue = -30720))
    private int mmsCompat$accentFocus(int c) {
        return 0xFFC77B50;
    }

    // ── text ────────────────────────────────────────────────────────────

    /** labels: lavender-gray 0xFFBBBBCC → neutral light gray */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -4473908))
    private int mmsCompat$labelRender(int c) {
        return 0xFFB8B8BD;
    }

    @ModifyConstant(method = "drawField", constant = @Constant(intValue = -4473908))
    private int mmsCompat$labelField(int c) {
        return 0xFFB8B8BD;
    }

    /** footer coords: blue-gray 0xFF777799 → neutral gray */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -8947815))
    private int mmsCompat$footer(int c) {
        return 0xFF8A8A8F;
    }

    // ── fields ──────────────────────────────────────────────────────────

    /** field background: navy 0xFF0A0A1E → near-black */
    @ModifyConstant(method = "drawField", constant = @Constant(intValue = -16119266))
    private int mmsCompat$fieldBg(int c) {
        return 0xFF111114;
    }

    /** field border (unfocused): steel-blue 0xFF2A3A5A → neutral */
    @ModifyConstant(method = "drawField", constant = @Constant(intValue = -14009766))
    private int mmsCompat$fieldBorder(int c) {
        return 0xFF45454A;
    }

    // ── action button fills (drawn under the vanilla button widget) ─────

    /** continue state: green 0xFF1A5C1A → desaturated green-gray */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -15049702))
    private int mmsCompat$btnContinue(int c) {
        return 0xFF2E3B2E;
    }

    /** reverse state: red 0xFF5C1A1A → desaturated red-gray */
    @ModifyConstant(method = "render", constant = @Constant(intValue = -10741222))
    private int mmsCompat$btnReverse(int c) {
        return 0xFF3B2E2E;
    }
}
