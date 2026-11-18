package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.puzzleslib.common.impl.attachment.builder.DataAttachmentBuilder;
import info.mudbourn.mmscompat.duck.AttachmentIdentifierDuck;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Syncs the creeper minion shoulder attachments to the players tracking the holder. */
@Mixin(DataAttachmentBuilder.class)
public abstract class CreeperMinionShoulderSyncMixin {

    @Unique
    private static final Identifier LEFT_SHOULDER =
            Identifier.fromNamespaceAndPath("mutantmonsters", "left_shoulder_creeper_minion");

    @Unique
    private static final Identifier RIGHT_SHOULDER =
            Identifier.fromNamespaceAndPath("mutantmonsters", "right_shoulder_creeper_minion");

    @Inject(method = "syncWith", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$syncShoulderMinionToTrackers(
        Object holder,
        ServerPlayer serverPlayer,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(this instanceof AttachmentIdentifierDuck duck)) {
            return;
        }
        Identifier identifier = duck.mmsCompat$attachmentId();
        if (LEFT_SHOULDER.equals(identifier) || RIGHT_SHOULDER.equals(identifier)) {
            cir.setReturnValue(true);
        }
    }
}
