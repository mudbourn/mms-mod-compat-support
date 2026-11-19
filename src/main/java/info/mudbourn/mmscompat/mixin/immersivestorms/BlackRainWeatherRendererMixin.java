package info.mudbourn.mmscompat.mixin.immersivestorms;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thedeathlycow.immersive.storms.client.world.BlackRainEffect;
import com.thedeathlycow.immersive.storms.client.world.WeatherRenderStateExtension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.WeatherEffectRenderer.ColumnInstance;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Tints black rain columns, on the 26.3 weather renderer that Immersive Storms 2.0 beta does not match.
@Mixin(WeatherEffectRenderer.class)
public abstract class BlackRainWeatherRendererMixin {

    @Unique
    private final ThreadLocal<WeatherRenderState> mmsCompat$renderState = new ThreadLocal<>();

    @WrapOperation(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;")
    )
    private Precipitation mmsCompat$checkBlackRain(
        ClientLevel level,
        BlockPos pos,
        Operation<Precipitation> original,
        @Share(value = "is_black_rain", namespace = "mms_compat") LocalBooleanRef isBlackRain
    ) {
        Precipitation precipitation = original.call(level, pos);
        isBlackRain.set(precipitation == Precipitation.RAIN && BlackRainEffect.isBlackRain(level, pos));

        return precipitation;
    }

    @WrapOperation(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WeatherEffectRenderer;createRainColumnInstance(Lnet/minecraft/util/RandomSource;JIIIIIF)Lnet/minecraft/client/renderer/WeatherEffectRenderer$ColumnInstance;")
    )
    private ColumnInstance mmsCompat$recordBlackRainColumn(
        WeatherEffectRenderer renderer,
        RandomSource random,
        long ticks,
        int x,
        int bottomY,
        int topY,
        int z,
        int lightCoords,
        float partialTicks,
        Operation<ColumnInstance> original,
        @Share(value = "is_black_rain", namespace = "mms_compat") LocalBooleanRef isBlackRain,
        @Local(argsOnly = true) WeatherRenderState renderState
    ) {
        ColumnInstance column = original.call(renderer, random, ticks, x, bottomY, topY, z, lightCoords, partialTicks);
        if (isBlackRain.get()) {
            ((WeatherRenderStateExtension) renderState).immersiveStorms$addBlackRainInstance(column);
        }

        return column;
    }

    @Inject(method = "prepare", at = @At("HEAD"))
    private void mmsCompat$captureRenderState(Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
        mmsCompat$renderState.set(renderState);
    }

    @Inject(method = "prepare", at = @At("TAIL"))
    private void mmsCompat$clearRenderState(Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
        mmsCompat$renderState.remove();
    }

    @WrapOperation(
        method = "prepareInstances",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer mmsCompat$tintBlackRain(
        VertexConsumer consumer,
        int color,
        Operation<VertexConsumer> original,
        @Local(name = "column") ColumnInstance column
    ) {
        WeatherRenderState renderState = mmsCompat$renderState.get();
        boolean blackRain = renderState != null
            && ((WeatherRenderStateExtension) renderState).immersiveStorms$isBlackRainInstance(column);

        return original.call(consumer, blackRain ? ARGB.color(ARGB.alpha(color), 32, 32, 32) : color);
    }
}
