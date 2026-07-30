package info.mudbourn.mmscompat.mixin.mutantmonsters;

import fuzs.puzzleslib.impl.attachment.builder.DataAttachmentBuilder;
import info.mudbourn.mmscompat.duck.AttachmentIdentifierDuck;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes creeper minions visible when they ride a player's shoulder.
 *
 * <p>Mutant Monsters tracks shoulder minions in two synced data attachments,
 * which its render layer reads to decide whether to draw anything. On NeoForge
 * they are published with {@code PlayerSet::nearEntity}; the Fabric branch
 * instead passes
 *
 * <pre>{@code entity -> Function.identity()::apply}</pre>
 *
 * <p>which builds a {@code PlayerSet} whose {@code apply} hands the consumer to
 * {@code Function.identity()} and throws the result away without ever invoking
 * it. No player is ever added, so the attachment reaches nobody, the render
 * layer always sees {@code Optional.empty()}, and the minion is invisible to
 * everyone including its owner. Vanilla's shoulder-entity NBT is server-only —
 * that is why the mod needs the attachment at all — so there is nothing on the
 * client to fall back to.
 *
 * <p>The fix lands on the single predicate puzzleslib derives from that
 * {@code PlayerSet}. Fabric's attachment API only consults it for players
 * already tracking the holder, so forcing it true for these two attachments is
 * exactly the {@code nearEntity} behaviour NeoForge gets, and it reuses the
 * mod's own stream codec and packets rather than adding a channel.
 *
 * <p>Scoped by attachment identifier via {@link AttachmentBuilderIdentifierMixin},
 * because {@code DataAttachmentBuilder} is shared by every puzzleslib mod and
 * a blanket override would change sync for all of them.
 */
@Mixin(DataAttachmentBuilder.class)
public abstract class CreeperMinionShoulderSyncMixin {

    @Unique
    private static final Identifier LEFT_SHOULDER =
            Identifier.fromNamespaceAndPath("mutantmonsters", "left_shoulder_creeper_minion");

    @Unique
    private static final Identifier RIGHT_SHOULDER =
            Identifier.fromNamespaceAndPath("mutantmonsters", "right_shoulder_creeper_minion");

    @Inject(method = "syncWith", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$syncShoulderMinionToTrackers(Object holder, ServerPlayer serverPlayer,
                                                        CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof AttachmentIdentifierDuck duck)) {
            return;
        }
        Identifier identifier = duck.mmsCompat$attachmentId();
        if (LEFT_SHOULDER.equals(identifier) || RIGHT_SHOULDER.equals(identifier)) {
            cir.setReturnValue(true);
        }
    }
}
