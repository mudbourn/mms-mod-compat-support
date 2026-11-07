package info.mudbourn.mmscompat.bubble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds the camera a security monitor is linked to. */
public class SecurityMonitorBlockEntity extends BlockEntity {

    private CameraLink link;

    public SecurityMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(BubbleContent.MONITOR_ENTITY, pos, state);
    }

    public CameraLink getLink() {
        return link;
    }

    public void setLink(CameraLink link) {
        this.link = link;
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        link = input.read("link", CameraLink.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (link != null) {
            output.store("link", CameraLink.CODEC, link);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        link = components.get(BubbleContent.CAMERA_LINK);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (link != null) {
            builder.set(BubbleContent.CAMERA_LINK, link);
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("link");
    }
}
