package info.mudbourn.mmscompat.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Moves a mod's 1.21 saved data file into the 26.x layout the first time it is read.
 *
 * <p>Vanilla's 26.x world upgrade only moves its own files: a mod saving under the
 * {@code minecraft} namespace now reads {@code dimensions/minecraft/<dim>/data/minecraft/<name>.dat}
 * and would start empty while its data sits in {@code data/}, {@code DIM-1/data/} or
 * {@code DIM1/data/}. The old file is renamed with {@value #MIGRATED} once moved, and a
 * file already in the new place is never replaced.
 */
@Mixin(SavedDataStorage.class)
public class LegacySavedDataMixin {

    @Unique
    private static final Logger LOGGER = LogUtils.getLogger();

    @Unique
    private static final String MIGRATED = ".migrated-1.21";

    @Unique
    private static final Map<String, String> VANILLA_DIMENSIONS = Map.of(
        "overworld", "",
        "the_nether", "DIM-1",
        "the_end", "DIM1"
    );

    @Shadow
    @Final
    private Path dataFolder;

    @Shadow
    private Path getDataFile(Identifier id) {
        throw new AssertionError();
    }

    @Inject(method = "readSavedData", at = @At("HEAD"))
    private void mms$moveLegacyFile(SavedDataType<?> type, CallbackInfoReturnable<?> cir) {
        Identifier id = type.id();
        if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return;
        }
        Path current = this.getDataFile(id);
        Path legacy = this.mms$legacyFolder().resolve(id.getPath() + ".dat");
        if (Files.exists(current) || !Files.isRegularFile(legacy)) {
            return;
        }
        try {
            Files.createDirectories(current.getParent());
            Files.copy(legacy, current);
            Files.move(legacy, legacy.resolveSibling(legacy.getFileName() + MIGRATED));
            LOGGER.info("Moved 1.21 saved data {} to {}", legacy, current);
        } catch (IOException e) {
            LOGGER.error("Could not move 1.21 saved data {} to {}", legacy, current, e);
        }
    }

    // The folder that held this storage's files before 26.x: a vanilla dimension's old DIM folder, else this folder.
    @Unique
    private Path mms$legacyFolder() {
        Path dimension = this.dataFolder.getParent();
        Path namespace = dimension == null ? null : dimension.getParent();
        Path dimensions = namespace == null ? null : namespace.getParent();
        if (dimensions == null
            || !dimensions.getFileName().toString().equals("dimensions")
            || !namespace.getFileName().toString().equals(Identifier.DEFAULT_NAMESPACE)) {
            return this.dataFolder;
        }
        String old = VANILLA_DIMENSIONS.get(dimension.getFileName().toString());
        if (old == null) {
            return this.dataFolder;
        }
        return dimensions.getParent().resolve(old).resolve("data");
    }
}
