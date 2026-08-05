package info.mudbourn.mmscompat.mixin.frostiful;

import info.mudbourn.mmscompat.VanityMarker;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops vanity ice skates from actually skating.
 *
 * <h2>Why this one needs a marker</h2>
 *
 * <p>Frostiful gates skating on the {@code frostiful:ice_skates} <em>item tag</em>,
 * not on a component and not on the item's identity in a way a skin could dodge. Tag
 * membership is a property of the item, so nothing written onto a stack changes it,
 * and there is no equipment asset to skin the skates onto — they are drawn by the
 * mod's own {@code IceSkateFeatureRenderer} from a custom model keyed to the item.
 * Both cheaper routes are closed, which is exactly the case {@link VanityMarker}
 * exists for.
 *
 * <h2>Where it injects</h2>
 *
 * <p>{@code frostiful$isWearingSkates()} is the single funnel. It is declared on
 * Frostiful's {@code IceSkater} interface and implemented into {@code LivingEntity}
 * by their {@code ice_skating.LivingEntityMovementMixin}, and everything downstream —
 * the skating flag, the movement changes, the skate sounds, the skating pose — asks
 * it rather than re-reading the tag. One false is the whole effect.
 *
 * <p>Because the method arrives from another mod's mixin rather than from the class
 * itself, this can only apply after theirs has. That is what the low priority buys:
 * mixins apply in ascending priority order, so a higher number here would try to
 * inject into a method that does not exist yet and fail the whole config. The gate
 * class already makes this file optional when Frostiful is absent; the priority makes
 * it correct when Frostiful is present.
 *
 * <h2>What it deliberately does not do</h2>
 *
 * <p>It does not touch rendering. {@code IceSkateFeatureRenderer} reads the foot
 * stack's tag directly rather than going through this method, so vanity skates are
 * still drawn on the feet — which is the entire point of handing them out. If a
 * future Frostiful version routes its renderer through {@code isWearingSkates} too,
 * this would make them invisible, and the fix would be to gate on the server side
 * only rather than to abandon the approach.
 */
@Mixin(value = LivingEntity.class, priority = 500)
public abstract class IceSkateVanityMixin {

    @Inject(method = "frostiful$isWearingSkates", at = @At("HEAD"), cancellable = true, remap = false)
    private void mmsCompat$vanitySkatesDoNotSkate(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (VanityMarker.isVanity(self.getItemBySlot(EquipmentSlot.FEET))) {
            cir.setReturnValue(false);
        }
    }
}
