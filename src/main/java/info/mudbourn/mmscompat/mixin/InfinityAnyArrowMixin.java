package info.mudbourn.mmscompat.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Lets Infinity spare every arrow, not just the plain one.
 *
 * Infinity's ammo_use effect only matches minecraft:arrow, so tipped,
 * spectral and modded arrows were still consumed. Any ammo in
 * #minecraft:arrows, except Weapons Expanded's explosive arrow, is presented to the enchantment check as a plain arrow;
 * the real stack is still the one fired.
 */
@Mixin(ProjectileWeaponItem.class)
public abstract class InfinityAnyArrowMixin {

    private static final Identifier MMS_EXPLOSIVE_ARROW = Identifier.fromNamespaceAndPath("weaponsexpanded", "explosive_arrow");

    @ModifyArg(
            method = "useAmmo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processAmmoUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)I"),
            index = 2)
    private static ItemStack mmsCompat$asPlainArrow(ItemStack ammo) {
        if (ammo.is(ItemTags.ARROWS) && !ammo.is(Items.ARROW)
                && !BuiltInRegistries.ITEM.getKey(ammo.getItem()).equals(MMS_EXPLOSIVE_ARROW)) {
            return new ItemStack(Items.ARROW);
        }
        return ammo;
    }
}
