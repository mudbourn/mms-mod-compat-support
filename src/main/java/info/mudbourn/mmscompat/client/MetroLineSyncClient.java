package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.metro.MetroLineSync;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.HashMap;
import java.util.Map;

/**
 * Client half of the metro line-name sync: caches line names by vehicle
 * entity id for the HUD title rewrite in {@code MetroHudTextMixin}.
 * Cleared on disconnect so stale ids never leak across sessions.
 */
public final class MetroLineSyncClient {

    private static final Map<Integer, String> lines = new HashMap<>();

    private MetroLineSyncClient() {}

    public static void register() {
        // payload type itself is registered in MetroLineSyncServer.register(),
        // which runs from the common initializer on both physical sides
        ClientPlayNetworking.registerGlobalReceiver(MetroLineSync.TYPE, (payload, context) ->
                context.client().execute(() -> lines.put(payload.vehicleId(), payload.line())));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> lines.clear());
    }

    /** Line name for a vehicle entity id, or "" while unknown/unsynced. */
    public static String lineFor(int vehicleId) {
        return lines.getOrDefault(vehicleId, "");
    }
}
