package info.mudbourn.mmscompat.mixin.sharedkeys;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import info.mudbourn.mmscompat.client.sharedkeys.SharedKeys;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Apoli polls the active power keys each client tick; the origins G and B read as up while they're routed elsewhere.
@Pseudo
@Mixin(targets = "io.github.apace100.apoli.ApoliClient", remap = false)
public abstract class ApoliSharedKeysMixin {

    @WrapOperation(method = "lambda$onInitializeClient$0",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean mmsCompat$routeActiveKeys(KeyMapping key, Operation<Boolean> original) {
        if ((key == SharedKeys.primary() || key == SharedKeys.secondary()) && SharedKeys.originsBlocked()) {
            return false;
        }
        return original.call(key);
    }
}
