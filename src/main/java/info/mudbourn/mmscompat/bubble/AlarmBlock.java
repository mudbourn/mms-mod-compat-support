package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;

/** Block that loops a loud alarm while it receives redstone power. */
public class AlarmBlock extends Block {

    public static final MapCodec<AlarmBlock> CODEC = simpleCodec(AlarmBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Identifier ALARM_ID = Identifier.parse("bubblellaneous:block.security_alarm.alarm");

    private static final SoundEvent ALARM = SoundEvent.createVariableRangeEvent(ALARM_ID);

    private static final int LOOP_TICKS = 201;

    private static final double STOP_RADIUS = 64.0;

    public AlarmBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
            .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getValue(POWERED)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void neighborChanged(
        BlockState state,
        Level level,
        BlockPos pos,
        Block block,
        Orientation orientation,
        boolean movedByPiston
    ) {
        if (level.isClientSide()) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered == state.getValue(POWERED)) {
            return;
        }
        level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        if (powered) {
            level.scheduleTick(pos, this, 1);
        } else if (level instanceof ServerLevel serverLevel) {
            stopAlarm(serverLevel, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.playSound(null, pos, ALARM, SoundSource.BLOCKS, 3.0f, 1.0f);
            level.scheduleTick(pos, this, LOOP_TICKS);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(
        BlockState state,
        ServerLevel level,
        BlockPos pos,
        boolean movedByPiston
    ) {
        stopAlarm(level, pos);
    }

    private static void stopAlarm(ServerLevel level, BlockPos pos) {
        level.getServer().getPlayerList().broadcast(
            null,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            STOP_RADIUS,
            level.dimension(),
            new ClientboundStopSoundPacket(ALARM_ID, SoundSource.BLOCKS)
        );
    }
}
