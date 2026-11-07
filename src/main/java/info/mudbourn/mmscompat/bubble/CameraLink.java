package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Link from a security monitor to a surveillance camera. */
public record CameraLink(GlobalPos pos, Component name, boolean legacy) {

    public static final Codec<CameraLink> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(CameraLink::pos),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(CameraLink::name),
            Codec.BOOL.optionalFieldOf("legacy", false).forGetter(CameraLink::legacy)
        ).apply(instance, CameraLink::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CameraLink> STREAM_CODEC = StreamCodec.composite(
        GlobalPos.STREAM_CODEC,
        CameraLink::pos,
        ComponentSerialization.STREAM_CODEC,
        CameraLink::name,
        ByteBufCodecs.BOOL,
        CameraLink::legacy,
        CameraLink::new
    );
}
