package info.mudbourn.mmscompat.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Accepts pack.mcmeta files that declare a usable format range but break
 * 1.21.9+'s cross-version field rules.
 *
 * <p>Vanilla rejects the whole pack when, for example, {@code min_format} is
 * given without {@code supported_formats}, {@code supported_formats} is kept
 * next to {@code min_format} 65+, or a single {@code pack_format} above 64 has
 * no range. Mod built-in packs that hit this (AmbientSounds, CreativeCore, the
 * Lukis structure packs and others on MMSLive01) were dropped, so their assets
 * and data never loaded. When strict validation fails, the range is taken from
 * whichever fields the pack does declare, in the order min/max, supported,
 * pack_format.
 */
@Mixin(PackFormat.IntermediaryFormat.class)
public class PackFormatLenientMixin {

    @Unique
    private static final Logger MMS$LOGGER = LogUtils.getLogger();

    @WrapMethod(method = "validate")
    private DataResult<InclusiveRange<PackFormat>> mms$fallbackRange(
        int lastPreMinor,
        boolean isPack,
        boolean hasOverlays,
        String label,
        String rangeField,
        Operation<DataResult<InclusiveRange<PackFormat>>> original
    ) {
        DataResult<InclusiveRange<PackFormat>> strict = original.call(lastPreMinor, isPack, hasOverlays, label, rangeField);
        if (strict.isSuccess()) {
            return strict;
        }
        Optional<InclusiveRange<PackFormat>> range = mms$declaredRange((PackFormat.IntermediaryFormat) (Object) this);
        if (range.isEmpty()) {
            return strict;
        }
        MMS$LOGGER.debug("Repaired pack metadata ({}): {}", label, strict.error().map(DataResult.Error::message).orElse(""));
        return DataResult.success(range.get());
    }

    @Unique
    private static Optional<InclusiveRange<PackFormat>> mms$declaredRange(PackFormat.IntermediaryFormat format) {
        if (format.min().isPresent()) {
            PackFormat min = format.min().get();
            PackFormat max = format.max().orElse(PackFormat.of(min.major(), Integer.MAX_VALUE));
            return mms$ordered(min, max);
        }
        if (format.supported().isPresent()) {
            InclusiveRange<Integer> supported = format.supported().get();
            return mms$ordered(PackFormat.of(supported.minInclusive()), PackFormat.of(supported.maxInclusive(), Integer.MAX_VALUE));
        }
        return format.format().map(major -> PackFormat.of(major).minorRange());
    }

    @Unique
    private static Optional<InclusiveRange<PackFormat>> mms$ordered(PackFormat min, PackFormat max) {
        return InclusiveRange.create(min, max).result();
    }
}
