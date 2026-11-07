package info.mudbourn.mmscompat.bubble;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** Monitor item that names the camera it is linked to. */
public class SecurityMonitorItem extends BlockItem {

    public SecurityMonitorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay display,
        Consumer<Component> lines,
        TooltipFlag flag
    ) {
        CameraLink link = stack.get(BubbleContent.CAMERA_LINK);
        if (link != null) {
            lines.accept(link.name().copy().withStyle(ChatFormatting.GRAY));
        }
    }
}
