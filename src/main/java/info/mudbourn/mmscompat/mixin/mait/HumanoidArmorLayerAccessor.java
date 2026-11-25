package info.mudbourn.mmscompat.mixin.mait;

import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor {

    @Mutable
    @Accessor("babyModelSet")
    @SuppressWarnings("rawtypes")
    void mms_compat$setBabyModelSet(ArmorModelSet set);
}
