package info.mudbourn.mmscompat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import traben.entity_model_features.models.animation.EMFAnimationEntityContext;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.parts.EMFModelPartRoot;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.UUID;

/**
 * The EMF half of {@link PoseDebug}, kept in its own class so that loading the
 * debug switch never drags EMF's classes in.
 *
 * <p>{@code PoseCommand} touches {@link PoseDebug} to flip the flag, and that path
 * has to work whether or not Entity Model Features is installed. Putting the
 * {@code EMFModelPartRoot} walk here means only the gated mixins — which already do
 * not load without EMF — ever resolve those types.
 */
public final class PoseDebugEmf {

    private PoseDebugEmf() {
    }

    /**
     * Samples both arms off an EMF root, if this root belongs to the local player.
     *
     * <p>Reads the arms out of the vanilla part collection by name, the same way
     * EMF Compat's own applier and {@code HeldPoseAdditiveMixin} do — the parts EMF
     * animates are these, so sampling anything else would report a model nobody is
     * writing to.
     */
    public static void sample(String tag, EMFModelPartRoot root) {
        if (!PoseDebug.recording() || root == null) {
            return;
        }

        EMFEntityRenderState state = EMFAnimationEntityContext.getEmfState();
        if (state == null || state.emfEntity() == null) {
            return;
        }
        UUID uuid = state.emfEntity().etf$getUuid();
        if (uuid == null || !uuid.equals(localUuid())) {
            return;
        }

        ModelPart left = null;
        ModelPart right = null;
        for (EMFModelPartVanilla part : root.getAllVanillaPartsEMF()) {
            String name = part.toStringShort();
            if (name == null) {
                continue;
            }
            if (left == null && name.contains("left_arm")) {
                left = part;
            } else if (right == null && name.contains("right_arm")) {
                right = part;
            }
        }

        if (left != null && right != null) {
            PoseDebug.sample(tag, left, right);
        }
    }

    static UUID localUuid() {
        return Minecraft.getInstance().player == null
                ? null
                : Minecraft.getInstance().player.getUUID();
    }
}
