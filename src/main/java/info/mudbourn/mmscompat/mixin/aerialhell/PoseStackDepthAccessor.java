package info.mudbourn.mmscompat.mixin.aerialhell;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PoseStack.class)
public interface PoseStackDepthAccessor {
    @Accessor("lastIndex")
    int mmsCompat$depth();
}
