package info.mudbourn.mmscompat.mixin.aerialhell;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * Fixes a disconnect whenever a Biome Shifter block entity loads without a
 * {@code field_size} tag.
 *
 * <p>{@code BiomeShifterBlockEntity.loadAdditional} reads the tag as
 *
 * <pre>    this.fieldSize = (Integer) view.getInt("field_size").get();</pre>
 *
 * with no presence check, so an absent tag throws
 * {@code NoSuchElementException: No value present} straight out of
 * deserialisation. On the client that lands inside Vertigo's
 * {@code ChunkSectionLoadPacket}, which calls {@code loadWithComponents} on
 * every block entity in the section it just received — an exception there is an
 * unhandled packet-handling error, which drops the connection. Any Biome
 * Shifter in view range takes the client down repeatedly, since the same
 * section is re-sent on every reconnect.
 *
 * <p>The right fallback is to keep the value the block entity already has.
 * {@code BiomeShifterBlock.newBlockEntity} constructs it with the block's own
 * {@code fieldSize}, so at the point {@code loadAdditional} runs the field is
 * already correct for that block — the NBT read is only there to restore a
 * value that may have been changed at runtime. Leaving it untouched on a
 * missing tag is exactly the pre-load state, not a guess.
 *
 * <p>Wrapping {@code Optional.get()} rather than the {@code getInt} call keeps
 * the injection point free of Minecraft types: the descriptor is pure
 * {@code java.util}, so the wrap survives remapping untouched. There is exactly
 * one {@code Optional.get()} in the method, so the target is unambiguous.
 *
 * <p><b>Remapping is left on here, unlike the other Aerial Hell mixins.</b>
 * They target methods Aerial Hell declares itself, which no mapping set
 * renames. {@code loadAdditional} is inherited from {@code BlockEntity}, so it
 * is {@code method_11014} in the shipped jar and must be remapped to match at
 * runtime. Loom can do that because Aerial Hell is a {@code modCompileOnly}
 * dependency and is remapped into the dev mappings alongside Minecraft — the
 * opposite of {@code DisplayCullingMixin}, whose target is not a dependency at
 * all and therefore has to be spelled in intermediary by hand.
 *
 * <p>Applied on both sides: the throw is in common deserialisation code, and a
 * dedicated server reading the same block entity off disk would hit it too.
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.BlockEntity.BiomeShifterBlockEntity")
public abstract class BiomeShifterFieldSizeMixin {

    @Shadow private int fieldSize;

    @WrapOperation(
        method = "loadAdditional",
        at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;")
    )
    private Object mmsCompat$keepFieldSizeWhenTagMissing(Optional<Object> stored, Operation<Object> original) {
        return stored.orElse(this.fieldSize);
    }
}
