package info.mudbourn.mmscompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmscompat.bubble.SurveillanceCameraBlockEntity;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Draws the surveillance camera head turned to its current sweep angle; the block model is only the stand. */
public final class SurveillanceCameraRenderer
    implements BlockEntityRenderer<SurveillanceCameraBlockEntity, SurveillanceCameraRenderer.State> {

    private static final ExtraModelKey<BlockStateModel> HEAD = ExtraModelKey.create(() -> "bubble camera head");
    private static final Identifier HEAD_MODEL =
        Identifier.fromNamespaceAndPath("mms_compat", "block/bubble/surveillance_camera/camera");

    public static void registerModel() {
        ModelLoadingPlugin.register(context -> context.addModel(HEAD, SimpleUnbakedExtraModel.blockStateModel(HEAD_MODEL)));
    }

    public static final class State extends BlockEntityRenderState {
        float yaw;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        SurveillanceCameraBlockEntity camera,
        State state,
        float partialTick,
        Vec3 cameraPos,
        ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderer.super.extractRenderState(camera, state, partialTick, cameraPos, crumbling);
        state.yaw = camera.viewYaw(partialTick);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraState) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getModel(HEAD);
        if (model == null) {
            return;
        }
        pose.pushPose();
        // The head model faces north, which is yaw 180; turn it about the block's vertical centre line.
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - state.yaw));
        pose.translate(-0.5, 0.0, -0.5);
        collector.submitBlockModel(
            pose,
            Sheets.cutoutBlockSheet(),
            model,
            1.0f,
            1.0f,
            1.0f,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            0
        );
        pose.popPose();
    }
}
