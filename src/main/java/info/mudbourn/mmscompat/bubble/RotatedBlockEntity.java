package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Free yaw of a ported Bubblellaneous block placed facing the player, like the datapack's player_precise facing. */
public class RotatedBlockEntity extends BlockEntity {

    private float yaw = Float.NaN;

    public RotatedBlockEntity(BlockPos pos, BlockState state) {
        super(BubbleContent.ROTATED_ENTITY, pos, state);
    }

    /** Yaw the block's front faces, falling back to its facing. */
    public float yaw() {
        return Float.isNaN(yaw) ? getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot() : yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = Mth.wrapDegrees(yaw);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        yaw = input.getFloatOr("yaw", Float.NaN);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!Float.isNaN(yaw)) {
            output.putFloat("yaw", yaw);
        }
    }
}
