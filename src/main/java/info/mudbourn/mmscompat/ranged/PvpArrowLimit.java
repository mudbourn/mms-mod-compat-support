package info.mudbourn.mmscompat.ranged;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

import java.util.Set;

/**
 * Limits player-versus-player archery to standard and heavy arrows.
 *
 * Any other arrow a player fires deals no damage to another player, which also
 * stops its post-hit effects (potions, glowing, freezing) and the explosive
 * arrow's blast. A player can still be hurt by their own arrows.
 */
public final class PvpArrowLimit {

    private static final Set<Identifier> ALLOWED = Set.of(
        Identifier.withDefaultNamespace("arrow"),
        Identifier.fromNamespaceAndPath("weaponsexpanded", "heavy_arrow")
    );

    private PvpArrowLimit() {}

    // Registers the damage veto.
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(PvpArrowLimit::allowDamage);
    }

    private static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        if (!(victim instanceof Player)
            || !(source.getDirectEntity() instanceof AbstractArrow arrow)
            || !(arrow.getOwner() instanceof Player shooter)
            || shooter == victim) {
            return true;
        }
        return ALLOWED.contains(BuiltInRegistries.ITEM.getKey(arrow.getPickupItemStackOrigin().getItem()));
    }
}
