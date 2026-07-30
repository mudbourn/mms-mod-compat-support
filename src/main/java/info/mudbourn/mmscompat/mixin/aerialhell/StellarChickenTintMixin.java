package info.mudbourn.mmscompat.mixin.aerialhell;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes an unconditional client crash on Stellar Chicken spawn.
 *
 * StellarChickenEntity.tick() colours a freshly spawned chicken from the grass
 * colour under it, via
 *
 *     level.getBlockTint(blockPosition(), Biome::getGrassColor)
 *
 * That method reference is a fresh ColorResolver instance, not the vanilla
 * BiomeColors.GRASS_COLOR_RESOLVER singleton. ClientLevel keeps a per-resolver
 * BlockTintCache and Fabric Rendering API v1 hard-throws
 * UnsupportedOperationException on any resolver it has never seen, so the first
 * tick of any uncoloured Stellar Chicken kills the client. The server has no
 * tint cache, so it only ever bites client-side — hence a client-only mixin,
 * leaving the mod's own code to run untouched on the server.
 *
 * Both resolvers bottom out in Biome.getGrassColor(double, double), so routing
 * through the registered vanilla singleton is value-identical.
 */
@Mixin(targets = "fr.factionbedrock.aerialhell.Entity.Passive.StellarChickenEntity", remap = false)
public abstract class StellarChickenTintMixin {

    @Inject(method = "getBlockPositionTint()I", at = @At("HEAD"), cancellable = true)
    private void mms$useRegisteredGrassResolver(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ClientLevel level) {
            cir.setReturnValue(BiomeColors.getAverageGrassColor(level, self.blockPosition()));
        }
    }
}
