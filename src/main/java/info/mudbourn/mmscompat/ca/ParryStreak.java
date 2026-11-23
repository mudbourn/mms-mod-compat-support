package info.mudbourn.mmscompat.ca;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A run of perfect parries. Each perfect parry starts or extends it; while it lasts, any shield or
 * blocking weapon goes up instantly (no raise delay) and lowering it puts nothing on cooldown, so a
 * player who keeps timing their guards can drop and re-raise between every swing of a combo. The
 * first hit that is not perfect-parried ends it and the normal slowdown applies again.
 */
public final class ParryStreak {

    // How long a perfect parry keeps the streak open for the next one.
    public static final int STREAK_TICKS = 40;

    private static final Map<UUID, Long> SERVER = new ConcurrentHashMap<>();
    // Client side: the local player's streak end in their own world's game time, and how to find them.
    private static long clientUntil;
    private static Supplier<Player> localPlayer = () -> null;

    private ParryStreak() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(StreakS2C.TYPE, StreakS2C.CODEC);
    }

    public static void setLocalPlayer(Supplier<Player> supplier) {
        localPlayer = supplier;
    }

    public static void onClientStreak(int ticks) {
        Player player = localPlayer.get();
        clientUntil = ticks > 0 && player != null ? player.level().getGameTime() + ticks : 0L;
    }

    public static void extend(ServerPlayer player) {
        SERVER.put(player.getUUID(), player.level().getGameTime() + STREAK_TICKS);
        ServerPlayNetworking.send(player, new StreakS2C(STREAK_TICKS));
    }

    public static void end(ServerPlayer player) {
        if (SERVER.remove(player.getUUID()) != null) {
            ServerPlayNetworking.send(player, new StreakS2C(0));
        }
    }

    public static boolean active(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
            Long until = SERVER.get(entity.getUUID());
            return until != null && entity.level().getGameTime() <= until;
        }
        return entity == localPlayer.get() && entity.level().getGameTime() <= clientUntil;
    }

    // Whether this cooldown group belongs to a shield or blocking weapon in either of the player's hands.
    public static boolean isGuardGroup(Player player, Identifier group) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.has(DataComponents.BLOCKS_ATTACKS) && player.getCooldowns().getCooldownGroup(stack).equals(group)) {
                return true;
            }
        }
        return false;
    }

    public static Player clientPlayer() {
        return localPlayer.get();
    }

    // Server to client: the local player's parry streak now lasts this many ticks (0 ends it).
    public record StreakS2C(int ticks) implements CustomPacketPayload {
        public static final Type<StreakS2C> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "parry_streak"));
        public static final StreamCodec<ByteBuf, StreakS2C> CODEC = ByteBufCodecs.VAR_INT.map(StreakS2C::new, StreakS2C::ticks);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
