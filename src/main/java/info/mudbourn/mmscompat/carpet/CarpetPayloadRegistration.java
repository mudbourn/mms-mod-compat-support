package info.mudbourn.mmscompat.carpet;

import carpet.network.CarpetClient;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class CarpetPayloadRegistration {
    private CarpetPayloadRegistration() {
    }

    // Registers Carpet's hello payload in Fabric's play registries.
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(
            CarpetClient.CarpetPayload.TYPE,
            CarpetClient.CarpetPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            CarpetClient.CarpetPayload.TYPE,
            CarpetClient.CarpetPayload.STREAM_CODEC
        );
    }
}
