package info.mudbourn.mmscompat.client.trashslot;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

// Ctrl+Alt with the left button held trashes every stack the cursor passes over, into TrashSlot's
// trash slot. Mouse Tweaks claims left clicks through these same screen mouse events, so ours run
// in a phase ordered before the default one; while a trash drag is on, Mouse Tweaks never sees it.
public final class QuickTrash {

    /** Implemented on container screens by CtrlShiftClickTrashMixin. */
    public interface Target {
        Slot mmsCompat$slotAt(double x, double y);

        void mmsCompat$trash(Slot slot);
    }

    private static final Identifier EARLY = Identifier.fromNamespaceAndPath("mms_compat", "quick_trash");

    private static boolean dragging;
    private static Slot last;

    private QuickTrash() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof Target) || screen instanceof CreativeModeInventoryScreen) return;
            early(ScreenMouseEvents.allowMouseClick(screen), (ScreenMouseEvents.AllowMouseClick) (s, event) -> !press(s, event));
            early(ScreenMouseEvents.allowMouseDrag(screen), (ScreenMouseEvents.AllowMouseDrag) (s, event, dx, dy) -> !drag(s, event));
            early(ScreenMouseEvents.allowMouseRelease(screen), (ScreenMouseEvents.AllowMouseRelease) (s, event) -> !release(event));
            ScreenEvents.remove(screen).register(s -> dragging = false);
        });
    }

    private static <T> void early(Event<T> event, T listener) {
        event.addPhaseOrdering(EARLY, Event.DEFAULT_PHASE);
        event.register(EARLY, listener);
    }

    // On macOS vanilla turns a Ctrl+left click into a right click (InputQuirks), so there the
    // trash press arrives as button 1 with Ctrl still held.
    private static boolean leftButton(MouseButtonEvent event) {
        return event.button() == 0
            || (InputQuirks.EMULATE_RIGHT_CLICK_WITH_CTRL_KEY && event.button() == 1 && event.hasControlDown());
    }

    private static boolean wants(MouseButtonEvent event) {
        return leftButton(event) && event.hasControlDown() && event.hasAltDown();
    }

    private static boolean press(Screen screen, MouseButtonEvent event) {
        dragging = false;
        if (!wants(event)) return false;
        dragging = true;
        last = null;
        trashUnder(screen, event);
        return true;
    }

    private static boolean drag(Screen screen, MouseButtonEvent event) {
        if (!dragging) return false;
        if (!wants(event)) {
            dragging = false;
            return false;
        }
        trashUnder(screen, event);
        return true;
    }

    private static boolean release(MouseButtonEvent event) {
        if (!dragging || (event.button() != 0 && event.button() != 1)) return false;
        dragging = false;
        last = null;
        return true;
    }

    private static void trashUnder(Screen screen, MouseButtonEvent event) {
        Target target = (Target) screen;
        Slot slot = target.mmsCompat$slotAt(event.x(), event.y());
        if (slot == null || slot == last || !slot.hasItem()) return;
        last = slot;
        target.mmsCompat$trash(slot);
    }
}
