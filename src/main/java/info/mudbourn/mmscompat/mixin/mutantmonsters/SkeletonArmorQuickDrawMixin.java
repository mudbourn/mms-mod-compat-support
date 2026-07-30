package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.mutantmonsters.handler.PlayerEventsHandler;
import fuzs.mutantmonsters.init.ModItems;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
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

/**
 * Extends the mutant skeleton chestplate's quick-draw buff to every ranged
 * weapon in the pack, not just the vanilla bow.
 *
 * <p>Mutant Monsters gates the buff on {@code instanceof BowItem}. MMS ships
 * five ranged weapons and only two of them pass that test — the vanilla bow and
 * Weapons Expanded's longbow, which subclasses it. The vanilla crossbow,
 * {@code weaponsexpanded:chain_crossbow}, {@code expanded_weaponry:longbow} and
 * {@code expanded_weaponry:slingshot} all miss out, the last two because
 * Kielson's {@code CustomBow} extends {@code ProjectileWeaponItem} directly.
 *
 * <p>The original body is replaced rather than supplemented, so the vanilla-bow
 * path is reproduced here exactly and the buff can never be applied twice.
 *
 * <p>Each weapon family needs its own "already at full draw" test, because
 * draining the use timer past that point would fire the shot early:
 * <ul>
 *   <li>Bows expose {@code getPowerForTime}.</li>
 *   <li>Crossbows are done once charged.</li>
 *   <li>Everything else — {@code CustomBow} in practice — falls back to the
 *       remaining-ticks guard. That is safe without referencing Expanded
 *       Weaponry's classes, which this mixin must not do: it is gated on Mutant
 *       Monsters alone and has to load with Expanded Weaponry absent.</li>
 * </ul>
 */
@Mixin(PlayerEventsHandler.class)
public abstract class SkeletonArmorQuickDrawMixin {

    @Inject(method = "onUseItemTick", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$widenQuickDraw(LivingEntity livingEntity, ItemStack itemStack,
                                                 InteractionHand interactionHand, MutableInt remainingUseDuration,
                                                 CallbackInfoReturnable<EventResult> cir) {
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
