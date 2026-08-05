package info.mudbourn.mmscompat.mixin.headhide;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmscompat.duck.FirstPersonSelfDuck;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops head-slot equipment from the first-person body.
 *
 * <p>With First-person Model on, the player's own body is drawn from inside its
 * own head — and the head model is the one part that is not drawn, so anything
 * worn on it is left hanging in the middle of the screen. Vanilla never had to
 * care, because vanilla first person draws no body at all.
 *
 * <p>The cut is at the slot, not at an item list: every helmet in the pack —
 * vanilla, modded, and the Clothing of the Lowlands vanity sets — reaches the
 * screen through this one method, so keying on {@link EquipmentSlot#HEAD}
 * covers the lot with nothing to maintain per item. Skulls, mob heads and
 * blocks worn on the head take a different route, {@code CustomHeadLayer},
 * which First-person Model already cancels for the camera entity.
 *
 * <p>Priority is above default so this HEAD injection is applied last and
 * therefore runs first, ahead of {@code mixin.lowlands.LowlandsArmorPieceMixin}
 * on the same seam — that one cancels the call when it takes over the draw, so
 * losing the race would let Lowlands helmets through the one hole this is meant
 * to close. Both mixins are ours, so the ordering is settled here rather than
 * left to load order.
 *
 * <p>First person only, and only for the camera entity: {@code getCameraType()}
 * is re-read on every extraction, so switching to third person restores the
 * helmet on the same frame, and other players keep theirs throughout.
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 1500)
public abstract class HeadEquipmentHideMixin {

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void mmsCompat$hideOwnHeadgear(PoseStack poseStack,
                                           SubmitNodeCollector collector,
                                           ItemStack stack,
                                           EquipmentSlot slot,
                                           int light,
                                           HumanoidRenderState state,
                                           CallbackInfo ci) {
        if (slot == EquipmentSlot.HEAD && ((FirstPersonSelfDuck) state).mmsCompat$isFirstPersonSelf()) {
            ci.cancel();
        }
    }
}
