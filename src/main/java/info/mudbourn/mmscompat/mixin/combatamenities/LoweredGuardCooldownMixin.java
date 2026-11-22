package info.mudbourn.mmscompat.mixin.combatamenities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Combat Amenities puts a shield on a 10-tick cooldown when it is lowered, but only through
// ShieldItem.releaseUsing. Anything else that blocks, a blocking weapon or a shield that is not
// a ShieldItem, got no cooldown and could be raised again at once. This gives them the same one.
@Mixin(LivingEntity.class)
public abstract class LoweredGuardCooldownMixin {

    private static final int LOWERED_COOLDOWN_TICKS = 10;

    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void mmsCompat$coolLoweredGuard(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) {
            return;
        }
        ItemStack using = player.getUseItem();
        if (using.isEmpty() || using.getItem() instanceof ShieldItem || !using.has(DataComponents.BLOCKS_ATTACKS)
            || player.getCooldowns().isOnCooldown(using)) {
            return;
        }
        player.getCooldowns().addCooldown(using, LOWERED_COOLDOWN_TICKS);
    }
}
