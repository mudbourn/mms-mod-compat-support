package info.mudbourn.mmscompat.mixin.trinkets;

import dev.emi.trinkets.api.LivingEntityTrinketComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Gives Trinkets' full sync packet registry access. Without it any trinket
 * with a registry-bound component, such as enchantments, fails to encode and
 * the whole payload arrives empty, so players who start tracking someone see
 * no trinkets or cosmetic armor until a slot changes.
 */
@Mixin(value = LivingEntityTrinketComponent.class, remap = false)
public class TrinketsFullSyncRegistryMixin {

    @Redirect(
        method = "writeSyncPacket",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/storage/TagValueOutput;createWithoutContext(Lnet/minecraft/util/ProblemReporter;)Lnet/minecraft/world/level/storage/TagValueOutput;",
            remap = true
        )
    )
    private TagValueOutput mms_compat$withRegistries(
            ProblemReporter reporter,
            RegistryFriendlyByteBuf buf,
            ServerPlayer recipient) {
        return TagValueOutput.createWithContext(reporter, buf.registryAccess());
    }
}
