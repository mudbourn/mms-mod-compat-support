package info.mudbourn.mmscompat.mixin.mait;

import java.util.List;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {

    @Accessor("layers")
    @SuppressWarnings("rawtypes")
    List<RenderLayer> mms_compat$getLayers();
}
