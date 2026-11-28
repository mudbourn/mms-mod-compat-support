package info.mudbourn.mmscompat.cc;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Axe turtles fell the whole natural tree when they dig one of its logs.
public final class TurtleTimber {
    private static final String TURTLE_PLAYER = "dan200.computercraft.shared.platform.FakePlayer";
    private static final int MAX_LOGS = 96;
    private static final int LEAF_SEARCH = 2;

    private static boolean felling;

    private TurtleTimber() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (felling || !(level instanceof ServerLevel serverLevel) || !isAxeTurtle(player, state)) return;

            felling = true;
            try {
                fell(serverLevel, player, pos);
            } finally {
                felling = false;
            }
        });
    }

    private static boolean isAxeTurtle(Player player, BlockState state) {
        return state.is(BlockTags.LOGS)
            && player.getClass().getName().equals(TURTLE_PLAYER)
            && player.getMainHandItem().is(ItemTags.AXES);
    }

    // Drops every connected log at the dug position so the turtle's drop capture collects them.
    private static void fell(ServerLevel level, Player player, BlockPos origin) {
        List<BlockPos> logs = connectedLogs(level, origin);
        if (logs.isEmpty() || !hasNaturalLeaves(level, logs)) return;

        ItemStack tool = player.getMainHandItem();
        List<ItemStack> drops = new ArrayList<>();
        for (BlockPos pos : logs) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            drops.addAll(Block.getDrops(state, level, pos, blockEntity, player, tool));
            level.destroyBlock(pos, false, player);
        }
        for (ItemStack drop : drops) {
            Block.popResource(level, origin, drop);
        }
    }

    private static List<BlockPos> connectedLogs(ServerLevel level, BlockPos origin) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> open = new ArrayDeque<>();
        seen.add(origin);
        open.add(origin);
        while (!open.isEmpty() && found.size() < MAX_LOGS) {
            BlockPos pos = open.poll();
            for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockPos candidate = next.immutable();
                if (!seen.add(candidate) || !level.getBlockState(candidate).is(BlockTags.LOGS)) continue;

                found.add(candidate);
                open.add(candidate);
                if (found.size() >= MAX_LOGS) break;
            }
        }
        return found;
    }

    private static boolean hasNaturalLeaves(ServerLevel level, List<BlockPos> logs) {
        for (BlockPos log : logs) {
            BlockPos low = log.offset(-LEAF_SEARCH, -LEAF_SEARCH, -LEAF_SEARCH);
            BlockPos high = log.offset(LEAF_SEARCH, LEAF_SEARCH, LEAF_SEARCH);
            for (BlockPos near : BlockPos.betweenClosed(low, high)) {
                BlockState state = level.getBlockState(near);
                if (state.is(BlockTags.LEAVES)
                    && state.hasProperty(LeavesBlock.PERSISTENT)
                    && !state.getValue(LeavesBlock.PERSISTENT)) {
                    return true;
                }
            }
        }
        return false;
    }
}
