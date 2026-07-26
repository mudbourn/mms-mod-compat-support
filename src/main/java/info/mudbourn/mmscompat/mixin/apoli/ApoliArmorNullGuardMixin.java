package info.mudbourn.mmscompat.mixin.apoli;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Null-guards Apoli's {@code apoli$shouldApplyArmor} field on LivingEntity.
 *
 * <p>Apoli adds this {@code Optional<Boolean>} field via mixin and initializes
 * it during entity construction.  Some mods (notably Aerial Hell) create
 * LivingEntity subclasses that skip the normal init path, leaving the field
 * null.  When the entity takes damage, Apoli's
 * {@code modifyArmorApplicance} mixin calls {@code isPresent()} on the null
 * field and the server crashes.</p>
 *
 * <p>This mixin injects at HEAD of {@code getDamageAfterArmorAbsorb} — the
 * same method Apoli targets — and initializes the field to
 * {@code Optional.of(true)} (default: apply armor normally) if it is null.
 * Reflection is used because the field belongs to another mixin and cannot
 * be {@code @Shadow}ed.</p>
 */
@Mixin(LivingEntity.class)
public abstract class ApoliArmorNullGuardMixin {

    private static final Logger mmsCompat$LOG = LoggerFactory.getLogger("mms_compat");
    private static Field mmsCompat$shouldApplyArmorField;
    private static boolean mmsCompat$fieldResolved = false;

    @Inject(
        method = "getDamageAfterArmorAbsorb",
        at = @At("HEAD"),
        require = 1
    )
    private void mmsCompat$ensureApoliArmorField(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (mmsCompat$fieldResolved && mmsCompat$shouldApplyArmorField == null) return;
        try {
            if (!mmsCompat$fieldResolved) {
                mmsCompat$shouldApplyArmorField = LivingEntity.class.getDeclaredField("apoli$shouldApplyArmor");
                mmsCompat$shouldApplyArmorField.setAccessible(true);
                mmsCompat$fieldResolved = true;
            }
            Object value = mmsCompat$shouldApplyArmorField.get(this);
            if (value == null) {
                mmsCompat$shouldApplyArmorField.set(this, Optional.of(true));
            }
        } catch (NoSuchFieldException e) {
            // Apoli version doesn't have this field — nothing to guard
            mmsCompat$fieldResolved = true;
            mmsCompat$shouldApplyArmorField = null;
        } catch (Exception e) {
            mmsCompat$LOG.warn("[mms_compat] failed to null-guard apoli$shouldApplyArmor", e);
        }
    }
}
