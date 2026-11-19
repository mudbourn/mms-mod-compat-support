package info.mudbourn.mmscompat.mixin.spellengine;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import info.mudbourn.mmscompat.client.spellengine.SpellContainers;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Spell Engine puts a spell on right mouse when spellHotbarUseKey is on, even when the
// spell comes from the offhand. Then a gun or other main-hand item loses right mouse.
// In the pack the main hand wins: an offhand spell container does not take right
// mouse unless the main-hand item carries spells itself. Offhand spells stay on the
// spell hotbar toggle.
@Mixin(targets = "net.spell_engine.client.input.SpellHotbar")
public abstract class SpellHotbarMainHandUseMixin {
    @ModifyExpressionValue(
        method = "update",
        at = @At(value = "FIELD", target = "Lnet/spell_engine/config/ClientConfig;spellHotbarUseKey:Z")
    )
    private boolean mmsCompat$mainHandOwnsUse(boolean useKey, @Local(argsOnly = true) LocalPlayer player) {
        return useKey && !SpellContainers.offhandOnly(player);
    }
}
