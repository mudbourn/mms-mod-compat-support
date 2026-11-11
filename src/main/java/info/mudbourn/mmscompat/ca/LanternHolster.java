package info.mudbourn.mmscompat.ca;

import com.github.thedeathlycow.frostiful.Frostiful;
import com.github.thedeathlycow.frostiful.config.group.EnvironmentConfigGroup;
import com.github.thedeathlycow.thermoo.api.ThermooAttributes;
import com.github.thedeathlycow.thermoo.api.temperature.TemperatureAware;
import com.github.thedeathlycow.thermoo.api.temperature.event.EnvironmentTickContext;
import com.github.thedeathlycow.thermoo.api.temperature.event.LivingEntityTemperatureTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

/**
 * Lets a light source carried in the Trinkets belt slot (Combat Amenities' belt)
 * warm its wearer like the same light placed beside them, at the cost of
 * heating up faster in hot places.
 */
public final class LanternHolster {

    private static final Identifier HEAT_PENALTY_ID =
        Identifier.fromNamespaceAndPath("mms_compat", "lantern_holster_heat");

    private static final double HEAT_PENALTY = -3.0;

    private static final int CHECK_INTERVAL = 20;

    private LanternHolster() {
    }

    public static void register() {
        LivingEntityTemperatureTickEvents.GET_PASSIVE_TEMPERATURE_CHANGE.register(LanternHolster::warmth);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updateHeatPenalty(player);
            }
        });
    }

    // Tops Frostiful's block-light warmth up to what the holstered light would give.
    private static int warmth(EnvironmentTickContext<? extends LivingEntity> context) {
        LivingEntity entity = context.affected();
        if (!(entity instanceof Player player) || player.isSpectator()) {
            return 0;
        }
        if (TemperatureAware.get(player).thermoo$getTemperature() > 0) {
            return 0;
        }
        int carried = BeltLight.of(player);
        if (carried <= 0) {
            return 0;
        }
        int ambient = context.level().getBrightness(LightLayer.BLOCK, context.pos());
        return Math.max(0, lightWarmth(carried) - lightWarmth(ambient));
    }

    private static int lightWarmth(int light) {
        EnvironmentConfigGroup config = Frostiful.getConfig().environmentConfig;
        int min = config.getMinLightForWarmth();
        return light < min ? 0 : config.getWarmthPerLightLevel() * (light - min);
    }

    private static void updateHeatPenalty(ServerPlayer player) {
        AttributeInstance resistance = player.getAttribute(ThermooAttributes.HEAT_RESISTANCE);
        if (resistance == null) {
            return;
        }
        boolean holstered = BeltLight.of(player) > 0;
        boolean applied = resistance.hasModifier(HEAT_PENALTY_ID);
        if (holstered && !applied) {
            resistance.addTransientModifier(new AttributeModifier(
                HEAT_PENALTY_ID,
                HEAT_PENALTY,
                AttributeModifier.Operation.ADD_VALUE));
        } else if (!holstered && applied) {
            resistance.removeModifier(HEAT_PENALTY_ID);
        }
    }
}
