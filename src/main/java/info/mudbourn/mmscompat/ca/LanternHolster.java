package info.mudbourn.mmscompat.ca;

import com.github.thedeathlycow.frostiful.config.FrostifulConfigYACL;
import com.github.thedeathlycow.thermoo.api.core.v2.TemperatureAware;
import com.github.thedeathlycow.thermoo.api.entity.v1.ThermooAttributes;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

// Makes a light source in the Combat Amenities belt slot count as placed light for Frostiful, heats any living wearer faster, and burns one item per day of use
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

    private static final Set<LivingEntity> WEARERS = Collections.newSetFromMap(new WeakHashMap<>());

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
            Iterator<LivingEntity> wearers = WEARERS.iterator();
            while (wearers.hasNext()) {
                LivingEntity wearer = wearers.next();
                updateHeatPenalty(wearer);
                burnDown(wearer);
                if (wearer.isRemoved() || !isHolstering(wearer)) {
                    wearers.remove();
                }
            }
        });
    }

    // Frostiful's light warmth at the wearer's feet, raised to the holstered light's warmth
    public static int withHolster(LivingEntity wearer, int placedWarmth) {
        if (!(wearer instanceof Player) && BeltLight.of(wearer) > 0) {
            WEARERS.add(wearer);
        }
        return Math.max(placedWarmth, lightWarmth(BeltLight.of(wearer)));
    }

    // Whether the entity carries a belt light or still has the heat penalty from one
    private static boolean isHolstering(LivingEntity entity) {
        if (BeltLight.of(entity) > 0) {
            return true;
        }
        AttributeInstance resistance = entity.getAttribute(ThermooAttributes.HEAT_RESISTANCE);
        return resistance != null && resistance.hasModifier(HEAT_PENALTY_ID);
    }

    private static int lightWarmth(int light) {
        int min = FrostifulConfigYACL.environmentSettings().minLightForWarmth();
        return light < min ? 0 : FrostifulConfigYACL.temperatureSourceSettings().lightTemperatureChangeForLevel(light - min);
    }

    private static void updateHeatPenalty(LivingEntity wearer) {
        AttributeInstance resistance = wearer.getAttribute(ThermooAttributes.HEAT_RESISTANCE);
        if (resistance == null) {
            return;
        }
        boolean holstered = BeltLight.of(wearer) > 0;
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

    // Counts time the holster is the light keeping the wearer warm, and spends one item per day
    private static void burnDown(LivingEntity wearer) {
        if (wearer.isSpectator() || TemperatureAware.get(wearer).thermoo$getTemperature() > 0) {
            return;
        }
        EquipmentSlot slot = BeltLight.slot();
        if (slot == null) {
            return;
        }
        ItemStack stack = wearer.getItemBySlot(slot);
        int carried = lightWarmth(BeltLight.lightOf(stack));
        int ambient = lightWarmth(wearer.level().getBrightness(LightLayer.BLOCK, wearer.blockPosition()));
        if (carried <= ambient) {
            return;
        }
        int warded = wearer.getAttachedOrElse(WARDED_TICKS, 0) + CHECK_INTERVAL;
        if (warded < BURN_TICKS) {
            wearer.setAttached(WARDED_TICKS, warded);
            return;
        }
        wearer.removeAttached(WARDED_TICKS);
        wearer.setItemSlot(slot, stack.copyWithCount(stack.getCount() - 1));
        wearer.level().playSound(
            null,
            wearer.blockPosition(),
            SoundEvents.FIRE_EXTINGUISH,
            wearer.getSoundSource(),
            0.35F,
            1.0F);
    }
}
