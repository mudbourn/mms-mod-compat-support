package info.mudbourn.mmscompat.mixin.sharedkeys;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmscompat.client.sharedkeys.SharedKeys;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// A ridden dragon's attack keys also answer to the origins G (primary) and B (secondary). tickRidden reads
// fly down, secondary, primary, then free look, in that order.
@Pseudo
@Mixin(targets = "nordmods.uselessreptile.common.entity.base.URRideableDragonEntity", remap = false)
public abstract class UselessReptileSharedKeysMixin {

    // Crouch flies the dragon down, alongside Useless Reptile's own fly-down key.
    @WrapOperation(method = "tickRidden",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 0))
    private boolean mmsCompat$flyDown(KeyMapping key, Operation<Boolean> original) {
        return original.call(key) || net.minecraft.client.Minecraft.getInstance().options.keyShift.isDown();
    }

    @WrapOperation(method = "tickRidden",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 1))
    private boolean mmsCompat$secondaryAttack(KeyMapping key, Operation<Boolean> original) {
        return original.call(key) || SharedKeys.down(SharedKeys.secondary());
    }

    @WrapOperation(method = "tickRidden",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    private boolean mmsCompat$primaryAttack(KeyMapping key, Operation<Boolean> original) {
        return original.call(key) || SharedKeys.down(SharedKeys.primary());
    }
}
