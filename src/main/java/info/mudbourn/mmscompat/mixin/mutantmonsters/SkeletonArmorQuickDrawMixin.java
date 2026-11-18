package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.mutantmonsters.common.handler.PlayerEventsHandler;
import fuzs.mutantmonsters.common.init.ModItems;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extends the mutant skeleton chestplate quick draw to every projectile weapon. */
@Mixin(PlayerEventsHandler.class)
public abstract class SkeletonArmorQuickDrawMixin {

    @Inject(method = "onUseItemTick", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$widenQuickDraw(
        LivingEntity livingEntity,
        ItemStack itemStack,
        InteractionHand interactionHand,
        MutableInt remainingUseDuration,
        CallbackInfoReturnable<EventResult> cir
    ) {
        cir.setReturnValue(EventResult.PASS);

        if (!livingEntity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.MUTANT_SKELETON_CHESTPLATE_ITEM)) {
            return;
        }

        Item item = itemStack.getItem();
        if (!(item instanceof ProjectileWeaponItem)) {
            return;
        }

        int remaining = remainingUseDuration.getAsInt();
        int elapsed = itemStack.getUseDuration(livingEntity) - remaining;

        boolean atFullDraw;
        if (item instanceof BowItem) {
            atFullDraw = BowItem.getPowerForTime(elapsed) >= 1.0F;
        } else if (item instanceof CrossbowItem) {
            atFullDraw = CrossbowItem.isCharged(itemStack);
        } else {
            atFullDraw = remaining <= 2;
        }

        if (!atFullDraw) {
            remainingUseDuration.mapAsInt(value -> value - 2);
        }
    }
}
