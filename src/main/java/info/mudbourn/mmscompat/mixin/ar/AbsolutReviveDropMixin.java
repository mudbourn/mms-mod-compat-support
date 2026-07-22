package info.mudbourn.mmscompat.mixin.ar;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * AbsolutRevive mixin \u2014 prevents dropping ALL items on down.
 *
 * Both the KO moment (handleFatalPlayerDamage) and the per-tick disarm
 * (tickPlayers) are fully suppressed. Items stay in hand.
 *
 * CRITICAL: handleFatalPlayerDamage calls stack.shrink(1) BEFORE
 * setItemInHand/drop. We inject BEFORE shrink() to snapshot the item,
 * then restore it in the drop redirect so it survives.
 */
@Mixin(targets = "goetic.mods.absolutrevive.common.EventHandler")
public class AbsolutReviveDropMixin {

    // Snapshot of the player's hand items BEFORE shrink() destroys them.
    private static ItemStack mmsCompat$savedMainhandFatal = ItemStack.EMPTY;
    private static ItemStack mmsCompat$savedOffhandFatal = ItemStack.EMPTY;

    // ===== handleFatalPlayerDamage \u2014 KO moment =====

    @Inject(
        method = "handleFatalPlayerDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
        )
    )
    private static void mmsCompat$snapshotBeforeShrink(Player player, CallbackInfoReturnable<Boolean> cir) {
        mmsCompat$savedMainhandFatal = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
        mmsCompat$savedOffhandFatal = player.getItemInHand(InteractionHand.OFF_HAND).copy();
    }

    @Redirect(
        method = "handleFatalPlayerDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 0
        )
    )
    private static void mmsCompat$safeSetItemInHandFatal0(Player player, InteractionHand hand, ItemStack stack) {
        // Don't clear hand \u2014 keep item
    }

    @Redirect(
        method = "handleFatalPlayerDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 1
        )
    )
    private static void mmsCompat$safeSetItemInHandFatal1(Player player, InteractionHand hand, ItemStack stack) {
        // Don't clear hand \u2014 keep item
    }

    @Redirect(
        method = "handleFatalPlayerDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 0
        )
    )
    private static ItemEntity mmsCompat$safeDropFatal0(Player player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, mmsCompat$savedMainhandFatal);
        }
        return null;
    }

    @Redirect(
        method = "handleFatalPlayerDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 1
        )
    )
    private static ItemEntity mmsCompat$safeDropFatal1(Player player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        if (player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, mmsCompat$savedOffhandFatal);
        }
        return null;
    }

    // ===== tickPlayers \u2014 per-tick disarm while unconscious =====
    // Suppress ALL setItemInHand(EMPTY) and drop() calls unconditionally.

    @Redirect(
        method = "tickPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 0
        )
    )
    private static void mmsCompat$keepItemInMainHand(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        // Don't clear \u2014 keep item in hand
    }

    @Redirect(
        method = "tickPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 0
        )
    )
    private static ItemEntity mmsCompat$suppressDropMainhandTick(ServerPlayer player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        return null;
    }

    @Redirect(
        method = "tickPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 1
        )
    )
    private static void mmsCompat$keepItemInOffhand(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        // Don't clear \u2014 keep item in hand
    }

    @Redirect(
        method = "tickPlayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            ordinal = 1
        )
    )
    private static ItemEntity mmsCompat$suppressDropOffhandTick(ServerPlayer player, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        return null;
    }

    // ===== ar_downed tag sync =====

    @Inject(
        method = "tickPlayers",
        at = @At("TAIL")
    )
    private static void mmsCompat$syncArDownedTag(ServerLevel level, CallbackInfo ci) {
        for (ServerPlayer player : level.players()) {
            if (goetic.mods.absolutrevive.common.EventHandler.isUnconscious(player)) {
                player.addTag("ar_downed");
            } else {
                player.removeTag("ar_downed");
            }
        }
    }
}
