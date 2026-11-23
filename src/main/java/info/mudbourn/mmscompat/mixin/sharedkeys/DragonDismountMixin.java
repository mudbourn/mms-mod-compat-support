package info.mudbourn.mmscompat.mixin.sharedkeys;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Crouch flies a Useless Reptile dragon down, so it only dismounts once the dragon has landed and stopped
// moving; hovering in place doesn't count. Other mounts
// dismount on crouch as usual. Runs on both sides, so the client and server agree.
@Mixin(Player.class)
public abstract class DragonDismountMixin {

    private static final double STILL = 0.01;

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$stayOnMovingDragon(CallbackInfoReturnable<Boolean> cir) {
        Entity vehicle = ((Player) (Object) this).getVehicle();
        if (vehicle != null && vehicle.getClass().getName().startsWith("nordmods.uselessreptile.")
                && (!vehicle.onGround() || vehicle.getDeltaMovement().horizontalDistanceSqr() > STILL * STILL)) {
            cir.setReturnValue(false);
        }
    }
}
