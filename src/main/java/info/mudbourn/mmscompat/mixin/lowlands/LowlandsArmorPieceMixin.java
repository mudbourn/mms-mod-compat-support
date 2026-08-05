package info.mudbourn.mmscompat.mixin.lowlands;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorModel;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorPose;
import info.mudbourn.mmscompat.client.lowlands.LowlandsArmorSets;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
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
import org.spongepowered.asm.mixin.Unique;
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
 * <p>Injected at {@code HEAD} of {@code renderArmorPiece}, not {@code submit}: the
 * slot is resolved by this point, and {@code submit} already carries the cemrelay
 * pose mixin, so this stays out of its way. See
 * {@code mixin.cemrelay.HumanoidArmorLayerMixin}.
 *
 * <p>Nothing here poses anything. Submission is deferred, so all posing happens at
 * draw time inside {@link LowlandsArmorPose} — which is the whole reason the first
 * cut rendered frozen armour.
 *
 * <p>The submit call mirrors vanilla's own exactly, substituting only the model:
 * {@code EquipmentLayerRenderer} still resolves the layer textures from the pack's
 * {@code equipment/<asset>.json}, so the inner/outer layer split and the existing
 * ported textures keep working untouched.
 */
@Mixin(HumanoidArmorLayer.class)
public abstract class LowlandsArmorPieceMixin {

    @Shadow @Final private EquipmentLayerRenderer equipmentRenderer;

    @Shadow protected abstract HumanoidModel<HumanoidRenderState> getArmorModel(HumanoidRenderState state, EquipmentSlot slot);

    @Shadow protected abstract boolean usesInnerModel(EquipmentSlot slot);

    /**
     * The wearer's own model, reached by cast rather than {@code @Shadow}.
     *
     * <p>{@code getParentModel()} is declared on {@link RenderLayer}, the
     * superclass, not on the layer this mixin targets. Mixin resolves
     * {@code @Shadow} members against the target class itself, so shadowing it
     * threw {@code InvalidMixinException} at apply time and silently disabled
     * this entire mixin. It is {@code public} on {@code RenderLayer}, so a cast
     * reaches it with no shadow at all.
     */
    @Unique
    private EntityModel<?> mms$parentModel() {
        return ((RenderLayer<?, ?>) (Object) this).getParentModel();
    }

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

        // Read the pose HERE, not at draw time. The cemrelay mixin has already posed
        // the vanilla armour model at submit HEAD, so it is correct at this instant —
        // but it and the wearer's model are single shared instances that every other
        // armour-wearing entity re-poses before the deferred draw runs. Copying from
        // them later reads whoever wrote last, which is the detached-armour bug.
        //
        // The wearer's model is handed over so the set can take its animation from
        // the actual player rig rather than approximating it; the vanilla armour
        // model is only the floor for when there is no CEM animation to relay.
        HumanoidModel<HumanoidRenderState> source = this.getArmorModel(state, slot);
        LowlandsArmorPose posed = LowlandsArmorPose.of(source, model, this.mms$parentModel(), state);

        EquipmentClientInfo.LayerType layerType = this.usesInnerModel(slot)
            ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
            : EquipmentClientInfo.LayerType.HUMANOID;

        this.equipmentRenderer.renderLayers(layerType, assetKey, posed,
            Pair.of(state, state), stack, poseStack, collector, light, state.outlineColor);

        ci.cancel();
    }
}
