package info.mudbourn.mmscompat.mixin.aerialhell;

import fr.factionbedrock.aerialhell.Item.Ability.DamageUseSituationInfo;
import fr.factionbedrock.aerialhell.Item.Ability.MiningUseSituationInfo;
import fr.factionbedrock.aerialhell.Item.Ability.UsingItemUseSituationInfo;
import fr.factionbedrock.aerialhell.Registry.AerialHellItemAbilities;
import info.mudbourn.mmscompat.aerialhell.TrinketTotems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// IN_MAIN_OR_OFF_HAND is lambda$static$1 in Aerial Hell 0.7.9: true when the stack
// matches the main or off hand item. A totem worn in a trinket slot passes while
// TrinketTotems is ticking it.
@Mixin(AerialHellItemAbilities.class)
public abstract class TotemHandConditionMixin {
    @Inject(method = "lambda$static$1", at = @At("RETURN"), cancellable = true)
    private static void mmsCompat$wornTotem(ItemStack stack, LivingEntity entity, EquipmentSlot slot,
                                            UsingItemUseSituationInfo using, DamageUseSituationInfo damage,
                                            MiningUseSituationInfo mining, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && TrinketTotems.isTicking(stack)) {
            cir.setReturnValue(true);
        }
    }
}
