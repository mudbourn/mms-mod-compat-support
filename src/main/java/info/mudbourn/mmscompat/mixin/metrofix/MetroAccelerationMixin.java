package info.mudbourn.mmscompat.mixin.metrofix;

import com.example.modmetro.MetroCartEntity;
import info.mudbourn.mmscompat.metro.MetroTuning;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Overrides ModMetro's hardcoded 1.1× per-tick acceleration multiplier
 * with the configurable {@link MetroTuning#acceleration_factor}.
 *
 * <p>ModMetro accelerates lead carts by multiplying velocity by 1.1 each tick
 * until reaching {@code MetroConfig.speed}.  A lower factor (e.g. 1.02)
 * means a longer, smoother ramp-up after a station dwell.
 *
 * <p>Note: the acceleration logic lives in {@code tickLeadCart()}, not
 * {@code tick()} — the latter delegates to tickLeadCart / tickFollowerCart.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroAccelerationMixin {

    /**
     * Replace the hardcoded 1.1 acceleration multiplier in tickLeadCart.
     * Only one occurrence exists in the refactored MetroCartEntity.
     */
    @ModifyConstant(
        method = "tickLeadCart",
        constant = @Constant(doubleValue = 1.1),
        require = 1
    )
    private double mmsCompat$accelerationFactor(double original) {
        return MetroTuning.acceleration_factor;
    }
}
