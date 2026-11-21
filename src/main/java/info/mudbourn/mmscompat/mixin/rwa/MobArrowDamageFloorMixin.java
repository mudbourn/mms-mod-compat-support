package info.mudbourn.mmscompat.mixin.rwa;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Gives a mob's arrow vanilla damage when RWA scales it to zero from a mob without ranged damage.
@Mixin(ProjectileUtil.class)
public abstract class MobArrowDamageFloorMixin {

    @Inject(method = "getMobArrow", at = @At("RETURN"))
    private static void mmsCompat$floorMobArrowDamage(
        LivingEntity mob,
        ItemStack projectile,
        float velocity,
        ItemStack weapon,
        CallbackInfoReturnable<AbstractArrow> cir
    ) {
        AbstractArrow arrow = cir.getReturnValue();
        if (arrow != null && ((ArrowBaseDamageAccessor) arrow).mmsCompat$baseDamage() <= 0.0) {
            arrow.setBaseDamageFromMob(velocity);
        }
    }
}
