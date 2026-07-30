package info.mudbourn.mmscompat.mixin.apoli;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Null-guards Apoli's two armor-decision fields on LivingEntity.
 *
 * <p>Apoli adds {@code apoli$shouldApplyArmor} and {@code apoli$shouldDamageArmor}
 * via mixin and initializes <em>both</em> in {@code modifyDamageTaken}, a
 * {@code @ModifyVariable} at HEAD of {@code LivingEntity.hurtServer}. Aerial
 * Hell's {@code AbstractCustomHurtMonsterEntity} <em>overrides</em>
 * {@code hurtServer} and never calls super — it routes through its own
 * {@code customHurt} / {@code tryActuallyHurt} straight into
 * {@code LivingEntity.actuallyHurt}. Apoli's initializer therefore never runs
 * for those mobs, the fields stay null, and Apoli's {@code modifyArmorApplicance}
 * NPEs on them further down the chain.</p>
 *
 * <p><b>Both fields, and both must be {@code Optional.empty()}.</b> Apoli's own
 * initializer produces {@code empty} whenever no power expresses a preference,
 * which is always the case for an entity that never went through
 * {@code hurtServer}. {@code empty}/{@code empty} makes
 * {@code modifyArmorApplicance} fall through without setting a return value, so
 * vanilla's armor math runs untouched — exactly what should happen for a mob
 * with no Apoli powers.</p>
 *
 * <p>Seeding {@code shouldApplyArmor} with {@code Optional.of(true)} instead, as
 * this guard did through 0.9.22, is not neutral: it forces Apoli's branch, calls
 * {@code hurtArmor}, and cancels vanilla's calculation outright. It also left
 * {@code shouldDamageArmor} null while steering execution directly onto a
 * dereference of it, which is what crashed the 2026-07-29 snake tick — the guard
 * ran and then walked into the NPE it was meant to prevent.</p>
 *
 * <p><b>Two injection sites, on purpose.</b> The primary is at HEAD of
 * {@code actuallyHurt}: it sits strictly upstream of
 * {@code getDamageAfterArmorAbsorb} and Apoli injects nothing there, so it is
 * uncontested and always wins. The secondary, at HEAD of
 * {@code getDamageAfterArmorAbsorb}, is a backstop for any path that reaches
 * armor calculation without passing through {@code actuallyHurt}.</p>
 *
 * <p>The backstop shares its injection point with Apoli's own HEAD inject. Mixin
 * inserts a HEAD callback at instruction 0, so the config applied <em>last</em>
 * ends up running <em>first</em>. The raised priority makes this mixin apply
 * after Apoli's and so run before it. That ordering is a Mixin implementation
 * detail, which is exactly why the real fix is the uncontested
 * {@code actuallyHurt} guard and not this.</p>
 *
 * <p>Reflection is used because the fields belong to another mod's mixin and
 * cannot be {@code @Shadow}ed.</p>
 */
@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class ApoliArmorNullGuardMixin {

    private static final Logger mmsCompat$LOG = LoggerFactory.getLogger("mms_compat");

    private static final String[] mmsCompat$FIELD_NAMES = {
            "apoli$shouldApplyArmor",
            "apoli$shouldDamageArmor"
    };

    private static Field[] mmsCompat$armorFields;
    private static boolean mmsCompat$fieldsResolved = false;

    /** Primary guard: upstream of the armor math, uncontested by Apoli. */
    @Inject(
        method = "actuallyHurt",
        at = @At("HEAD"),
        require = 1
    )
    private void mmsCompat$ensureApoliArmorFieldsEarly(ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
        mmsCompat$ensureApoliArmorFields();
    }

    /** Backstop for paths that skip actuallyHurt. */
    @Inject(
        method = "getDamageAfterArmorAbsorb",
        at = @At("HEAD"),
        require = 1
    )
    private void mmsCompat$ensureApoliArmorFieldsLate(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        mmsCompat$ensureApoliArmorFields();
    }

    private void mmsCompat$ensureApoliArmorFields() {
        if (mmsCompat$fieldsResolved && mmsCompat$armorFields == null) {
            return;
        }
        try {
            if (!mmsCompat$fieldsResolved) {
                Field[] resolved = new Field[mmsCompat$FIELD_NAMES.length];
                for (int i = 0; i < mmsCompat$FIELD_NAMES.length; i++) {
                    resolved[i] = LivingEntity.class.getDeclaredField(mmsCompat$FIELD_NAMES[i]);
                    resolved[i].setAccessible(true);
                }
                mmsCompat$armorFields = resolved;
                mmsCompat$fieldsResolved = true;
            }
            for (Field field : mmsCompat$armorFields) {
                if (field.get(this) == null) {
                    field.set(this, Optional.empty());
                }
            }
        } catch (NoSuchFieldException e) {
            // Apoli version doesn't have these fields — nothing to guard
            mmsCompat$fieldsResolved = true;
            mmsCompat$armorFields = null;
        } catch (Exception e) {
            mmsCompat$LOG.warn("[mms_compat] failed to null-guard Apoli armor fields", e);
        }
    }
}
