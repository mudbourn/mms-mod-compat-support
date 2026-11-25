package info.mudbourn.mmscompat.mixin.mait;

import info.mudbourn.mmscompat.client.mait.BabySkeletonArmor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Gives skeleton armor layers a baby armor set shaped like the skeleton's limbs.
@Mixin(AbstractSkeletonRenderer.class)
@SuppressWarnings("rawtypes")
public abstract class AbstractSkeletonRendererMixin {

    @Inject(
        method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/renderer/entity/ArmorModelSet;Lnet/minecraft/client/model/monster/skeleton/SkeletonModel;)V",
        at = @At("RETURN")
    )
    private void mms_compat$addBabyArmor(
        EntityRendererProvider.Context context, ArmorModelSet<ModelLayerLocation> armorSet, SkeletonModel<?> bodyModel, CallbackInfo ci
    ) {
        BabySkeletonArmor.Kind kind = BabySkeletonArmor.forArmor(armorSet);
        if (kind == null) {
            return;
        }
        for (RenderLayer layer : ((LivingEntityRendererAccessor) this).mms_compat$getLayers()) {
            if (layer instanceof HumanoidArmorLayer) {
                ((HumanoidArmorLayerAccessor) layer).mms_compat$setBabyModelSet(kind.bakeArmor(context.getModelSet()));
            }
        }
    }
}
