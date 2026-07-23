package info.mudbourn.mmscompat.metro;

import com.example.modmetro.MetroCartEntity;
import info.mudbourn.mmscompat.mixin.metrofix.MetroCartLineAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server half of the metro line-name sync.
 *
 * Once a second, checks each online player's vehicle: if it's a metro cart,
 * sends {@link MetroLineSync} whenever the (vehicleId, line) pair differs
 * from what that player last received — covers boarding, line changes at
 * stations, and switching carts. Poll-based on purpose: ModMetro assigns
 * {@code lineName} deep inside its movement tick, and hooking that spot
 * would couple us to fragile decompiled internals for no gain at 1 Hz.
 *
 * Only referenced when ModMetro is loaded (guarded in the mod initializer),
 * so the MetroCartEntity import never resolves without it.
 */
public final class MetroLineSyncServer {

    /** player UUID -> last "vehicleId|line" sent; pruned on disconnect. */
    private static final Map<UUID, String> lastSent = new HashMap<>();

    private MetroLineSyncServer() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(MetroLineSync.TYPE, MetroLineSync.CODEC);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                lastSent.remove(handler.getPlayer().getUUID()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!(player.getVehicle() instanceof MetroCartEntity cart)) {
                    lastSent.remove(player.getUUID());
                    continue;
                }
                String line = ((MetroCartLineAccessor) cart).mmsCompat$getLineName();
                if (line == null) line = "";
                String key = cart.getId() + "|" + line;
                if (!key.equals(lastSent.get(player.getUUID()))
                        && ServerPlayNetworking.canSend(player, MetroLineSync.TYPE)) {
                    ServerPlayNetworking.send(player, new MetroLineSync(cart.getId(), line));
                    lastSent.put(player.getUUID(), key);
                }
            }
        });
    }
}
