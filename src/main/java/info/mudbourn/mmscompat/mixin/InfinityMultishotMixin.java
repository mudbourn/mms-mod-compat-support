package info.mudbourn.mmscompat.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Infinity and Multishot share a crossbow.
 *
 * Combat Amenities adds Infinity to the crossbow exclusive set, which is also
 * Multishot's, so an anvil refused the pair. Piercing keeps its vanilla
 * conflict with Multishot.
 */
@Mixin(Enchantment.class)
public abstract class InfinityMultishotMixin {

    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void mmsCompat$allowInfinityMultishot(
            Holder<Enchantment> first,
            Holder<Enchantment> second,
            CallbackInfoReturnable<Boolean> cir) {
        if ((first.is(Enchantments.INFINITY) && second.is(Enchantments.MULTISHOT))
                || (first.is(Enchantments.MULTISHOT) && second.is(Enchantments.INFINITY))) {
            cir.setReturnValue(true);
        }
    }
}
