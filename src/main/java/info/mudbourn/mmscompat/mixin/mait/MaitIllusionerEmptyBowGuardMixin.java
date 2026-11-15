package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips an Illusioner's ranged shot when it has no bow in either hand.
 *
 * <p>Mob AI Tweaks' {@code arrowsOriginateFromClones} fires one arrow per mirror
 * clone using the main-hand stack as the weapon. With no bow held that stack is
 * empty and vanilla's arrow constructor throws "Invalid weapon firing an
 * arrow", crashing the server tick. The lowered priority makes this HEAD inject
 * run before Mob AI Tweaks' own.</p>
 */
@Mixin(value = Illusioner.class, priority = 500)
public abstract class MaitIllusionerEmptyBowGuardMixin {

    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$skipShotWithoutBow(LivingEntity target, float power, CallbackInfo ci) {
        Illusioner self = (Illusioner) (Object) this;
        if (self.getItemInHand(ProjectileUtil.getWeaponHoldingHand(self, Items.BOW)).isEmpty()) {
            ci.cancel();
        }
    }
}
