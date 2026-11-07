package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** Head rotation state of a surveillance camera, swept while someone watches. */
public class SurveillanceCameraBlockEntity extends BlockEntity {

    private static final int END_PAUSE_TICKS = 40;

    private float offset;
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

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        offset = input.getFloatOr("offset", 0.0f);
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
