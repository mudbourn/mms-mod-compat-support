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
 * <p>ModMetro accelerates carts by multiplying velocity by 1.1 each tick
 * until reaching {@code MetroConfig.speed}.  A lower factor (e.g. 1.02)
 * means a longer, smoother ramp-up after a station dwell.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroAccelerationMixin {

    /**
     * Replace the hardcoded 1.1 acceleration multiplier.
     * There are two occurrences in the tick method — one for the main
     * acceleration branch, one for the clamped branch.  Both must match.
     */
    @ModifyConstant(
        method = "tick",
        constant = @Constant(doubleValue = 1.1),
        require = 2
    )
    private double mmsCompat$accelerationFactor(double original) {
        return MetroTuning.acceleration_factor;
    }
}
