package info.mudbourn.mmscompat.bubble;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Registered content of the Bubblellaneous port. */
public final class BubbleContent {

    public static final String NAMESPACE = "mms_compat";

    public static final DataComponentType<CameraLink> CAMERA_LINK = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("camera_link"),
        DataComponentType.<CameraLink>builder()
            .persistent(CameraLink.CODEC)
            .networkSynchronized(CameraLink.STREAM_CODEC)
            .build()
    );

    public static final Block ALARM_BLOCK = block(
        "alarm_block",
        AlarmBlock::new,
        base().lightLevel(state -> state.getValue(AlarmBlock.POWERED) ? 15 : 0)
    );

    public static final Block CALL_BELL = block("call_bell", CallBellBlock::new, base().noOcclusion());

    public static final Block CLIPBOARD = block(
        "clipboard",
        ClipboardBlock::new,
        base().noOcclusion().sound(SoundType.WOOL)
    );

    public static final Block COMPUTER = block("computer", ComputerBlock::new, base().noOcclusion());

    public static final Block SECURITY_MONITOR = block(
        "security_monitor",
        SecurityMonitorBlock::new,
        base().noOcclusion()
    );

    public static final Block SURVEILLANCE_CAMERA = block(
        "surveillance_camera",
        SurveillanceCameraBlock::new,
        base().noOcclusion()
    );

    public static final Block IRON_RISING_DOOR = block(
        "iron_rising_door",
        RisingDoorBlock::new,
        base().noOcclusion().strength(5.0f, 6.0f)
    );

    public static final Item ALARM_BLOCK_ITEM = item(ALARM_BLOCK, "alarm_block", BlockItem::new);
    public static final Item CALL_BELL_ITEM = item(CALL_BELL, "call_bell", BlockItem::new);
    public static final Item CLIPBOARD_ITEM = item(CLIPBOARD, "clipboard", BlockItem::new);
    public static final Item COMPUTER_ITEM = item(COMPUTER, "computer", BlockItem::new);
    public static final Item SECURITY_MONITOR_ITEM = item(
        SECURITY_MONITOR,
        "security_monitor",
        SecurityMonitorItem::new
    );
    public static final Item SURVEILLANCE_CAMERA_ITEM = item(
        SURVEILLANCE_CAMERA,
        "surveillance_camera",
        BlockItem::new
    );
    public static final Item IRON_RISING_DOOR_ITEM = item(IRON_RISING_DOOR, "iron_rising_door", BlockItem::new);

    public static final BlockEntityType<SecurityMonitorBlockEntity> MONITOR_ENTITY = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        id("security_monitor"),
        FabricBlockEntityTypeBuilder.create(SecurityMonitorBlockEntity::new, SECURITY_MONITOR).build()
    );

    public static final BlockEntityType<SurveillanceCameraBlockEntity> CAMERA_ENTITY = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        id("surveillance_camera"),
        FabricBlockEntityTypeBuilder.create(SurveillanceCameraBlockEntity::new, SURVEILLANCE_CAMERA).build()
    );

    public static final BlockEntityType<RisingDoorBlockEntity> RISING_DOOR_ENTITY = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        id("iron_rising_door"),
        FabricBlockEntityTypeBuilder.create(RisingDoorBlockEntity::new, IRON_RISING_DOOR).build()
    );

    public static final EntityType<CameraViewEntity> CAMERA_VIEW = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        id("camera_view"),
        EntityType.Builder.<CameraViewEntity>of(CameraViewEntity::new, MobCategory.MISC)
            .sized(0.1f, 0.1f)
            .noSave()
            .noSummon()
            .clientTrackingRange(8)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, id("camera_view")))
    );

    private static final Map<String, Item> REPLACED_LOOT = Map.of(
        "alarm_block", ALARM_BLOCK_ITEM,
        "call_bell", CALL_BELL_ITEM,
        "clipboard", CLIPBOARD_ITEM,
        "computer", COMPUTER_ITEM,
        "security_monitor", SECURITY_MONITOR_ITEM,
        "surveillance_camera", SURVEILLANCE_CAMERA_ITEM,
        "iron_rising_door", IRON_RISING_DOOR_ITEM
    );

    private BubbleContent() {}

    public static void register() {
        CameraView.register();
        BubbleMigration.register();
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> RisingDoorBlock.backfill(chunk));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
            REPLACED_LOOT.values().forEach(entries::accept)
        );
        LootTableEvents.REPLACE.register((key, original, source, registries) -> {
            Identifier table = key.identifier();
            if (!table.getNamespace().equals("bubblellaneous") || !table.getPath().startsWith("block/")) {
                return null;
            }
            Item item = REPLACED_LOOT.get(table.getPath().substring("block/".length()));
            if (item == null) {
                return null;
            }
            return LootTable.lootTable()
                .setParamSet(LootContextParamSets.COMMAND)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(item)))
                .build();
        });
    }

    public static Item itemFor(String name) {
        return REPLACED_LOOT.get(name);
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, "bubble_" + name);
    }

    private static BlockBehaviour.Properties base() {
        return BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.METAL);
    }

    private static Block block(
        String name,
        Function<BlockBehaviour.Properties, Block> factory,
        BlockBehaviour.Properties properties
    ) {
        Identifier id = id(name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return Registry.register(
            BuiltInRegistries.BLOCK,
            key,
            factory.apply(properties.setId(key).overrideDescription("block.bubblellaneous." + name))
        );
    }

    private static Item item(Block block, String name, BiFunction<Block, Item.Properties, Item> factory) {
        Identifier id = id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(
            BuiltInRegistries.ITEM,
            key,
            factory.apply(block, new Item.Properties().setId(key).overrideDescription("block.bubblellaneous." + name))
        );
    }
}
