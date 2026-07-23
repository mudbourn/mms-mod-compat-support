package info.mudbourn.mmscompat.mixin.metrofix;

import com.example.modmetro.MetroCartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes MetroCartEntity's server-only {@code lineName} field.
 *
 * ModMetro never syncs the line to clients (only CURRENT/NEXT_STATION are
 * data-tracked), so the line-sync payload reads it server-side through this
 * accessor. See {@code MetroLineSyncServer}.
 */
@Mixin(value = MetroCartEntity.class, remap = false)
public interface MetroCartLineAccessor {

    @Accessor("lineName")
    String mmsCompat$getLineName();
}
