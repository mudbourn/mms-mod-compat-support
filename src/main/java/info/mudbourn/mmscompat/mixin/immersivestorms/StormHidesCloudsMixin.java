package info.mudbourn.mmscompat.mixin.immersivestorms;

import com.thedeathlycow.immersive.storms.client.world.WeatherArchetype;
import com.thedeathlycow.immersive.storms.registry.ISEnvironmentAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

// Better Clouds draws its clouds with its own fog distance, so the short fog Immersive Storms
// sets for a sandstorm or blizzard (FogData.cloudEnd) never reaches them and the cloud layer
// shows through the dust. While the camera is in such a storm and the rain is past a light
// drizzle (the fog is already thick by then, so the cut is hidden), the clouds are skipped;
// returning true tells Better Clouds' caller they were drawn, so vanilla clouds stay off too.
@Pseudo
@Mixin(targets = "com.qendolin.betterclouds.rendering.CloudRenderCoordinator", remap = false)
public abstract class StormHidesCloudsMixin {

    private static final float MMS_RAIN_THRESHOLD = 0.3F;

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true, require = 0)
    private void mmsCompat$hideInStorm(Consumer<?> passes, Vec3 camera, float partialTick, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.level.getRainLevel(partialTick) < MMS_RAIN_THRESHOLD) return;
        WeatherArchetype weather = client.gameRenderer.mainCamera().attributeProbe()
            .getValue(ISEnvironmentAttributes.WEATHER_ARCHETYPE, partialTick);
        if (weather == WeatherArchetype.SANDSTORM || weather == WeatherArchetype.BLIZZARD) {
            cir.setReturnValue(true);
        }
    }
}
