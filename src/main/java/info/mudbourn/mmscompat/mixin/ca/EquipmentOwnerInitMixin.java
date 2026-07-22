package info.mudbourn.mmscompat.mixin.ca;

import info.mudbourn.mmscompat.duck.EntityEquipmentDuck;
import net.hollowed.combatamenities.util.entities.EntityEquipment;
import net.hollowed.combatamenities.util.interfaces.EquipmentInterface;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stamps the owning LivingEntity onto Combat Amenities' EntityEquipment
 * instance at entity construction time. This enables the Trinkets redirect
 * to resolve the TrinketComponent from the entity.
 */
@Pseudo
@Mixin(targets = "net.minecraft.world.entity.LivingEntity", remap = true)
public class EquipmentOwnerInitMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsCompat$stampEquipmentOwner(CallbackInfo ci) {
        try {
            EntityEquipment eq = ((EquipmentInterface) this).combat_Amenities$getEquipment();
            if (eq != null) {
                ((EntityEquipmentDuck) eq).mms_compat$setOwner((LivingEntity) (Object) this);
            }
        } catch (Throwable ignored) {
            // EquipmentInterface duck may not be present on all LivingEntity subclasses
        }
    }
}
