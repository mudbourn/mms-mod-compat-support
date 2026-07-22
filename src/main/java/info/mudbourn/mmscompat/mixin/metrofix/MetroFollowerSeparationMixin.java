package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.modmetro.MetroCartEntity;
import com.example.modmetro.config.MetroConfig;

/**
 * Hard minimum-separation clamp for follower carts.
 *
 * ModMetro followers are speed-matched to the car ahead by a proportional
 * controller (correction = error * 0.4) that clamps wanted speed at zero —
 * but zero speed doesn't cancel the overshoot already in flight, so when a
 * lead cart brakes hard the followers glide into it and visibly overlap
 * ("noclip") until the P-loop unwinds. Metro carts can't rely on vanilla
 * minecart collision pushing: it would fight the follower teleport logic.
 *
 * The mod already teleport-corrects the too-far case (dist > spacing + 4);
 * this is the symmetric too-close clamp. After tickFollowerCart runs, if a
 * follower ended up closer than (spacing - 0.35) to the car ahead, it is
 * placed back at exactly `spacing` along its actual approach direction and
 * given the front car's velocity, so the correction is invisible.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroFollowerSeparationMixin {

    @Shadow
    private MetroCartEntity cachedFrontCart;

    @Inject(method = "tickFollowerCart", at = @At("TAIL"))
    private void mmsCompat$clampSeparation(Level world, CallbackInfo ci) {
        MetroCartEntity front = this.cachedFrontCart;
        if (front == null || front.isRemoved()) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        double dx = self.getX() - front.getX();
        double dz = self.getZ() - front.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        double spacing = MetroConfig.spacing;
        if (dist > 0.001 && dist < spacing - 0.35) {
            double nx = dx / dist;
            double nz = dz / dist;
            self.teleportTo(front.getX() + nx * spacing, self.getY(), front.getZ() + nz * spacing);
            self.setDeltaMovement(front.getDeltaMovement());
        }
    }
}
