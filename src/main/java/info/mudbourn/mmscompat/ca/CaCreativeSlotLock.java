package info.mudbourn.mmscompat.ca;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.hollowed.combatamenities.networking.slots.SlotCreativeClientPacketPayload;

/**
 * Replaces Combat Amenities' creative back and belt slot packet handler with
 * one that ignores it, leaving those slots to the vanilla creative slot sync.
 */
public final class CaCreativeSlotLock {

    private CaCreativeSlotLock() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerPlayNetworking.unregisterGlobalReceiver(SlotCreativeClientPacketPayload.ID.id());
            ServerPlayNetworking.registerGlobalReceiver(SlotCreativeClientPacketPayload.ID, (payload, context) -> {
            });
        });
    }
}
