package info.mudbourn.mmscompat.alexscaves;

import com.github.alexmodguy.alexscaves.server.item.TotemOfPossessionItem;
import eu.pb4.trinkets.api.TrinketsApi;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Lets a Totem of Possession worn in a trinket slot be used: while the possess key is held, the
 * server runs the totem's own use tick on the worn stack each tick, steering the bound mob as
 * holding right-click with it in hand would, and releases it when the key is let go. Binding a
 * mob is unchanged: hit it with the totem in hand.
 */
public final class WornPossession {

    private static final Map<UUID, Integer> HOLDING = new HashMap<>();

    private WornPossession() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(KeyC2S.TYPE, KeyC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeyC2S.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!payload.down()) {
                release(player, HOLDING.remove(player.getUUID()));
            } else if (!HOLDING.containsKey(player.getUUID()) && !worn(player).isEmpty()) {
                HOLDING.put(player.getUUID(), 0);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(WornPossession::tick);
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = HOLDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ItemStack stack = player == null ? ItemStack.EMPTY : worn(player);
            if (player == null || !player.isAlive() || stack.isEmpty()) {
                it.remove();
                continue;
            }
            int held = entry.getValue();
            int remaining = stack.getUseDuration(player) - held;
            stack.getItem().onUseTick(player.level(), player, stack, remaining);
            entry.setValue(held + 1);
            // The totem resets its binding when the mob dies, it breaks or possession ends.
            if (!TotemOfPossessionItem.isBound(stack)) {
                stack.getItem().releaseUsing(stack, player.level(), player, remaining);
                it.remove();
            }
        }
    }

    private static void release(ServerPlayer player, Integer held) {
        if (held == null) {
            return;
        }
        ItemStack stack = worn(player);
        if (!stack.isEmpty()) {
            stack.getItem().releaseUsing(stack, player.level(), player, stack.getUseDuration(player) - held);
        }
    }

    // The first bound Totem of Possession in the player's trinket slots.
    private static ItemStack worn(ServerPlayer player) {
        var attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) {
            return ItemStack.EMPTY;
        }
        return attachment.findFirst(stack -> stack.getItem() instanceof TotemOfPossessionItem && TotemOfPossessionItem.isBound(stack))
            .map(slot -> slot.get())
            .orElse(ItemStack.EMPTY);
    }

    // Client to server: the possess key went down or up.
    public record KeyC2S(boolean down) implements CustomPacketPayload {
        public static final Type<KeyC2S> TYPE = new Type<>(Identifier.fromNamespaceAndPath("mms_compat", "worn_possession"));
        public static final StreamCodec<ByteBuf, KeyC2S> CODEC = ByteBufCodecs.BOOL.map(KeyC2S::new, KeyC2S::down);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
