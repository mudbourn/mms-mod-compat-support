package info.mudbourn.mmscompat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmscompat.bubble.RotatedBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;

/** Draws a block's north-facing model turned to its free yaw. */
public final class RotatedBlockRenderer implements BlockEntityRenderer<RotatedBlockEntity, RotatedBlockRenderer.State> {

    private static final long MODEL_SEED = 42L;

    public static final class State extends BlockEntityRenderState {
        float yaw;
        final List<BlockStateModelPart> parts = new ArrayList<>();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        RotatedBlockEntity block,
        State state,
        float partialTick,
        Vec3 cameraPos,
        ModelFeatureRenderer.CrumblingOverlay crumbling
    ) {
        BlockEntityRenderer.super.extractRenderState(block, state, partialTick, cameraPos, crumbling);
        state.yaw = block.yaw();
        state.parts.clear();
        Minecraft.getInstance()
            .getModelManager()
            .getBlockStateModelSet()
            .get(block.getBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH))
            .collectParts(RandomSource.create(MODEL_SEED), state.parts);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.parts.isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.rotate(Axis.YP.rotationDegrees(180.0f - state.yaw));
        pose.translate(-0.5, 0.0, -0.5);
        collector.submitBlockModel(
            pose,
            Sheets.cutoutBlockItemSheet(),
            new ArrayList<>(state.parts),
            BlockModelRenderState.EMPTY_TINTS,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            0
        );
        pose.popPose();
    }
}
