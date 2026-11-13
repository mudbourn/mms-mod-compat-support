package info.mudbourn.mmscompat.ca;

import com.github.thedeathlycow.frostiful.Frostiful;
import com.github.thedeathlycow.frostiful.config.group.EnvironmentConfigGroup;
import com.github.thedeathlycow.thermoo.api.ThermooAttributes;
import com.github.thedeathlycow.thermoo.api.temperature.TemperatureAware;
import com.mojang.serialization.Codec;
import dev.emi.trinkets.api.TrinketInventory;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

/**
 * Makes a light source in the Trinkets belt slot (Combat Amenities' belt) count
 * as placed light for Frostiful, heats its wearer faster, and burns one item
 * out of the stack after one day of keeping the cold away. The burn progress
 * syncs to its owner to draw a durability bar on the belt stack.
 */
public final class LanternHolster {

    private static final Identifier HEAT_PENALTY_ID =
        Identifier.fromNamespaceAndPath("mms_compat", "lantern_holster_heat");

    private static final double HEAT_PENALTY = -3.0;

    private static final int CHECK_INTERVAL = 20;

    public static final int BURN_TICKS = 24000;

    public static final AttachmentType<Integer> WARDED_TICKS = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath("mms_compat", "lantern_holster_warded"),
        builder -> builder
            .persistent(Codec.INT)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()));

    private LanternHolster() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % CHECK_INTERVAL != 0) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updateHeatPenalty(player);
                burnDown(player);
            }
        });
    }

    /**
     * Frostiful's light warmth at the wearer's feet, raised to what the
     * holstered light would give if placed there.
     */
    public static int withHolster(ServerPlayer player, int placedWarmth) {
        return Math.max(placedWarmth, lightWarmth(BeltLight.of(player)));
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

    // Counts time the holster is the light keeping the wearer warm, and spends one item per day.
    private static void burnDown(ServerPlayer player) {
        if (player.isSpectator() || TemperatureAware.get(player).thermoo$getTemperature() > 0) {
            return;
        }
        TrinketInventory slot = BeltLight.slot(player);
        if (slot == null) {
            return;
        }
        ItemStack stack = slot.getItem(0);
        int carried = lightWarmth(BeltLight.lightOf(stack));
        int ambient = lightWarmth(player.level().getBrightness(LightLayer.BLOCK, player.blockPosition()));
        if (carried <= ambient) {
            return;
        }
        int warded = player.getAttachedOrElse(WARDED_TICKS, 0) + CHECK_INTERVAL;
        if (warded < BURN_TICKS) {
            player.setAttached(WARDED_TICKS, warded);
            return;
        }
        player.removeAttached(WARDED_TICKS);
        slot.setItem(0, stack.copyWithCount(stack.getCount() - 1));
        player.level().playSound(
            null,
            player.blockPosition(),
            SoundEvents.FIRE_EXTINGUISH,
            SoundSource.PLAYERS,
            0.35F,
            1.0F);
    }
}
