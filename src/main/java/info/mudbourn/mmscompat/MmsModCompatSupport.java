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

        // Thrown-weapon projectile. Registered unconditionally: the entity type
        // has to exist on both sides regardless of which weapon mods are present,
        // or a saved projectile comes back as an unknown entity.
        info.mudbourn.mmscompat.throwable.MmsThrowables.register();

        // Anchor/tuna swing cooldown, tuna knockback, glaive blocking
        MmsSounds.register();
        WeaponTuning.register();

        // Metro line-name sync (ModMetro never sends lineName to clients)
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmetro")) {
            info.mudbourn.mmscompat.metro.MetroLineSyncServer.register();
            info.mudbourn.mmscompat.metro.MetroTrainDespawn.register();
            // Cruise-zone governor settings (config/mms_compat_metro.json)
            info.mudbourn.mmscompat.metro.MetroTuning.load();
        }

        // /vanity command (permission level 0 — public)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerVanityCommand(dispatcher);
            registerMetroAccelCommand(dispatcher);
            // /mmsjob debug wrappers (op only) — self-targeted Jobs+ test harness
            if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("jobsplus")) {
                info.mudbourn.mmscompat.jobsplus.JobsDebugCommand.register(dispatcher);
            }
        });

        // Drop Jobs+ XP cooldown state when a player leaves
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            JobsPlusActionCooldown.forget(handler.getPlayer().getUUID()));

        // Xaero's World Map chunk sync (server-side packet handler)

        LOG.info("MMS Mod Compat Support v0.6.6 loaded — creative-tab dedup, REI null-filter fix, AR item-drop suppression, creative embargo, /vanity, Jobs+ XP cooldown, warrior job, Xaero chunk sync.");
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
            .then(Commands.literal("kits")
                .executes(ctx -> {
                    String names = String.join("§7, §e", VanityKits.KITS.keySet());
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("§7Vanity kits: §e" + names), false);
                    return 1;
                })
            )
            .then(Commands.literal("kit")
                .then(Commands.argument("kit", com.mojang.brigadier.arguments.StringArgumentType.word())
                    .suggests((ctx, builder) ->
                        net.minecraft.commands.SharedSuggestionProvider.suggest(VanityKits.KITS.keySet(), builder))
                    .executes(ctx -> {
                        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                            ctx.getSource().sendFailure(Component.literal("§cPlayers only."));
                            return 0;
                        }
                        String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "kit");
                        VanityKits.Kit kit = VanityKits.KITS.get(name);
                        if (kit == null) {
                            ctx.getSource().sendFailure(Component.literal(
                                "§cNo such vanity kit: §e" + name + "§c — try §e/vanity kits"));
                            return 0;
                        }

                        int given = VanityKits.give(player, kit);
                        if (given == 0) {
                            ctx.getSource().sendFailure(Component.literal(
                                "§cKit §e" + name + "§c has no items available in this modset."));
                            return 0;
                        }
                        final int count = given;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                            "§7Gave §e" + count + "§7 vanity piece(s) — §e" + kit.display()), false);
                        return count;
                    })
                )
            )
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

    private void registerMetroAccelCommand(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("metroaccel")
            .requires(src -> src.permissions().hasPermission(
                new net.minecraft.server.permissions.Permission.HasCommandLevel(
                    net.minecraft.server.permissions.PermissionLevel.byId(2))))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() ->
                    Component.literal(String.format(
                        "\u00a77Metro acceleration factor: \u00a7e%.4f \u00a77(default 1.1)",
                        info.mudbourn.mmscompat.metro.MetroTuning.acceleration_factor)), false);
                return 1;
            })
            .then(Commands.literal("set")
                .then(Commands.argument("value",
                        com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(1.0, 1.5))
                    .executes(ctx -> {
                        double val = com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx, "value");
                        info.mudbourn.mmscompat.metro.MetroTuning.acceleration_factor = val;
                        info.mudbourn.mmscompat.metro.MetroTuning.save();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal(String.format(
                                "\u00a7aMetro acceleration factor set to \u00a7e%.4f", val)), true);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("reset")
                .executes(ctx -> {
                    info.mudbourn.mmscompat.metro.MetroTuning.acceleration_factor = 1.1;
                    info.mudbourn.mmscompat.metro.MetroTuning.save();
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("\u00a7aMetro acceleration factor reset to default (1.1)"), true);
                    return 1;
                })
            )
        );
    }
}
