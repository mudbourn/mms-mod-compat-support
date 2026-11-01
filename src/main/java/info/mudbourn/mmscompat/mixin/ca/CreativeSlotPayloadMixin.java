package info.mudbourn.mmscompat.mixin.ca;

import net.hollowed.combatamenities.networking.slots.SlotCreativeClientPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Combat Amenities' creative slot sync send the stack held in its own
 * inventory slot instead of whatever sits at creative menu index 46 or 47.
 */
@Mixin(value = SlotCreativeClientPacketPayload.class, remap = false)
public abstract class CreativeSlotPayloadMixin {

    @Shadow
    @Final
    private int slotId;

    @Shadow
    @Final
    @Mutable
    private ItemStack itemStack;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void mms_compat$readOwnSlot(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        itemStack = player.getInventory().getItem(slotId).copy();
    }
}
