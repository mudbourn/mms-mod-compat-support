package info.mudbourn.mmscompat.mixin.scorchful;

import com.github.thedeathlycow.scorchful.world.SandstormEffects;
import com.github.thedeathlycow.scorchful.world.ScorchfulBiome;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Scorchful's breezes join the monster spawns out under the sky during a sandstorm. Its own
// NaturalSpawnerMixin is left out on 26.3 (mms-uproot), because it wraps a getMobsAt that took the
// biome holder; this wraps 26.3's ChunkGenerator.getMobsAt in NaturalSpawner.mobsAt instead, with
// the same rule: only the biome's plain monster list (not a structure's), and only where
// SandstormEffects.canBreezesSpawnAt holds (config on, raining, a sandstorm biome, open sky).
@Mixin(NaturalSpawner.class)
public abstract class SandstormBreezeSpawnMixin {

    @WrapOperation(
        method = "mobsAt",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getMobsAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/util/random/WeightedList;")
    )
    private static WeightedList<MobSpawnSettings.SpawnerData> mmsCompat$sandstormBreezes(
            ChunkGenerator generator, Level level, StructureManager structures, MobCategory category, BlockPos pos,
            Operation<WeightedList<MobSpawnSettings.SpawnerData>> original) {
        WeightedList<MobSpawnSettings.SpawnerData> mobs = original.call(generator, level, structures, category, pos);
        if (category != MobCategory.MONSTER || !(level instanceof ServerLevel server)) return mobs;
        Holder<Biome> biome = server.getBiome(pos);
        MobSpawnSettings natural = server.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos);
        if (mobs != natural.getMobsToSpawn(category)) return mobs;
        if (!SandstormEffects.canBreezesSpawnAt(server, pos, biome)) return mobs;
        return ((ScorchfulBiome) (Object) biome.value()).scorchful$scorchfulBreezeModifiedSpawnData(mobs);
    }
}
