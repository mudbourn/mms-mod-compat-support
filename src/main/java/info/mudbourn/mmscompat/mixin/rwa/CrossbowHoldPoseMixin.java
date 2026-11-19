package info.mudbourn.mmscompat.mixin.rwa;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Holds the crossbow pose for a charged Ranged Weapon API crossbow.
@Mixin(AvatarRenderer.class)
public abstract class CrossbowHoldPoseMixin {

    @WrapOperation(
        method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean mmsCompat$customCrossbowHold(ItemStack stack, Object item, Operation<Boolean> original) {
        return item == Items.CROSSBOW && stack.getItem() instanceof CustomCrossbow
            || original.call(stack, item);
    }
}
