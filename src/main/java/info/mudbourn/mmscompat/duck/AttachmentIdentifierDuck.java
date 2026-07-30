package info.mudbourn.mmscompat.duck;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Remembers which attachment a puzzleslib data-attachment builder is building.
 *
 * <p>The builder only ever sees its identifier as an argument to {@code build},
 * but its synchronization predicate is invoked long afterwards, by which point
 * there is no way to tell one attachment from another. Stashing the identifier
 * on the builder lets the predicate be overridden for one specific attachment
 * instead of for every mod using the library.
 *
 * @see info.mudbourn.mmscompat.mixin.mutantmonsters.CreeperMinionShoulderSyncMixin
 */
public interface AttachmentIdentifierDuck {

    @Nullable
    Identifier mmsCompat$attachmentId();

    void mmsCompat$setAttachmentId(Identifier identifier);
}
