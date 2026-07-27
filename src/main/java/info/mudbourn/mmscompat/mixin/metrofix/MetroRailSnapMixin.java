package info.mudbourn.mmscompat.mixin.metrofix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import info.mudbourn.mmscompat.metro.MetroRailPath;
import info.mudbourn.mmscompat.metro.MetroTuning;

import com.example.modmetro.MetroCartEntity;

/**
 * Fixes the floating-follower-cart bug: ModMetro snaps carts to
 * {@code railY + 0.5} in {@code tickFollowerCart}, but a minecart riding a
 * flat rail actually sits at {@code railY + 0.0625}. The lead cart is never
 * re-snapped so it always looks right; followers float ~0.44 blocks up the
 * moment either recovery branch below fires.
 *
 * Two of {@code tickFollowerCart}'s three {@code teleportTo} calls carry the
 * bug (the third copies the front cart's own Y verbatim and needs no fix
 * here). Both are corrected in place via ordinal-targeted redirects rather
 * than a single blanket one, so the untouched third call — and every other
 * {@code teleportTo} in the class — is left alone.
 *
 * Also hardens {@code isOnRail}, which currently only checks whether the
 * cart's block column contains rail — a cart hovering at {@code +0.5} is
 * still "on" the same column, so recovery never re-fires and the float is
 * permanent. And reorders {@code findNearestRail} so it only returns a rail
 * above the cart as a last resort, since that adds a further full block of
 * lift on top of the {@code +0.5} bug.
 */
@Mixin(MetroCartEntity.class)
public abstract class MetroRailSnapMixin {

    /** railY + 0.5 -> railY + 0.0625: the gap this redirect removes. */
    private static final double LIFT_CORRECTION = 0.5 - 0.0625;

    @Redirect(method = "tickFollowerCart", at = @At(value = "INVOKE",
            target = "Lcom/example/modmetro/MetroCartEntity;teleportTo(DDD)V", ordinal = 0))
    private void mmsCompat$fixOffRailRecoveryY(MetroCartEntity self, double x, double y, double z) {
        self.teleportTo(x, y - LIFT_CORRECTION, z);
    }

    @Redirect(method = "tickFollowerCart", at = @At(value = "INVOKE",
            target = "Lcom/example/modmetro/MetroCartEntity;teleportTo(DDD)V", ordinal = 1))
    private void mmsCompat$fixCatchupSnapY(MetroCartEntity self, double x, double y, double z) {
        self.teleportTo(x, y - LIFT_CORRECTION, z);
    }

    /**
     * A cart floating at {@code railY + 0.5} sits in the same block column as
     * the rail beneath it, so ModMetro's own check reads it as on-rail and
     * the recovery branch that would fix it never runs. Reject that: if the
     * cart is riding meaningfully above where a flat rail would put it,
     * treat it as off-rail so {@code tickFollowerCart}'s recovery fires.
     */
    @Inject(method = "isOnRail", at = @At("RETURN"), cancellable = true)
    private void mmsCompat$hardenIsOnRail(Level world, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        MetroCartEntity self = (MetroCartEntity) (Object) this;
        double expectedY = Math.floor(self.getY()) + 0.0625;
        double verticalError = self.getY() - expectedY;
        if (verticalError > MetroTuning.rail_vertical_tolerance) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Same candidates as ModMetro's own scan, reordered so a rail directly
     * above the cart is only ever returned once every at-or-below candidate
     * (the block itself, straight down, and the four horizontals at both
     * those heights) has failed. The original order tried "up" second,
     * ahead of every horizontal, which routinely handed followers a rail one
     * block too high on top of the separate {@code +0.5} snap bug.
     */
    @Overwrite
    private BlockPos findNearestRail(Level world, BlockPos center) {
        if (MetroRailPath.isRail(world, center)) {
            return center;
        }

        BlockPos down = center.below();
        if (MetroRailPath.isRail(world, down)) {
            return down;
        }

        BlockPos[] horizontals = { center.north(), center.south(), center.east(), center.west() };

        for (BlockPos h : horizontals) {
            if (MetroRailPath.isRail(world, h)) {
                return h;
            }
            if (MetroRailPath.isRail(world, h.below())) {
                return h.below();
            }
        }

        BlockPos up = center.above();
        if (MetroRailPath.isRail(world, up)) {
            return up;
        }

        for (BlockPos h : horizontals) {
            if (MetroRailPath.isRail(world, h.above())) {
                return h.above();
            }
        }

        return null;
    }
}
