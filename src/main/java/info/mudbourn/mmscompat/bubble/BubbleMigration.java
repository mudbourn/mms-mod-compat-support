package info.mudbourn.mmscompat.bubble;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/** Converts placed datapack blocks and held items of the ported types into the real ones. */
public final class BubbleMigration {

    private static final String PACK = "bubblellaneous";

    private static final int PER_TICK = 64;

    private static final int PLAYER_SCAN_TICKS = 200;

    private static final Queue<Display.ItemDisplay> QUEUE = new ArrayDeque<>();
    // Containers are converted on the next tick, never inside CHUNK_LOAD: any write marks
    // the chunk dirty through a blocking getChunk on the chunk still loading, which deadlocks.
    private static final Queue<BlockEntity> CONTAINER_QUEUE = new ArrayDeque<>();

    private BubbleMigration() {}

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof Display.ItemDisplay display && display.getTags().contains("bbln.block")) {
                QUEUE.add(display);
            } else if (entity instanceof ItemEntity item) {
                item.setItem(convert(item.getItem()));
            }
        });
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> queueContainers(chunk));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> convertPlayer(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(BubbleMigration::tick);
    }

    private static void tick(MinecraftServer server) {
        while (!CONTAINER_QUEUE.isEmpty()) {
            BlockEntity entity = CONTAINER_QUEUE.poll();
            if (
                !entity.isRemoved()
                    && entity.getLevel() instanceof ServerLevel level
                    && level.isLoaded(entity.getBlockPos())
                    // An unrolled loot table would roll on getItem; leave it to the first opener.
                    && !(entity instanceof RandomizableContainer loot && loot.getLootTable() != null)
                    && entity instanceof Container container
            ) {
                convertContainer(container);
            }
        }
        for (int i = 0; i < PER_TICK && !QUEUE.isEmpty(); i++) {
            Display.ItemDisplay display = QUEUE.poll();
            if (
                !display.isRemoved()
                    && display.level() instanceof ServerLevel level
                    && level.isLoaded(display.blockPosition())
            ) {
                migrate(level, display);
            }
        }
        if (server.getTickCount() % PLAYER_SCAN_TICKS == 0) {
            server.getPlayerList().getPlayers().forEach(BubbleMigration::convertPlayer);
        }
    }

    private static void convertPlayer(ServerPlayer player) {
        convertContainer(player.getInventory());
        convertContainer(player.getEnderChestInventory());
    }

    private static void queueContainers(LevelChunk chunk) {
        for (BlockEntity entity : chunk.getBlockEntities().values()) {
            if (entity instanceof Container) {
                CONTAINER_QUEUE.add(entity);
            }
        }
    }

    private static void convertContainer(Container container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            ItemStack converted = convert(stack);
            if (converted != stack) {
                container.setItem(slot, converted);
            }
        }
    }

    /** Returns the real item for a datapack item frame stack, or the stack itself. */
    public static ItemStack convert(ItemStack stack) {
        if (!stack.is(Items.ITEM_FRAME)) {
            return stack;
        }
        Identifier model = stack.get(DataComponents.ITEM_MODEL);
        if (model == null || !PACK.equals(model.getNamespace())) {
            return stack;
        }
        Item target = BubbleContent.itemFor(model.getPath());
        if (target == null) {
            return stack;
        }
        ItemStack result = new ItemStack(target, stack.getCount());
        Component custom = stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            result.set(DataComponents.CUSTOM_NAME, custom);
        }
        if (target == BubbleContent.SECURITY_MONITOR_ITEM && stack.get(DataComponents.ENTITY_DATA) != null) {
            CompoundTag entityData = stack.get(DataComponents.ENTITY_DATA).copyTagWithoutId();
            CompoundTag properties = path(
                entityData,
                "Item",
                "components",
                "minecraft:custom_data",
                PACK,
                "block_properties"
            );
            CameraLink link = readLink(properties);
            if (link != null) {
                result.set(BubbleContent.CAMERA_LINK, link);
            }
        }
        return result;
    }

    private static void migrate(ServerLevel level, Display.ItemDisplay display) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        display.saveWithoutId(output);
        CompoundTag tag = output.buildResult();
        CompoundTag data = path(tag, "item", "components", "minecraft:custom_data", PACK);
        String id = path(data, "block_data").getStringOr("id", "");
        BlockPos pos = display.blockPosition();
        Direction facing = Direction.fromYRot(display.getYRot());
        BlockState state = stateFor(level, display, id, pos, facing, data);
        if (state == null) {
            return;
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof SecurityMonitorBlockEntity monitor) {
            monitor.setLink(readLink(path(data, "block_properties")));
        }
        removeChildren(level, display);
        display.discard();
    }

    private static BlockState stateFor(
        ServerLevel level,
        Display.ItemDisplay display,
        String id,
        BlockPos pos,
        Direction facing,
        CompoundTag data
    ) {
        BlockState existing = level.getBlockState(pos);
        return switch (id) {
            case "alarm_block" -> BubbleContent.ALARM_BLOCK.defaultBlockState()
                .setValue(
                    AlarmBlock.POWERED,
                    existing.is(Blocks.REDSTONE_LAMP) && existing.getValue(RedstoneLampBlock.LIT)
                );
            case "call_bell" -> BubbleContent.CALL_BELL.defaultBlockState();
            case "clipboard" -> BubbleContent.CLIPBOARD.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing);
            case "computer" -> BubbleContent.COMPUTER.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(ComputerBlock.STATE, computerState(data));
            case "security_monitor" -> BubbleContent.SECURITY_MONITOR.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(SecurityMonitorBlock.LINKED, readLink(path(data, "block_properties")) != null);
            case "surveillance_camera" -> BubbleContent.SURVEILLANCE_CAMERA.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing);
            case "iron_rising_door" -> doorState(level, display, pos, data);
            default -> null;
        };
    }

    private static ComputerState computerState(CompoundTag data) {
        String state = path(data, "block_properties").getStringOr("block_state", "default");
        return state.equals("default") ? ComputerState.DEFAULT : ComputerState.LOGIN;
    }

    private static BlockState doorState(
        ServerLevel level,
        Display.ItemDisplay display,
        BlockPos pos,
        CompoundTag data
    ) {
        int rotation = path(data, "block_properties").getIntOr("rotation", 0);
        boolean alongZ = rotation == 90 || rotation == -90;
        return BubbleContent.IRON_RISING_DOOR.defaultBlockState()
            .setValue(RisingDoorBlock.AXIS, alongZ ? Direction.Axis.Z : Direction.Axis.X)
            .setValue(RisingDoorBlock.OPEN, score(level, display, "bbln.block_state") == 1)
            .setValue(RisingDoorBlock.ABOVE, level.getBlockState(pos.above()).is(BubbleContent.IRON_RISING_DOOR));
    }

    private static int score(ServerLevel level, Entity entity, String objectiveName) {
        Scoreboard scoreboard = level.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return 0;
        }
        ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(entity, objective);
        return info == null ? 0 : info.value();
    }

    private static void removeChildren(ServerLevel level, Display.ItemDisplay display) {
        Scoreboard scoreboard = level.getScoreboard();
        Objective idObjective = scoreboard.getObjective("bbln.block.id");
        if (idObjective == null) {
            return;
        }
        int id = score(level, display, "bbln.block.id");
        AABB area = new AABB(display.blockPosition()).inflate(1.5);
        List<Entity> nearby = level.getEntities(
            (Entity) null,
            area,
            entity -> entity != display && entity.getTags().contains("+bubblellaneous")
        );
        for (Entity entity : nearby) {
            if (
                scoreboard.getPlayerScoreInfo(entity, idObjective) != null
                    && score(level, entity, "bbln.block.id") == id
            ) {
                entity.discard();
            }
        }
    }

    private static CameraLink readLink(CompoundTag properties) {
        CompoundTag camera = path(properties, "camera");
        CompoundTag location = path(camera, "location");
        Optional<String> dimension = location.getString("dimension");
        if (dimension.isEmpty() || !location.contains("x")) {
            return null;
        }
        Component name = Component.translatable("item.bubblellaneous.default_camera");
        Tag nameTag = camera.get("name");
        if (nameTag != null) {
            name = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, nameTag).result().orElse(name);
        }
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension.get()));
        BlockPos pos = new BlockPos(location.getIntOr("x", 0), location.getIntOr("y", 0), location.getIntOr("z", 0));
        return new CameraLink(GlobalPos.of(key, pos), name, true);
    }

    private static CompoundTag path(CompoundTag root, String... keys) {
        CompoundTag current = root;
        for (String key : keys) {
            current = current.getCompoundOrEmpty(key);
        }
        return current;
    }
}
