package info.mudbourn.mmscompat.mixin.metro;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.config.MetroConfig;
import info.mudbourn.mmscompat.MetroSpeedScale;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Speed-awareness patch for ModMetro carts.
 *
 * ModMetro's automation logic is written against vanilla 0.4 b/t.
 * ACE raises the minecart speed cap via the behavior class, bypassing
 * ModMetro's entity-level getMaxSpeed override. Every downstream
 * calculation (braking, search radius, spacing) still assumes 0.4.
 *
 * This mixin scales only kinematic quantities (speed caps, braking ramp
 * length, lookahead distance). Geometry and standoff distances (1.5d,
 * 3.0d) are NOT scaled — see spec §3e for rationale.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroCartSpeedMixin {

    // ===== Max speed alignment =====

    /**
     * Replace the vanilla baseline 0.4 in {@code Math.max(0.4, configSpeed)}
     * with the effective max speed from ACE gamerules.
     */
    @ModifyConstant(method = "getMaxSpeed", constant = @Constant(doubleValue = 0.4))
    private double mmsCompat$scaleMaxSpeed(double original) {
        return MetroSpeedScale.effectiveMaxSpeed((MetroCartEntity)(Object)this);
    }

    /**
     * Same for the ServerLevel overload (method_7504 → getMaxSpeed(ServerLevel) after remap).
     */
    @ModifyConstant(method = "getMaxSpeed(Lnet/minecraft/server/level/ServerLevel;)D", constant = @Constant(doubleValue = 0.4))
    private double mmsCompat$scaleServerMaxSpeed(double original, ServerLevel world) {
        return MetroSpeedScale.effectiveMaxSpeed((MetroCartEntity)(Object)this);
    }

    // ===== Brake distance scaling =====

    /**
     * Scale {@code MetroConfig.brake_distance} reads in {@code applyProximityBraking}.
     * Three field accesses: the threshold check (ordinal 0), the ramp denominator
     * (ordinal 1), and the rail-connection search depth (ordinal 2).
     * {@code brake_distance} is a static field (getstatic), not a constant.
     */
    @Redirect(
        method = "applyProximityBraking",
        at = @At(
            value = "FIELD",
            target = "Lcom/example/modmetro/config/MetroConfig;brake_distance:D",
            ordinal = 0
        )
    )
    private double mmsCompat$scaleBrakeDistanceCheck() {
        return MetroConfig.brake_distance * MetroSpeedScale.scale((MetroCartEntity)(Object)this);
    }

    @Redirect(
        method = "applyProximityBraking",
        at = @At(
            value = "FIELD",
            target = "Lcom/example/modmetro/config/MetroConfig;brake_distance:D",
            ordinal = 1
        )
    )
    private double mmsCompat$scaleBrakeDistanceDenom() {
        return MetroConfig.brake_distance * MetroSpeedScale.scale((MetroCartEntity)(Object)this);
    }

    @Redirect(
        method = "applyProximityBraking",
        at = @At(
            value = "FIELD",
            target = "Lcom/example/modmetro/config/MetroConfig;brake_distance:D",
            ordinal = 2
        )
    )
    private double mmsCompat$scaleBrakeDistanceSearch() {
        return MetroConfig.brake_distance * MetroSpeedScale.scale((MetroCartEntity)(Object)this);
    }

    // ===== Search radius (squared distance in lambda) =====

    /**
     * Scale the 625.0 squared-distance search radius (25 blocks) in the
     * entity filter lambda. Since it's a squared distance, scale by scale².
     */
    @ModifyConstant(method = "lambda$applyProximityBraking$1", constant = @Constant(doubleValue = 625.0))
    private double mmsCompat$scaleSearchRadiusSq(double original) {
        double s = MetroSpeedScale.scale((MetroCartEntity)(Object)this);
        return original * s * s;
    }
}
