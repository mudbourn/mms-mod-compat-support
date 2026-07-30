package info.mudbourn.mmscompat.mixin.mutantmonsters;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import info.mudbourn.mmscompat.ranged.SkeletonArmorSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Suppresses Infinity while three or more mutant skeleton armour pieces are
 * worn, so the set's ranged buffs come at a cost.
 *
 * <p>Infinity is not a flag to test for — it is an enchantment effect that
 * drives {@code EnchantmentHelper#processAmmoUse} to zero, and vanilla reads
 * that zero as "consume nothing, and mark the arrow intangible so it cannot be
 * picked up". Forcing the result back to 1 restores both halves at once: the
 * arrow is spent, and the one that lands is recoverable.
 *
 * <p>This intercepts the call's result rather than the enchantment, so the two
 * paths that never reach it are left alone — creative mode, and the extra
 * projectiles a multishot volley spawns, which are meant to be free.
 *
 * <p>Applies to every projectile weapon, not just bows: a crossbow can carry
 * Infinity here too, and the drawback should not be dodgeable by switching
 * weapon. Compare {@link SkeletonArmorQuickDrawMixin}, which widens a buff the
 * mod gated too narrowly.
 */
@Mixin(ProjectileWeaponItem.class)
public abstract class SkeletonArmorInfinityMixin {

    @ModifyExpressionValue(
            method = "useAmmo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processAmmoUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)I"))
    private static int mmsCompat$suppressInfinity(int ammoUse, ItemStack weapon, ItemStack ammo,
                                                  LivingEntity shooter, boolean intangible) {
        if (ammoUse == 0 && SkeletonArmorSet.atDrawbackThreshold(shooter)) {
            return 1;
        }
        return ammoUse;
    }
}
