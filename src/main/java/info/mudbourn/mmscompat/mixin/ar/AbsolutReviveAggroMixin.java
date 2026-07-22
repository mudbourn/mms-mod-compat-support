package info.mudbourn.mmscompat.mixin.ar;

import goetic.mods.absolutrevive.common.EventHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * AbsolutRevive mixin — makes hostiles lose interest in a downed player.
 *
 * AbsolutRevive's only aggro protection is a ModifyVariable on Mob.setTarget
 * that nulls an unconscious player passed as the new target. That misses three
 * cases, which this sweep covers each tick for every mob within RADIUS of a
 * downed player:
 *
 *   1. A goal target set BEFORE the player went down (setTarget is never called
 *      again, so the mod's hook never fires and the mob keeps attacking).
 *   2. Brain-based acquisition (piglins, hoglins, warden, …) which write
 *      ATTACK_TARGET / ANGRY_AT memory directly, bypassing setTarget entirely.
 *   3. NeutralMob persistent anger (endermen), which survives the whole
 *      down/revive cycle and re-fires setTarget on revive — the mob never
 *      actually forgot the player. Clearing it here, while they are down, is
 *      what stops the re-aggro after they get back up.
 *
 * Deliberately NOT invulnerability: a downed player still takes damage (the mod
 * already allows entity-sourced damage while unconscious). Bosses/minibosses
 * tagged #mms_compat:revive_ignores_downed are exempt and keep hunting, so a
 * stray AoE or an angry Warden can still finish someone off.
 *
 * Only anger directed at THIS downed player is cleared — a mob mid-fight with
 * a healthy player nearby is left alone.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveAggroMixin {

    private static final TagKey<EntityType<?>> IGNORES_DOWNED = TagKey.create(
        Registries.ENTITY_TYPE,
        Identifier.fromNamespaceAndPath("mms_compat", "revive_ignores_downed"));

    private static final double RADIUS = 32.0;

    @Inject(method = "tickPlayers", at = @At("TAIL"))
    private static void mmsCompat$suppressAggroAroundDowned(ServerLevel level, CallbackInfo ci) {
        for (ServerPlayer downed : level.players()) {
            if (!EventHandler.isUnconscious(downed)) {
                continue;
            }
            UUID downedId = downed.getUUID();
            AABB box = downed.getBoundingBox().inflate(RADIUS);

            for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
                if (mob.getType().is(IGNORES_DOWNED)) {
                    continue;
                }

                // 1. Goal target locked in before the player went down.
                if (mob.getTarget() == downed) {
                    mob.setTarget(null);
                }

                // 2. Brain-based acquisition.
                Brain<?> brain = mob.getBrain();
                if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                        && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == downed) {
                    brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
                if (brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)
                        && downedId.equals(brain.getMemory(MemoryModuleType.ANGRY_AT).orElse(null))) {
                    brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                }

                // 3. Persistent anger (endermen re-aggro on revive).
                if (mob instanceof NeutralMob neutral && downedId.equals(neutral.getPersistentAngerTarget())) {
                    neutral.stopBeingAngry();
                }
            }
        }
    }
}
