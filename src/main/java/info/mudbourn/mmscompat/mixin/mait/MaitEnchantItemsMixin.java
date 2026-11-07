package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class MaitEnchantItemsMixin {

    private static final String PERFECT_PARRY = "enchantment.mob-ai-tweaks.perfect_parry";
    private static final String DESPISE = "enchantment.mob-ai-tweaks.despise";

    @Inject(method = {"isSupportedItem", "canEnchant"}, at = @At("HEAD"), cancellable = true)
    private void mms$retargetMaitItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        String key = mms$key((Enchantment) (Object) this);
        if (PERFECT_PARRY.equals(key) && stack.has(DataComponents.BLOCKS_ATTACKS)) {
            cir.setReturnValue(true);
        } else if (DESPISE.equals(key) && !(stack.getItem() instanceof TridentItem)) {
            cir.setReturnValue(false);
        }
    }

    // Translation key of the enchantment's name, or null for a literal name.
    private static String mms$key(Enchantment enchantment) {
        return enchantment.description().getContents() instanceof TranslatableContents contents
            ? contents.getKey()
            : null;
    }
}
