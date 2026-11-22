package info.mudbourn.mmscompat.ca;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// An attacker whose melee hit is perfect parried cannot attack for STAGGER_TICKS (0.6 s), mobs and players alike.
public final class ParryStagger {

    public static final int STAGGER_TICKS = 12;

    // Game time each staggered attacker may attack again.
    private static final Map<UUID, Long> STAGGERED = new ConcurrentHashMap<>();

    private ParryStagger() {
    }

    public static void stagger(LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        STAGGERED.put(attacker.getUUID(), level.getGameTime() + STAGGER_TICKS);
        level.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getY(1.0) + 0.3, attacker.getZ(), 8, 0.3, 0.1, 0.3, 0.1);
    }

    // Whether this entity is still staggered and must not attack.
    public static boolean staggered(Entity entity) {
        Long until = STAGGERED.get(entity.getUUID());
        if (until == null) {
            return false;
        }
        if (entity.level().getGameTime() >= until) {
            STAGGERED.remove(entity.getUUID());
            return false;
        }
        return true;
    }
}
