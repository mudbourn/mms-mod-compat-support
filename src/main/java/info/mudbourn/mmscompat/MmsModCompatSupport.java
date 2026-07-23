package info.mudbourn.mmscompat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MmsModCompatSupport implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("mms_compat");

    @Override
    public void onInitialize() {
        // Creative-menu embargo strip
        CreativeEmbargo.register();
        info.mudbourn.mmscompat.waypoint.SharedWaypointServer.register();

        // Metro line-name sync (ModMetro never sends lineName to clients)
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmetro")) {
            info.mudbourn.mmscompat.metro.MetroLineSyncServer.register();
            info.mudbourn.mmscompat.metro.MetroTrainDespawn.register();
        }

        // /vanity command (permission level 0 \u2014 public)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerVanityCommand(dispatcher);
        });

        // Drop Jobs+ XP cooldown state when a player leaves
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            JobsPlusActionCooldown.forget(handler.getPlayer().getUUID()));

        // Xaero's World Map chunk sync (server-side packet handler)

        LOG.info("MMS Mod Compat Support v0.6.5 loaded — creative-tab dedup, REI null-filter fix, AR item-drop suppression, creative embargo, /vanity, Jobs+ XP cooldown, warrior job, Xaero chunk sync.");
    }

    private void registerVanityCommand(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vanity")
            .executes(ctx -> {
                if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cPlayers only."));
                    return 0;
                }
                ItemStack stack = player.getMainHandItem();
                if (stack.isEmpty()) {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cHold an item in your main hand."));
                    return 0;
                }
                // Check for streak/artifact items (inline to avoid cross-mod dependency)
                if (isStreakItem(stack)) {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cCannot vanity a streak item."));
                    return 0;
                }
                // Validate the item is equippable armor (has EQUIPPABLE component with an armor slot)
                Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                if (equippable == null || !isArmorSlot(equippable.slot())) {
                    ctx.getSource().sendFailure(Component.literal("\u00a7cOnly armor can be made vanity."));
                    return 0;
                }

                VanityUtil.applyVanity(stack);
                ctx.getSource().sendSuccess(() ->
                    Component.literal("\u00a77Item made \u00a7evanity\u00a77 (cosmetic, no stats, no enchants, unbreakable)"), false);
                return 1;
            })
        );
    }

    private static boolean isStreakItem(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return false;
        CompoundTag tag = customData.copyTag();
        return tag.contains("streak_item") || tag.contains("artifact");
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
            || slot == EquipmentSlot.CHEST
            || slot == EquipmentSlot.LEGS
            || slot == EquipmentSlot.FEET;
    }
}
