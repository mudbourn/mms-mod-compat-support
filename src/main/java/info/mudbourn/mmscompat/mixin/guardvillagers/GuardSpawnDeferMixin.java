package info.mudbourn.mmscompat.mixin.guardvillagers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Adds the guard spawned beside a newly loaded villager on the next task, after the chunk finishes tracking its entities.
@Pseudo
@Mixin(targets = "dev.sterner.guardvillagers.GuardVillagers")
public abstract class GuardSpawnDeferMixin {

    @WrapOperation(
        method = "lambda$onInitialize$1",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"
        )
    )
    private static void mmsCompat$deferGuardSpawn(ServerLevel level, Entity guard, Operation<Void> original) {
        MinecraftServer server = level.getServer();
        server.schedule(new TickTask(server.getTickCount(), () -> original.call(level, guard)));
    }
}
