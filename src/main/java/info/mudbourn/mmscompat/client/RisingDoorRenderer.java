package info.mudbourn.mmscompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmscompat.bubble.RisingDoorBlock;
import info.mudbourn.mmscompat.bubble.RisingDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Draws a rising door panel slid up (top piece) or down (lower piece) by its open fraction. */
public final class RisingDoorRenderer
    implements BlockEntityRenderer<RisingDoorBlockEntity, RisingDoorRenderer.State> {

    private static final float TOP_TRAVEL = 0.94f;

    private static final float BOTTOM_TRAVEL = -1.0f;

    public static final class State extends BlockEntityRenderState {
        float offset;
        boolean hidden;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        RisingDoorBlockEntity door,
        State state,
        float partialTick,
        Vec3 cameraPos,
        ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderer.super.extractRenderState(door, state, partialTick, cameraPos, crumbling);
        boolean bottom = state.blockState.getValue(RisingDoorBlock.ABOVE);
        float openness = door.openness(partialTick);
        state.offset = openness * (bottom ? BOTTOM_TRAVEL : TOP_TRAVEL);
        state.hidden = bottom && openness >= 1.0f;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.hidden) {
            return;
        }
        BlockStateModel model = Minecraft.getInstance()
            .getBlockRenderer()
            .getBlockModel(state.blockState.setValue(RisingDoorBlock.OPEN, false));
        pose.pushPose();
        pose.translate(0.0, state.offset, 0.0);
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
