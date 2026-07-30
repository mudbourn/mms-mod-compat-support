package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import info.mudbourn.mmscompat.duck.ArrowFlightDuck;
import info.mudbourn.mmscompat.ranged.ArrowFlight;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the per-weapon flight profiles from {@link ArrowFlight}.
 *
 * <p>The profile is resolved once, in the constructor that takes the firing
 * weapon, and cached on the arrow — the shooter's helmet and the weapon in
 * hand can both change mid-flight, and an arrow that was loosed from a longbow
 * should stay a longbow arrow either way.
 *
 * <p>This targets vanilla, so it lives in the ungated config; the mod-specific
 * matching is all inside {@link ArrowFlight}, keyed by registry id.
 */
@Mixin(AbstractArrow.class)
public abstract class ArrowFlightMixin implements ArrowFlightDuck {

    @Unique
    private float mmsCompat$gravityScale = ArrowFlight.VANILLA[0];

    @Unique
    private float mmsCompat$inertiaBonus = ArrowFlight.VANILLA[1];

    @Override
    public float mmsCompat$gravityScale() {
        return this.mmsCompat$gravityScale;
    }

    @Override
    public float mmsCompat$inertiaBonus() {
        return this.mmsCompat$inertiaBonus;
    }

    @Override
    public void mmsCompat$setFlight(float gravityScale, float inertiaBonus) {
        this.mmsCompat$gravityScale = gravityScale;
        this.mmsCompat$inertiaBonus = inertiaBonus;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("RETURN"))
    private void mmsCompat$resolveFlight(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level level,
                                         ItemStack pickupItem, ItemStack weapon, CallbackInfo ci) {
        float[] profile = ArrowFlight.profileFor(weapon, shooter);
        this.mmsCompat$setFlight(profile[0], profile[1]);
    }

    @ModifyReturnValue(method = "getDefaultGravity", at = @At("RETURN"))
    private double mmsCompat$scaleGravity(double original) {
        return original * this.mmsCompat$gravityScale;
    }

    /**
     * Nudges the per-tick speed retention toward lossless. Vanilla passes 0.99
     * in air and {@code getWaterInertia()} in water; scaling the shortfall
     * rather than substituting a constant keeps the water case proportionate.
     */
    @ModifyVariable(method = "applyInertia(F)V", at = @At("HEAD"), argsOnly = true)
    private float mmsCompat$reduceDrag(float inertia) {
        if (this.mmsCompat$inertiaBonus <= 0.0F) {
            return inertia;
        }
        return 1.0F - (1.0F - inertia) * (1.0F - this.mmsCompat$inertiaBonus);
    }
}
