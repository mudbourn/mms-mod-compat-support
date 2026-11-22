package info.mudbourn.mmscompat.mixin.alexscaves;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Grows the hood of darkness 1.05x so it doesn't z-fight with the Floran origin's head layer.
// The scale goes on the part's initial pose, so the per-frame pose reset keeps it.
@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.client.model.layered.DarknessArmorModel")
public abstract class DarknessHoodScaleMixin {

    private static final float HOOD_SCALE = 1.05F;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsCompat$scaleHood(ModelPart root, CallbackInfo ci) {
        ModelPart hood = root.getChild("head").getChild("hood");
        hood.setInitialPose(hood.getInitialPose().scaled(HOOD_SCALE));
        hood.resetPose();
    }
}
