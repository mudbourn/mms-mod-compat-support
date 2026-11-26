package info.mudbourn.mmscompat.ca;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// Marks the entity taking damage that has no attacker, such as fire or a fall, while Combat Amenities checks for a parry.
public final class UnparryableDamage {

    private static final ThreadLocal<LivingEntity> MARKED = new ThreadLocal<>();

    private UnparryableDamage() {
    }

    public static void mark(LivingEntity entity, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_SHIELD) || source.getEntity() == null && source.getDirectEntity() == null) {
            MARKED.set(entity);
        }
    }

    public static void clear() {
        MARKED.remove();
    }

    public static boolean marked(LivingEntity entity) {
        return MARKED.get() == entity;
    }
}
