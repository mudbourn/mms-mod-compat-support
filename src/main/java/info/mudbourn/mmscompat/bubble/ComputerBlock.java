package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Desktop computer that boots to a login screen when clicked. */
public class ComputerBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);

    public static final EnumProperty<ComputerState> STATE = EnumProperty.create("state", ComputerState.class);

    private static final int BOOT_TICKS = 66;

    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(0, 0, 1, 16, 16, 13));

    public ComputerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(STATE, ComputerState.DEFAULT));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        switch (state.getValue(STATE)) {
            case DEFAULT -> {
                level.setBlock(pos, state.setValue(STATE, ComputerState.BOOT), Block.UPDATE_ALL);
                level.scheduleTick(pos, this, BOOT_TICKS);
            }
            case LOGIN -> level.setBlock(pos, state.setValue(STATE, ComputerState.DEFAULT), Block.UPDATE_ALL);
            case BOOT -> { }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STATE) == ComputerState.BOOT) {
            level.setBlock(pos, state.setValue(STATE, ComputerState.LOGIN), Block.UPDATE_ALL);
        }
    }
}
