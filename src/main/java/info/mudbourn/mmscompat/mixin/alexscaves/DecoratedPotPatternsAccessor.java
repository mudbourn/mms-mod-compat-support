package info.mudbourn.mmscompat.mixin.alexscaves;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Reads and replaces the vanilla sherd-to-pattern map.
 */
@Mixin(DecoratedPotPatterns.class)
public interface DecoratedPotPatternsAccessor {

    @Accessor("ITEM_TO_POT_TEXTURE")
    static Map<Item, ResourceKey<DecoratedPotPattern>> mms$itemToPotTexture() {
        throw new AssertionError();
    }

    @Mutable
    @Accessor("ITEM_TO_POT_TEXTURE")
    static void mms$setItemToPotTexture(Map<Item, ResourceKey<DecoratedPotPattern>> map) {
        throw new AssertionError();
    }
}
