package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Invisible viewpoint a player spectates while watching a surveillance camera; the client turns it with the head. */
public class CameraViewEntity extends Entity {

    private static final EntityDataAccessor<BlockPos> CAMERA =
        SynchedEntityData.defineId(CameraViewEntity.class, EntityDataSerializers.BLOCK_POS);

    private float clientYaw = Float.NaN;

    public CameraViewEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CAMERA, BlockPos.ZERO);
    }

    public void setCamera(BlockPos pos) {
        entityData.set(CAMERA, pos);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            return;
        }
        if (level().getBlockEntity(entityData.get(CAMERA)) instanceof SurveillanceCameraBlockEntity camera) {
            float yaw = camera.viewYaw();
            yRotO = Float.isNaN(clientYaw) ? yaw : clientYaw;
            setYRot(yaw);
            clientYaw = yaw;
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
