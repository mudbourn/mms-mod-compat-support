package info.mudbourn.mmscompat.mixin.sharedkeys;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmscompat.client.sharedkeys.SharedKeys;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Alex's Caves reads its special ability key through ClientProxy.isKeyDown. On one of its mounts, or crouching in
// its keybind armor, the origins G counts as that key as well.
@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.ClientProxy", remap = false)
public abstract class AlexsCavesSharedKeysMixin {

    @WrapOperation(method = "isKeyDown",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean mmsCompat$specialAbility(KeyMapping key, Operation<Boolean> original) {
        boolean down = original.call(key);
        if (key == SharedKeys.acSpecial() && !down && SharedKeys.primaryIsCavesSpecial()) {
            return SharedKeys.down(SharedKeys.primary());
        }
        return down;
    }
}
