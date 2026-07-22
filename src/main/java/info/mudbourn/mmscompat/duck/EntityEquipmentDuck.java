package info.mudbourn.mmscompat.duck;

import net.minecraft.world.entity.LivingEntity;

/**
 * Duck interface stamped onto Combat Amenities' EntityEquipment
 * to back-reference its owning LivingEntity.
 */
public interface EntityEquipmentDuck {
    LivingEntity mms_compat$getOwner();
    void mms_compat$setOwner(LivingEntity owner);
}
