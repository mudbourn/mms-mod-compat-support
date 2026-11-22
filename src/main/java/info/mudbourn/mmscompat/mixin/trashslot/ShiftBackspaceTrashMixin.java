package info.mudbourn.mmscompat.mixin.trashslot;

import net.blay09.mods.trashslot.TrashHelper;
import net.blay09.mods.trashslot.TrashSlotConfig;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Shift+Backspace over a slot sends that stack to TrashSlot's trash slot. It runs before the
// screen's own key handling, so the drop key (Backspace) doesn't also drop an item.
@Mixin(AbstractContainerScreen.class)
public abstract class ShiftBackspaceTrashMixin<T extends AbstractContainerMenu> {

    @Shadow protected Slot hoveredSlot;
    @Shadow @Final protected T menu;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$trashHovered(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() != InputConstants.KEY_BACKSPACE || !event.hasShiftDown()) return;
        if ((Object) this instanceof CreativeModeInventoryScreen) return;
        Slot slot = hoveredSlot;
        Minecraft client = Minecraft.getInstance();
        if (slot == null || !slot.hasItem() || client.player == null) return;
        if (!TrashHelper.canDelete(slot.getItem())) return;
        TrashSlotConfig.getDeletionProvider().deleteContainerItem(client.player, menu, slot.index, false, TrashSlotGuiHandler.getTrashSlot());
        cir.setReturnValue(true);
    }
}
