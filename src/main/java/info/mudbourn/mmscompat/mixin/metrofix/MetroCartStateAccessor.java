package info.mudbourn.mmscompat.mixin.metrofix;

import com.example.modmetro.MetroCartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read/write access to MetroCartEntity's private per-cart state on carts
 * OTHER than {@code this}.
 *
 * {@code @Shadow} only reaches the instance a mixin is applied to, and
 * reversing a train ({@link MetroReverseConsistMixin}) has to rewrite every
 * cart in the consist at once. See {@link MetroCartLineAccessor} for the
 * read-only line-name accessor that predates this.
 */
@Mixin(value = MetroCartEntity.class, remap = false)
public interface MetroCartStateAccessor {

    @Accessor("lastDirection")
    Vec3 mmsCompat$getLastDirection();

    @Accessor("lastDirection")
    void mmsCompat$setLastDirection(Vec3 dir);

    @Accessor("lastStationPos")
    BlockPos mmsCompat$getLastStationPos();

    @Accessor("lastStationPos")
    void mmsCompat$setLastStationPos(BlockPos pos);

    @Accessor("lineName")
    String mmsCompat$getLine();

    @Accessor("lineName")
    void mmsCompat$setLine(String lineName);

    @Accessor("cachedFrontCart")
    void mmsCompat$setCachedFrontCart(MetroCartEntity cart);

    @Accessor("cachedLeadCart")
    void mmsCompat$setCachedLeadCart(MetroCartEntity cart);

    @Accessor("isWaiting")
    void mmsCompat$setWaiting(boolean waiting);

    @Accessor("stationWaitTimer")
    void mmsCompat$setStationWaitTimer(int ticks);
}
