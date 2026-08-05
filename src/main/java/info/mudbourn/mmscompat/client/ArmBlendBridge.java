package info.mudbourn.mmscompat.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import traben.entity_texture_features.features.state.ETFEntityRenderState;
import traben.entity_texture_features.features.state.HoldsETFRenderState;

import java.util.UUID;

/**
 * Lends {@link PoseBlend}'s in-flight arm rotations to readers that run too early to
 * see them.
 *
 * <h2>The ordering problem</h2>
 *
 * <p>A frame has two phases. Everything <em>submits</em> first — {@code setupAnim} on
 * the entity's model, then each {@code RenderLayer#submit} collecting nodes — and the
 * collected nodes are <em>drawn</em> afterwards. EMF animates in the second phase:
 * {@code EMFModelPartWithState#render} calls {@code EMFModelPartRoot#animate}, which
 * is where {@code PoseReleaseMixin} drives the blend.
 *
 * <p>So every armour path reads the wearer one phase too early. The relay in
 * {@code mixin.cemrelay.HumanoidArmorLayerMixin} runs at {@code submit} HEAD, and
 * {@code LowlandsArmorPose#capture} reads its output from inside
 * {@code renderArmorPiece}, still within that same submit. Neither can see a blend
 * that has not been computed yet. The body eases across a handover and the armour on
 * top of it cuts — which is the whole point of the easing lost for anyone wearing
 * something.
 *
 * <h2>What this does</h2>
 *
 * <p>{@link #apply} writes the arms as they were <em>last drawn</em> — the value
 * {@link PoseBlend#blendedArms} kept from the previous frame's draw phase — onto the
 * wearer's arm parts, just long enough for the armour to be posed from them, and
 * {@link #restore} puts back whatever was there. The draw phase then recomputes the
 * body's own arms from scratch as it always did; nothing here survives into it.
 *
 * <p>One frame of lag is the price, and it is only paid during a transition: outside
 * one {@code blendedArms} returns null and this is a no-op down to a map lookup. At
 * 60fps a 16ms lag against a 260ms ease is under a frame's worth of the curve, far
 * below the snap it removes. Matching phases properly would mean moving the blend
 * ahead of submit, which cannot be done — it has to run after EMF's animation, and
 * EMF's animation is in the draw.
 *
 * <p>Restoring rather than leaving the values is deliberate. The wearer's model is a
 * single shared instance; leaving a stale arm on it would hand the next entity to
 * submit this player's pose, which is the same class of bug
 * {@code LowlandsArmorPose} exists to avoid.
 */
public final class ArmBlendBridge {

    private ArmBlendBridge() {
    }

    /**
     * Swaps the blended arms onto {@code model}, returning what they held so
     * {@link #restore} can undo it, or null when there is nothing to do.
     *
     * @param model the wearer's own model
     * @param state the render state being submitted, for the wearer's identity
     */
    public static float[] apply(EntityModel<?> model, EntityRenderState state) {
        if (model == null) {
            return null;
        }
        UUID uuid = uuidOf(state);
        if (uuid == null) {
            return null;
        }
        float[] blended = PoseBlend.blendedArms(uuid);
        if (blended == null) {
            return null;
        }

        ModelPart left = CemLayerPoseRelay.findChild(model.root(), "left_arm");
        ModelPart right = CemLayerPoseRelay.findChild(model.root(), "right_arm");
        if (left == null || right == null) {
            return null;
        }

        float[] previous = {
                left.xRot, left.yRot, left.zRot,
                right.xRot, right.yRot, right.zRot
        };
        write(left, right, blended);
        return previous;
    }

    /** Puts back what {@link #apply} displaced. A null {@code previous} is a no-op. */
    public static void restore(EntityModel<?> model, float[] previous) {
        if (model == null || previous == null) {
            return;
        }
        ModelPart left = CemLayerPoseRelay.findChild(model.root(), "left_arm");
        ModelPart right = CemLayerPoseRelay.findChild(model.root(), "right_arm");
        if (left != null && right != null) {
            write(left, right, previous);
        }
    }

    private static void write(ModelPart left, ModelPart right, float[] arms) {
        left.xRot = arms[0];
        left.yRot = arms[1];
        left.zRot = arms[2];
        right.xRot = arms[3];
        right.yRot = arms[4];
        right.zRot = arms[5];
    }

    /**
     * The entity behind a render state, via ETF's own attachment.
     *
     * <p>{@code EMFAnimationEntityContext} is not used here for the same reason the
     * phase split exists: it is set up around EMF's animation, which is in the draw
     * phase, and this runs in the submit. ETF hangs its state off the render state
     * itself, which is the object actually in hand at submit time.
     */
    private static UUID uuidOf(EntityRenderState state) {
        if (!(state instanceof HoldsETFRenderState holder)) {
            return null;
        }
        ETFEntityRenderState etf = holder.etf$getState();
        return etf == null ? null : etf.uuid();
    }
}
