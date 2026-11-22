package info.mudbourn.mmscompat.mixin.thirdperson;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// The camera aim ray uses block outlines, so grass and flowers catch it; colliders only lets it pass through anything you can walk through.
@Pseudo
@Mixin(targets = "io.github.leawind.thirdperson.internal.bridge.MinecraftSpatialQuerying", remap = false)
public abstract class AimIgnoresPassableMixin {

    @ModifyVariable(method = "clipBlocks", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean mmsCompat$collidersOnly(boolean colliders) {
        return true;
    }
}
