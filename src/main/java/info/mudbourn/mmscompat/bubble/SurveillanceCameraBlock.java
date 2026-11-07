package info.mudbourn.mmscompat.bubble;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/** Wall camera that a linked security monitor can view. */
public class SurveillanceCameraBlock extends BaseEntityBlock {

    public static final MapCodec<SurveillanceCameraBlock> CODEC = simpleCodec(SurveillanceCameraBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final SoundEvent REGISTER_SOUND = SoundEvent.createVariableRangeEvent(
        Identifier.parse("bubblellaneous:block.lock.register")
    );

    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.box(5, 2, 3, 11, 13, 16));

    public SurveillanceCameraBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SurveillanceCameraBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical()) {
            return null;
        }
        return defaultBlockState().setValue(FACING, face);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wall = pos.relative(facing.getOpposite());
        return !level.getBlockState(wall).getCollisionShape(level, wall).isEmpty();
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
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        if (!stack.is(BubbleContent.SECURITY_MONITOR_ITEM)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Component name = stack.has(DataComponents.CUSTOM_NAME)
            ? stack.get(DataComponents.CUSTOM_NAME)
            : Component.translatable("item.bubblellaneous.default_camera");
        ItemStack linked = stack.copyWithCount(1);
        linked.set(BubbleContent.CAMERA_LINK, new CameraLink(GlobalPos.of(level.dimension(), pos), name, false));
        stack.consume(1, player);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, linked);
        } else {
            player.getInventory().placeItemBackInInventory(linked);
        }
        level.playSound(null, pos, REGISTER_SOUND, SoundSource.BLOCKS, 0.25f, 1.0f);
        player.displayClientMessage(
            Component.translatable("ui.bubblellaneous.camera.registered").withStyle(ChatFormatting.GREEN),
            false
        );
        return InteractionResult.SUCCESS;
    }
}
