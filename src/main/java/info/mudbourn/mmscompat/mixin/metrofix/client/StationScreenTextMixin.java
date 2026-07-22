package info.mudbourn.mmscompat.mixin.metrofix.client;

import com.example.modmetro.client.StationScreen;
import info.mudbourn.mmscompat.MetroText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Localizes the station configuration GUI.
 *
 * Every label here is a plain ldc constant handed to Component.literal,
 * so @ModifyConstant reaches all of them cleanly.
 *
 * Deliberately NOT touched:
 *  - "Estación" / "Línea 1" / "Terminal" in <init>: these are the initial
 *    *field values* mirrored from StationBlockEntity, i.e. persisted NBT
 *    data, not labels. Translating them would write English defaults back
 *    into saved station data and desync client from server.
 *  - "continue" / "reverse": action state values stored in NBT.
 *  - "METRO VILLA STARSHIP": branding.
 *
 * Note the double spaces after the ✔ ✖ ▶ ◀ ⚠ glyphs are load-bearing —
 * the constants must match byte-for-byte or the injector fails at load.
 */
@Mixin(StationScreen.class)
public abstract class StationScreenTextMixin {

    @ModifyConstant(method = "<init>", constant = @Constant(stringValue = "Configurar Estación"))
    private static String mmsCompat$title(String original) {
        return MetroText.tr("mms_compat.metro.gui.title");
    }

    @ModifyConstant(method = "init", constant = @Constant(stringValue = "✔  Guardar"))
    private String mmsCompat$save(String original) {
        return "✔  " + MetroText.tr("mms_compat.metro.gui.save");
    }

    @ModifyConstant(method = "init", constant = @Constant(stringValue = "✖  Cancelar"))
    private String mmsCompat$cancel(String original) {
        return "✖  " + MetroText.tr("mms_compat.metro.gui.cancel");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Configuración de Parada"))
    private String mmsCompat$heading(String original) {
        return MetroText.tr("mms_compat.metro.gui.heading");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Nombre de la Estación"))
    private String mmsCompat$stationName(String original) {
        return MetroText.tr("mms_compat.metro.gui.station_name");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Tiempo de Espera  (segundos)"))
    private String mmsCompat$waitTime(String original) {
        return MetroText.tr("mms_compat.metro.gui.wait_time");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Próxima Estación"))
    private String mmsCompat$nextStation(String original) {
        return MetroText.tr("mms_compat.metro.gui.next_station");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "Línea"))
    private String mmsCompat$line(String original) {
        return MetroText.tr("mms_compat.metro.gui.line");
    }

    @ModifyConstant(method = "render", constant = @Constant(stringValue = "⚠  Ingresa un valor entre 1 y 9999"))
    private String mmsCompat$rangeWarning(String original) {
        return "⚠  " + MetroText.tr("mms_compat.metro.gui.range_warning");
    }

    @ModifyConstant(method = "buildActionText",
        constant = @Constant(stringValue = "▶  Acción: Continuar hacia siguiente"))
    private String mmsCompat$actionContinue(String original) {
        return "▶  " + MetroText.tr("mms_compat.metro.gui.action_continue");
    }

    @ModifyConstant(method = "buildActionText",
        constant = @Constant(stringValue = "◀  Acción: Invertir dirección"))
    private String mmsCompat$actionReverse(String original) {
        return "◀  " + MetroText.tr("mms_compat.metro.gui.action_reverse");
    }
}
