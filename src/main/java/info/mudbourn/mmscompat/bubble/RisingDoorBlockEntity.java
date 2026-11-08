package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Client-side slide timing of a rising door panel; holds no saved data. */
public class RisingDoorBlockEntity extends BlockEntity {

    private static final float SLIDE_TICKS = 5.0f;

    private long changedAt = Long.MIN_VALUE;

    public RisingDoorBlockEntity(BlockPos pos, BlockState state) {
        super(BubbleContent.RISING_DOOR_ENTITY, pos, state);
    }

    @Override
    public void setBlockState(BlockState state) {
        boolean was = getBlockState().getValue(RisingDoorBlock.OPEN);
        super.setBlockState(state);
        if (level != null && level.isClientSide() && was != state.getValue(RisingDoorBlock.OPEN)) {
            changedAt = level.getGameTime();
        }
    }

    /** Fraction of the way from closed to open, eased over the slide. */
    public float openness(float partialTick) {
        boolean open = getBlockState().getValue(RisingDoorBlock.OPEN);
        if (level == null || changedAt == Long.MIN_VALUE) {
            return open ? 1.0f : 0.0f;
        }
        float progress = Mth.clamp((level.getGameTime() - changedAt + partialTick) / SLIDE_TICKS, 0.0f, 1.0f);
        return open ? progress : 1.0f - progress;
    }
}
