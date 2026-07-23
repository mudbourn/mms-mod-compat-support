package info.mudbourn.mmscompat.metro;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.MetroMod;
import com.example.modmetro.MetroSpawnerItem;
import info.mudbourn.mmscompat.MetroText;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * QOL inverse of the spawner: hitting a metro cart while holding the train
 * spawner item despawns the whole train (every cart sharing the target's
 * lead UUID), instead of uselessly whacking one cart. Spawn with
 * right-click, remove with left-click — one tool, both directions.
 *
 * The attack is cancelled either way so the cart never takes a hit while
 * the spawner is in hand. Removal itself runs server-side only.
 */
public final class MetroTrainDespawn {

    private MetroTrainDespawn() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(entity instanceof MetroCartEntity cart)
                    || !(player.getItemInHand(hand).getItem() instanceof MetroSpawnerItem)) {
                return InteractionResult.PASS;
            }
            if (world instanceof ServerLevel level) {
                UUID trainId = cart.getLeadCartUuid();
                List<MetroCartEntity> train = new ArrayList<>();
                for (Entity e : level.getEntities(MetroMod.METRO_CART, ent -> true)) {
                    if (e instanceof MetroCartEntity other
                            && (other == cart || Objects.equals(trainId, other.getLeadCartUuid()))) {
                        train.add(other);
                    }
                }
                for (MetroCartEntity c : train) {
                    c.ejectPassengers();
                    c.discard();
                }
                player.displayClientMessage(Component.literal(
                        String.format(MetroText.tr("mms_compat.metro.cmd.cars_removed"), train.size())), true);
            }
            // cancel the swing on both sides — the spawner never damages carts
            return InteractionResult.SUCCESS;
        });
    }
}
