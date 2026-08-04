package info.mudbourn.mmscompat.mixin.lowlands;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorModel;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorSets;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws Clothing of the Lowlands vanity sets with their own geometry.
 *
 * <p>The sets are not vanilla-layout armour: each ships a custom model with its own
 * texture size and UV layout, so the stock humanoid equipment renderer maps their
 * atlases onto vanilla boxes and produces flat slabs. This diverts the draw to the
 * ported model in {@code client.lowlands} while leaving everything else — including
 * every other mod's and vanilla's own armour — on the untouched vanilla path.
 *
 * <p>The dispatch key is the stack's {@code equippable.asset_id}, the same value
 * {@code LowlandsVanity} stamps server-side, so no item registration is needed and
 * the vanilla-leather-plus-components design survives intact. A set that has not
 * been transcribed yet simply is not in the table and falls through to vanilla.
 *
 * <p>Injected at {@code HEAD} of {@code renderArmorPiece}, not {@code submit}. That
 * matters twice over: {@code submit} already carries the cemrelay pose mixin, and
 * by {@code renderArmorPiece} the slot has been resolved and the vanilla armour
 * model for it has been posed — including by that relay — so copying transforms
 * from it inherits all the existing pose work rather than competing with it. See
 * {@code mixin.cemrelay.HumanoidArmorLayerMixin}.
 *
 * <p>The submit call mirrors vanilla's own exactly, substituting only the model:
 * {@code EquipmentLayerRenderer} still resolves the layer textures from the pack's
 * {@code equipment/<asset>.json}, so the inner/outer layer split and the existing
 * ported textures keep working untouched.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class LowlandsArmorPieceMixin {

    @Shadow @Final private EquipmentLayerRenderer equipmentRenderer;

    @Shadow protected abstract HumanoidModel<?> getArmorModel(HumanoidRenderState state, EquipmentSlot slot);

    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot slot);

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void mms$renderLowlandsPiece(PoseStack poseStack,
                                         SubmitNodeCollector collector,
                                         ItemStack stack,
                                         EquipmentSlot slot,
                                         int light,
                                         HumanoidRenderState state,
                                         CallbackInfo ci) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }

        ResourceKey<EquipmentAsset> assetKey = equippable.assetId().get();
        Identifier assetId = assetKey.identifier();
        LowlandsArmorModel model = LowlandsArmorSets.model(assetId, slot);
        if (model == null) {
            return;
        }

        // Transforms only — part visibility was fixed when this per-slot instance
        // was baked. Mutating shared state here would not survive the deferred
        // draw; see the BAKED javadoc in LowlandsArmorSets.
        model.copyTransforms(this.getArmorModel(state, slot));

        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(slot)
            ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
            : EquipmentClientInfo.LayerType.HUMANOID;

        this.equipmentRenderer.renderLayers(layerType, assetKey, model, state, stack,
            poseStack, collector, light, state.outlineColor);

        ci.cancel();
    }
}
