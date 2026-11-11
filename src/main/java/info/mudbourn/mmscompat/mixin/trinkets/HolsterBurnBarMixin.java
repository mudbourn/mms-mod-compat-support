package info.mudbourn.mmscompat.mixin.trinkets;

import dev.emi.trinkets.api.TrinketInventory;
import info.mudbourn.mmscompat.ca.BeltLight;
import info.mudbourn.mmscompat.ca.LanternHolster;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws the holster burn progress as a durability bar on the light stack in the
 * local player's belt slot.
 */
@Mixin(ItemStack.class)
public abstract class HolsterBurnBarMixin {

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void mms_compat$burnBarVisible(CallbackInfoReturnable<Boolean> cir) {
        if (mms_compat$burnLeft() >= 0.0F) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void mms_compat$burnBarWidth(CallbackInfoReturnable<Integer> cir) {
        float left = mms_compat$burnLeft();
        if (left >= 0.0F) {
            cir.setReturnValue(Math.round(13.0F * left));
        }
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void mms_compat$burnBarColor(CallbackInfoReturnable<Integer> cir) {
        float left = mms_compat$burnLeft();
        if (left >= 0.0F) {
            cir.setReturnValue(Mth.hsvToRgb(left / 3.0F, 1.0F, 1.0F));
        }
    }

    // Fraction of burn time left for this stack when it is the belt light, or -1.
    @Unique
    private float mms_compat$burnLeft() {
        if (!FabricLoader.getInstance().isModLoaded("frostiful")) {
            return -1.0F;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return -1.0F;
        }
        TrinketInventory slot = BeltLight.slot(player);
        if (slot == null || slot.getItem(0) != (Object) this || BeltLight.lightOf((ItemStack) (Object) this) <= 0) {
            return -1.0F;
        }
        int warded = player.getAttachedOrElse(LanternHolster.WARDED_TICKS, 0);
        if (warded <= 0) {
            return -1.0F;
        }
        return Mth.clamp(1.0F - (float) warded / LanternHolster.BURN_TICKS, 0.0F, 1.0F);
    }
}
