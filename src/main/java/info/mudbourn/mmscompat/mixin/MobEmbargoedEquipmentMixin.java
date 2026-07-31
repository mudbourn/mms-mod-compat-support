package info.mudbourn.mmscompat.mixin;

import info.mudbourn.mmscompat.CreativeEmbargo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps embargoed weapon-duplicates out of mob hands.
 *
 * <p>The embargo has three layers — creative menu ({@link CreativeEmbargo}),
 * recipe viewers (the {@code #c:hidden_from_recipe_viewers} tag) and DisableMod
 * (crafting, trades, player inventory sweep) — and all three assume the item
 * reaches a player through an item pipeline. Mob spawn equipment is not a
 * pipeline: Basic Weapons' {@code MonsterMixin} hardcodes iron club/dagger/hammer
 * onto zombies and the golden set onto piglins, with no config and no loot table
 * to override, and Weapons Expanded swaps zombie swords the same way. The
 * result was zombies visibly wielding {@code basicweapons:iron_hammer} — an
 * embargoed item — which is also how one ends up on the ground.
 *
 * <p>Cancelling at HEAD means the mob simply spawns with that slot empty. This
 * covers any mod that equips mobs this way, and it lives in the ungated config
 * for the same reason.
 *
 * <p>{@code setItemSlot} is declared on {@link LivingEntity} (and the
 * {@code EquipmentHolder} interface), not on {@link Mob} — as of 1.21.11 Mob no
 * longer overrides it, so a {@code @Mixin(Mob.class)} injection fails to find
 * any target and hard-crashes at bootstrap. Target LivingEntity and narrow to
 * mobs with an instanceof guard so players are untouched.
 *
 * <p>Note this also declines embargoed items a mob tries to pick up, which is
 * the same intent. Mobs already spawned holding an embargoed weapon keep it
 * until they die — this is a spawn-side fix, not a retroactive sweep.
 */
@Mixin(LivingEntity.class)
public abstract class MobEmbargoedEquipmentMixin {

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$refuseEmbargoedEquipment(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if ((Object) this instanceof Mob && !stack.isEmpty() && CreativeEmbargo.isEmbargoed(stack)) {
            ci.cancel();
        }
    }
}
