package info.mudbourn.mmscompat.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public class CreativeTabDedupMixin {

    @Shadow
    @Final
    private Collection<ItemStack> tabContents;

    @Inject(
        method = "accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mmsCompat$skipDuplicateStack(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        if (this.tabContents.contains(stack)) {
            ci.cancel();
        }
    }
}
