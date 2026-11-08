package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Iron rising door whose connected panels open and close together, by hand or redstone. */
public class RisingDoorBlock extends BaseEntityBlock {

    public static final MapCodec<RisingDoorBlock> CODEC = simpleCodec(RisingDoorBlock::new);

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public static final BooleanProperty ABOVE = BooleanProperty.create("above");

    private static final SoundEvent OPEN_SOUND = SoundEvent.createVariableRangeEvent(
        Identifier.parse("bubblellaneous:block.safe_block.open")
    );

    private static final SoundEvent CLOSE_SOUND = SoundEvent.createVariableRangeEvent(
        Identifier.parse("bubblellaneous:block.safe_block.close")
    );

    private static final int MAX_GROUP = 512;

    private static final VoxelShape SHAPE_X = Block.box(0, 0, 7, 16, 16, 9);

    private static final VoxelShape SHAPE_Z = Block.box(7, 0, 0, 9, 16, 16);

    public RisingDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(AXIS, Direction.Axis.X)
            .setValue(OPEN, false)
            .setValue(POWERED, false)
            .setValue(ABOVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RisingDoorBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, OPEN, POWERED, ABOVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        return defaultBlockState()
            .setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis())
            .setValue(POWERED, level.hasNeighborSignal(pos))
            .setValue(ABOVE, level.getBlockState(pos.above()).is(this));
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess ticks,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        RandomSource random
    ) {
        if (direction == Direction.UP) {
            return state.setValue(ABOVE, neighborState.is(this));
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    protected VoxelShape getCollisionShape(
        BlockState state,
        BlockGetter level,
        BlockPos pos,
        CollisionContext context
    ) {
        if (state.getValue(OPEN)) {
            return Shapes.empty();
        }
        return getShape(state, level, pos, context);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide()) {
            boolean open = !state.getValue(OPEN);
            if (open || !anyPowered(level, pos)) {
                setGroup(level, pos, open, true);
            }
        }
        return InteractionResult.SUCCESS;
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
            setGroup(level, pos, true, true);
        } else if (!anyPowered(level, pos)) {
            setGroup(level, pos, false, true);
        }
    }

    /** Gives doors placed before the door had a block entity their missing one. */
    public static void backfill(LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int index = 0; index < sections.length; index++) {
            LevelChunkSection section = sections[index];
            if (section.hasOnlyAir() || !section.maybeHas(state -> state.getBlock() instanceof RisingDoorBlock)) {
                continue;
            }
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(index));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (section.getBlockState(x, y, z).getBlock() instanceof RisingDoorBlock) {
                            BlockPos pos = chunk.getPos().getBlockAt(x, baseY + y, z);
                            chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
                        }
                    }
                }
            }
        }
    }

    private void setGroup(Level level, BlockPos origin, boolean open, boolean sound) {
        boolean changed = false;
        for (BlockPos member : group(level, origin)) {
            BlockState current = level.getBlockState(member);
            if (current.getValue(OPEN) != open) {
                level.setBlock(member, current.setValue(OPEN, open), Block.UPDATE_ALL);
                changed = true;
            }
        }
        if (changed && sound) {
            level.playSound(null, origin, open ? OPEN_SOUND : CLOSE_SOUND, SoundSource.BLOCKS, 0.5f, 1.0f);
        }
    }

    private boolean anyPowered(Level level, BlockPos origin) {
        return group(level, origin).stream().anyMatch(member -> level.getBlockState(member).getValue(POWERED));
    }

    private List<BlockPos> group(Level level, BlockPos origin) {
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> members = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Direction.Axis axis = level.getBlockState(origin).getValue(AXIS);
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty() && members.size() < MAX_GROUP) {
            BlockPos current = queue.poll();
            members.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!seen.add(next) || !level.isLoaded(next)) {
                    continue;
                }
                BlockState neighbor = level.getBlockState(next);
                if (neighbor.is(this) && neighbor.getValue(AXIS) == axis) {
                    queue.add(next);
                }
            }
        }
        return members;
    }
}
