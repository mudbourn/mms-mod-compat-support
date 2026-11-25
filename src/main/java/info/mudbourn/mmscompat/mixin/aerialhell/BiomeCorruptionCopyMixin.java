package info.mudbourn.mmscompat.mixin.aerialhell;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Refills biomes for Aerial Hell's shadow spread section by section into fresh copies, skipping faster biome fills that rewrite in place.
@Mixin(targets = "fr.factionbedrock.aerialhell.Util.BlockHelper")
public abstract class BiomeCorruptionCopyMixin {

    @WrapOperation(
        method = {"corruptBiome", "uncorruptBiome"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;fillBiomesFromNoise(Lnet/minecraft/world/level/biome/BiomeResolver;)V"),
        require = 0)
    private static void mmsCompat$fillByCopy(ChunkAccess chunk, BiomeResolver resolver, Operation<Void> original) {
        ChunkPos pos = chunk.getPos();
        int quartX = QuartPos.fromBlock(pos.getMinBlockX());
        int quartZ = QuartPos.fromBlock(pos.getMinBlockZ());
        LevelHeightAccessor height = chunk.getHeightAccessorForGeneration();
        for (int sectionY = height.getMinSectionY(); sectionY <= height.getMaxSectionY(); sectionY++) {
            chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY)).fillBiomesFromNoise(resolver, quartX, QuartPos.fromSection(sectionY), quartZ);
        }
    }
}
