package info.mudbourn.mmscompat.mixin.aerialhell;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Pops the pose Aerial Hell's resonator cancel leaves pushed, so the other hand does not inherit it.
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ResonatorHandPoseMixin {
    @WrapOperation(
        method = "submitHandsWithItems",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
        ),
        require = 2
    )
    private void mmsCompat$restorePose(
        FirstPersonHandsAndItemsRenderer renderer,
        PlayerRenderState player,
        FirstPersonHandsAndItemsRenderState hands,
        float frameInterp,
        float xRot,
        InteractionHand hand,
        float attack,
        ItemStack stack,
        float inverseArmHeight,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int light,
        Operation<Void> original
    ) {
        PoseStackDepthAccessor depth = (PoseStackDepthAccessor) poseStack;
        int before = depth.mmsCompat$depth();
        original.call(
            renderer,
            player,
            hands,
            frameInterp,
            xRot,
            hand,
            attack,
            stack,
            inverseArmHeight,
            poseStack,
            collector,
            light
        );
        while (depth.mmsCompat$depth() > before) {
            poseStack.popPose();
        }
    }
}
