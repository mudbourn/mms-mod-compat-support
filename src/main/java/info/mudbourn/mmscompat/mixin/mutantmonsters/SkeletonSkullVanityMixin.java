package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.mutantmonsters.common.handler.PlayerEventsHandler;
import fuzs.puzzleslib.common.api.event.v1.core.EventResult;
import fuzs.puzzleslib.common.api.event.v1.data.MutableInt;
import info.mudbourn.mmsvanity.VanityMarker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stops a vanity mutant skeleton skull from granting multishot. */
@Mixin(PlayerEventsHandler.class)
public abstract class SkeletonSkullVanityMixin {

    @Inject(method = "onArrowLoose", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$vanitySkullGrantsNothing(
        Player player,
        ItemStack bow,
        Level level,
        MutableInt charge,
        CallbackInfoReturnable<EventResult> cir
    ) {
        if (VanityMarker.isVanity(player.getItemBySlot(EquipmentSlot.HEAD))) {
            cir.setReturnValue(EventResult.PASS);
        }
    }
}
