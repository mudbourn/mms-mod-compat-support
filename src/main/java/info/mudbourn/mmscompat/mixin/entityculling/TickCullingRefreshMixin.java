package info.mudbourn.mmscompat.mixin.entityculling;

import dev.tr7zw.entityculling.access.Cullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

// EntityCulling's tick culling swaps a hidden entity's client tick for a cut-down one that leaves some of its
// state stale, which leaves floating, unhittable ghosts. Every few ticks this clears the culled flags just before
// the entity ticks, so it gets a full vanilla tick and catches up. A frame drawn in between is hidden by the depth
// buffer, and EntityCulling's culling thread sets the flags again on its next pass.
@Mixin(Level.class)
public abstract class TickCullingRefreshMixin {

    // A culled entity gets a full tick once in this many ticks, staggered by entity id.
    @Unique
    private static final int REFRESH_INTERVAL = 4;

    @Inject(method = "guardEntityTick", at = @At("HEAD"))
    private <T extends Entity> void mmsCompat$refreshCulledTick(Consumer<T> tick, T entity, CallbackInfo ci) {
        if (entity.level().isClientSide()
            && entity instanceof Cullable cullable
            && (entity.tickCount + entity.getId()) % REFRESH_INTERVAL == 0
            && (cullable.isCulled() || cullable.isOutOfCamera())) {
            cullable.setCulled(false);
            cullable.setOutOfCamera(false);
        }
    }
}
