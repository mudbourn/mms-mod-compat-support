package info.mudbourn.mmscompat.mixin.combatamenities;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Combat Amenities adds back and belt slots to EquipmentSlot, but vanilla's bySlot switch
// has no case for them and throws MatchException. Alex's Caves walks every slot for item
// tooltips, so hovering an item crashed the client. Extra slots map to Combat Amenities'
// own group, or to ANY if that group is missing.
@Mixin(EquipmentSlotGroup.class)
public abstract class ExtraSlotGroupMixin {
    @Unique
    private static EquipmentSlotGroup mmsCompat$extraGroup;

    @Inject(method = "bySlot", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$extraSlot(EquipmentSlot slot, CallbackInfoReturnable<EquipmentSlotGroup> cir) {
        if (slot.ordinal() <= EquipmentSlot.SADDLE.ordinal()) {
            return;
        }
        if (mmsCompat$extraGroup == null) {
            EquipmentSlotGroup group = EquipmentSlotGroup.ANY;
            for (EquipmentSlotGroup candidate : EquipmentSlotGroup.values()) {
                if (candidate.name().equals("COMBATAMENITIES_SLOT")) {
                    group = candidate;
                }
            }
            mmsCompat$extraGroup = group;
        }
        cir.setReturnValue(mmsCompat$extraGroup);
    }
}
