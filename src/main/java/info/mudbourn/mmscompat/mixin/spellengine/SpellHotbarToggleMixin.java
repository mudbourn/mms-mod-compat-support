package info.mudbourn.mmscompat.mixin.spellengine;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import info.mudbourn.mmscompat.client.spellengine.SpellHotbarToggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Spell Engine casts with number keys 1-9 whenever a spell item is held, so slot
// switching by number key stops working. In the pack the spell hotbar is live only
// while the spell hotbar toggle (period) is on; while it is off, Spell Engine treats
// the hotbar as bypassed. The first isDown in handleSlotsInternal is the
// bypass_spell_hotbar check.
@Mixin(targets = "net.spell_engine.client.input.SpellHotbar")
public abstract class SpellHotbarToggleMixin {
    @WrapOperation(
        method = "handleSlotsInternal",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0)
    )
    private boolean mmsCompat$bypassUnlessToggled(KeyMapping bypass, Operation<Boolean> original) {
        return original.call(bypass) || !SpellHotbarToggle.on();
    }
}
