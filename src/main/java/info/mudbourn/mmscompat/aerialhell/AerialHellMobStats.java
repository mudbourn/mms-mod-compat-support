package info.mudbourn.mmscompat.aerialhell;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

/**
 * Aerial Hell mobs that kept near-vanilla stats, raised to sit with the rest of the dimension.
 * Each one takes its base stats from a parent class (a zombie-like humanoid, the evil cow, the regular
 * pirate or mud mob), so Aerial Hell never tuned them on their own.
 *
 * <p>Stats are set when the mob loads. A mob that was already hurt keeps the same share of its health.
 */
public final class AerialHellMobStats {

    private record Stats(double health, double damage, double armor) {
    }

    private static final Map<Identifier, Stats> STATS = Map.of(
        id("mummy"), new Stats(35.0, 5.0, 2.0),                  // was 22 / 3 / 0, next to the 40 / 5 verdigris zombie
        id("venomous_snake"), new Stats(24.0, 4.0, 0.0),         // was 20 / 3; its poison is unchanged
        id("cortinarius_cow"), new Stats(30.0, 4.0, 0.0),        // was 20 / 3, the evil cow's stats
        id("slime_pirate"), new Stats(32.0, 6.0, 0.0),           // was 20 / 4
        id("ghost_slime_pirate"), new Stats(32.0, 6.0, 0.0),     // was 20 / 4
        id("slime_ninja_pirate"), new Stats(26.0, 6.0, 0.0),     // was 18 / 4; stays faster than the plain pirate
        id("ghost_slime_ninja_pirate"), new Stats(26.0, 6.0, 0.0), // was 18 / 4
        id("mud_spectral_soldier"), new Stats(15.0, 4.0, 0.0),   // was 10 / 3; stays weaker than the 20 / 3 mud soldier it copies
        id("mud_spectral_golem"), new Stats(30.0, 6.0, 3.0),     // was 15 / 5, armor 3; half the 60 health mud golem
        id("mud_spectral_cycle_mage"), new Stats(40.0, 6.0, 0.0) // was 25 / 5
    );

    private AerialHellMobStats() {
    }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LivingEntity living)) {
                return;
            }
            Stats stats = STATS.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
            if (stats == null) {
                return;
            }
            float share = living.getHealth() / living.getMaxHealth();
            boolean changed = set(living, Attributes.MAX_HEALTH, stats.health());
            set(living, Attributes.ATTACK_DAMAGE, stats.damage());
            set(living, Attributes.ARMOR, stats.armor());
            if (changed) {
                living.setHealth(share * living.getMaxHealth());
            }
        });
    }

    private static boolean set(LivingEntity living, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null || instance.getBaseValue() == value) {
            return false;
        }
        instance.setBaseValue(value);
        return true;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("aerialhell", path);
    }
}
