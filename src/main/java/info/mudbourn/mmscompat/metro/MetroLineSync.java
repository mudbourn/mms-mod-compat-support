package info.mudbourn.mmscompat.metro;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C payload carrying a metro cart's line name.
 *
 * ModMetro keeps {@code lineName} as a plain server-side field — clients
 * only ever receive the current/next station trackers — so the in-cart HUD
 * cannot show which line the player is riding. The server half
 * ({@link MetroLineSyncServer}) pushes this to riders; the client half
 * caches it by vehicle entity id for the HUD title rewrite.
 */
public record MetroLineSync(int vehicleId, String line) implements CustomPacketPayload {

    public static final Type<MetroLineSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "metro_line"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MetroLineSync> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, MetroLineSync::vehicleId,
                    ByteBufCodecs.STRING_UTF8, MetroLineSync::line,
                    MetroLineSync::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
