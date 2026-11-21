package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** Shared placement and backfill for the blocks drawn by {@link RotatedBlockEntity}. */
public final class RotatedBlocks {

    private RotatedBlocks() {}

    static boolean rotated(Block block) {
        return block == BubbleContent.CLIPBOARD || block == BubbleContent.COMPUTER;
    }

    static BlockEntity create(BlockPos pos, BlockState state) {
        return new RotatedBlockEntity(pos, state);
    }

    // Turns the placed block to face its placer at the placer's exact yaw.
    static void place(Level level, BlockPos pos, LivingEntity placer) {
        if (placer != null && level.getBlockEntity(pos) instanceof RotatedBlockEntity rotated) {
            rotated.setYaw(Mth.wrapDegrees(placer.getYRot() + 180.0f));
        }
    }

    /** Creates the block entity for clipboards and computers placed before they had one. */
    public static void backfill(LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int index = 0; index < sections.length; index++) {
            LevelChunkSection section = sections[index];
            if (section.hasOnlyAir() || !section.maybeHas(state -> rotated(state.getBlock()))) {
                continue;
            }
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(index));
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        if (rotated(section.getBlockState(x, y, z).getBlock())) {
                            BlockPos pos = chunk.getPos().getBlockAt(x, baseY + y, z);
                            chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
                        }
                    }
                }
            }
        }
    }
}
