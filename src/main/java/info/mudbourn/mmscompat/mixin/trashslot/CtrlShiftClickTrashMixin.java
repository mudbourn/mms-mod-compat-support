package info.mudbourn.mmscompat.mixin.trashslot;

import info.mudbourn.mmscompat.client.trashslot.QuickTrash;
import net.blay09.mods.trashslot.TrashHelper;
import net.blay09.mods.trashslot.TrashSlotConfig;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Gives container screens the slot lookup and deletion QuickTrash needs for its Ctrl+Alt drag.
// The clicks themselves come through Fabric's screen mouse events (see QuickTrash), because
// Mouse Tweaks takes left clicks there before the screen's own mouseClicked runs.
@Mixin(AbstractContainerScreen.class)
public abstract class CtrlShiftClickTrashMixin<T extends AbstractContainerMenu> implements QuickTrash.Target {

    @Shadow @Final protected T menu;

    @Shadow
    private Slot getHoveredSlot(double x, double y) {
        throw new AssertionError();
    }

    @Override
    public Slot mmsCompat$slotAt(double x, double y) {
        return getHoveredSlot(x, y);
    }

    @Override
    public void mmsCompat$trash(Slot slot) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !slot.hasItem() || !TrashHelper.canDelete(slot.getItem())) return;
        TrashSlotConfig.getDeletionProvider().deleteContainerItem(client.player, menu, slot.index, false, TrashSlotGuiHandler.getTrashSlot());
    }
}
