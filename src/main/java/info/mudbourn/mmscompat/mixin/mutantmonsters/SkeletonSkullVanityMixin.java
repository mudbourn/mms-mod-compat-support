package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.mutantmonsters.handler.PlayerEventsHandler;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import info.mudbourn.mmscompat.VanityMarker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops a vanity Mutant Skeleton Skull from granting multishot.
 *
 * <p>The skull is the one piece of that set which cannot be made inert the cheap way.
 * Its three companions are handed out as leather bases wearing the
 * {@code mutantmonsters:mutant_skeleton} equipment asset, so they never match the
 * mod's item checks at all — but the skull is a <em>block</em>, drawn from its block
 * model the way a vanilla mob head is. Skin it onto a helmet and it loses its shape,
 * which is the whole reason anyone wants to wear it.
 *
 * <p>So the item stays real and the effect is gated instead.
 * {@code PlayerEventsHandler.onArrowLoose} reads the head slot and compares it to
 * {@code ModItems.MUTANT_SKELETON_SKULL_ITEM}; this runs first and returns
 * {@code PASS} — the handler's own "did nothing" result — whenever the skull in that
 * slot carries {@link VanityMarker}. A real skull is untouched, and so is every other
 * reason this event fires.
 *
 * <p>Deliberately gated on the worn stack rather than the bow: the marker means "this
 * armour is cosmetic", and a player wearing a vanity skull should still get the
 * ordinary behaviour of whatever they are holding.
 *
 * <p>Sibling of {@code SkeletonArmorQuickDrawMixin}, which owns the chest slot's
 * quick draw in the same class. That one needs no vanity check: the vanity chestplate
 * is a skinned leather base, so its item comparison already fails.
 */
@Mixin(PlayerEventsHandler.class)
public abstract class SkeletonSkullVanityMixin {

    @Inject(method = "onArrowLoose", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$vanitySkullGrantsNothing(Player player, ItemStack bow, Level level,
                                                           MutableInt charge, boolean hasAmmo,
                                                           CallbackInfoReturnable<EventResult> cir) {
        if (VanityMarker.isVanity(player.getItemBySlot(EquipmentSlot.HEAD))) {
            cir.setReturnValue(EventResult.PASS);
        }
    }
}
