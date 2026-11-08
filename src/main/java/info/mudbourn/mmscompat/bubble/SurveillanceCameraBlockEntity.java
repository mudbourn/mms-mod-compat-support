package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Head rotation state of a surveillance camera. The sweep runs every tick on both sides like the
 * datapack's; the server resyncs clients when the head turns around, is turned by hand or changes mode.
 */
public class SurveillanceCameraBlockEntity extends BlockEntity {

    private static final int END_PAUSE_TICKS = 40;

    private float offset;
    private float previousOffset;
    private int direction = 1;
    private int pause;
    private boolean manual;
    private long lastAdvance = -1;

    public SurveillanceCameraBlockEntity(BlockPos pos, BlockState state) {
        super(BubbleContent.CAMERA_ENTITY, pos, state);
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
        setChanged();
        sync();
    }

    /** Head yaw for rendering, interpolated between the last two ticks. */
    public float viewYaw(float partialTick) {
        return getBlockState().getValue(SurveillanceCameraBlock.FACING).toYRot()
            + Mth.lerp(partialTick, previousOffset, offset);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SurveillanceCameraBlockEntity camera) {
        camera.previousOffset = camera.offset;
        camera.advance(level);
    }

    public float viewYaw() {
        return getBlockState().getValue(SurveillanceCameraBlock.FACING).toYRot() + offset;
    }

    public Vec3 headPos() {
        return Vec3.atCenterOf(worldPosition).add(0.0, 0.16, 0.0);
    }

    /** Turns the head one degree, refusing when a block is in the way. */
    public void turn(int side) {
        if (!blocked(side)) {
            offset += side;
            setChanged();
            sync();
        }
    }

    /** Advances the automatic sweep once per game tick. */
    public void advance(Level level) {
        if (manual || lastAdvance == level.getGameTime()) {
            return;
        }
        lastAdvance = level.getGameTime();
        if (pause > 0) {
            pause--;
            return;
        }
        if (blocked(direction)) {
            direction = -direction;
            pause = END_PAUSE_TICKS;
            setChanged();
            sync();
            return;
        }
        offset += direction;
    }

    private boolean blocked(int side) {
        if (level == null) {
            return false;
        }
        float yaw = viewYaw() * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double rightX = -Mth.cos(yaw);
        double rightZ = -Mth.sin(yaw);
        Vec3 center = Vec3.atCenterOf(worldPosition);
        BlockPos probe = BlockPos.containing(
            center.x + side * rightX + 0.5 * forwardX,
            center.y,
            center.z + side * rightZ + 0.5 * forwardZ
        );
        BlockState state = level.getBlockState(probe);
        return !state.isAir() && !state.getCollisionShape(level, probe).isEmpty();
    }

    /** Sends the server's state to watching clients so their sweep cannot drift. */
    private void sync() {
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
        offset = input.getFloatOr("offset", 0.0f);
        previousOffset = offset;
        direction = input.getIntOr("direction", 1);
        pause = input.getIntOr("pause", 0);
        manual = input.getBooleanOr("manual", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("offset", offset);
        output.putInt("direction", direction);
        output.putInt("pause", pause);
        output.putBoolean("manual", manual);
    }
}
