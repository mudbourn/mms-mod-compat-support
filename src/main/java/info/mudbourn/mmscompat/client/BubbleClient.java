package info.mudbourn.mmscompat.client;

import info.mudbourn.mmscompat.bubble.BubbleContent;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;

/** Client side of the Bubblellaneous port. */
public final class BubbleClient {

    private BubbleClient() {}

    public static void register() {
        EntityRendererRegistry.register(BubbleContent.CAMERA_VIEW, NoopRenderer::new);
        SurveillanceCameraRenderer.registerModel();
        BlockEntityRenderers.register(BubbleContent.CAMERA_ENTITY, context -> new SurveillanceCameraRenderer());
        BlockEntityRenderers.register(BubbleContent.RISING_DOOR_ENTITY, context -> new RisingDoorRenderer());
    }
}
