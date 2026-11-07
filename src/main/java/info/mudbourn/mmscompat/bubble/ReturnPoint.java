package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Where a player stood and which mode they used before viewing a camera. */
public record ReturnPoint(ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch, GameType gameType) {

    public static final Codec<ReturnPoint> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ReturnPoint::dimension),
            Vec3.CODEC.fieldOf("pos").forGetter(ReturnPoint::pos),
            Codec.FLOAT.fieldOf("yaw").forGetter(ReturnPoint::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(ReturnPoint::pitch),
            GameType.CODEC.fieldOf("game_type").forGetter(ReturnPoint::gameType)
        ).apply(instance, ReturnPoint::new)
    );
}
