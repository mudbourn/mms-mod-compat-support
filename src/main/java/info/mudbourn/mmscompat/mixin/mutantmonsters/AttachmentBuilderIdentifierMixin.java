package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.puzzleslib.fabric.impl.attachment.builder.FabricDataAttachmentBuilder;
import info.mudbourn.mmscompat.duck.AttachmentIdentifierDuck;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records the identifier a puzzleslib attachment builder is building under, so
 * {@link CreeperMinionShoulderSyncMixin} can target a single attachment.
 *
 * <p>Purely bookkeeping — it changes no behaviour on its own, and the field it
 * writes is read by nothing else.
 */
@Mixin(FabricDataAttachmentBuilder.class)
public abstract class AttachmentBuilderIdentifierMixin implements AttachmentIdentifierDuck {

    @Unique
    @Nullable
    private Identifier mmsCompat$attachmentId;

    @Override
    @Nullable
    public Identifier mmsCompat$attachmentId() {
        return this.mmsCompat$attachmentId;
    }

    @Override
    public void mmsCompat$setAttachmentId(Identifier identifier) {
        this.mmsCompat$attachmentId = identifier;
    }

    @Inject(method = "build", at = @At("HEAD"))
    private void mmsCompat$rememberIdentifier(Identifier identifier, CallbackInfoReturnable<?> cir) {
        this.mmsCompat$setAttachmentId(identifier);
    }
}
