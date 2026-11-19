package info.mudbourn.mmscompat.mixin.immersivestorms;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.thedeathlycow.immersive.storms.client.world.BlackRainEffect;
import com.thedeathlycow.immersive.storms.registry.ISParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Swaps rain splash particles for black rain ones in black rain areas.
@Mixin(ClientLevel.class)
public abstract class BlackRainParticleMixin {

    @WrapOperation(
        method = "tickWeatherEffects",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private void mmsCompat$blackRainParticle(
        ClientLevel level,
        ParticleOptions particle,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        Operation<Void> original,
        @Local(name = "rainParticlePosition") BlockPos pos
    ) {
        ParticleOptions chosen = particle == ParticleTypes.RAIN && BlackRainEffect.isBlackRain(level, pos)
            ? ISParticleTypes.BLACK_RAIN
            : particle;

        original.call(level, chosen, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
