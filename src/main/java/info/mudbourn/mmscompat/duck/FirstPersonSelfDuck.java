package info.mudbourn.mmscompat.duck;

/**
 * Marks a render state as "this is the camera entity, drawn in first person".
 *
 * <p>Render states carry no back-reference to the entity they were extracted
 * from, so anything downstream of extraction — a feature renderer, a layer —
 * has no way to ask "is this me?" on its own. The flag is stamped once during
 * extraction, while the entity is still in hand, and read at draw time.
 *
 * <p>Deliberately not sourced from First-person Model's own
 * {@code LivingEntityRenderStateAccess}: reading that would make this a
 * compile-time dependency on a client mod we do not control the version of,
 * for a question — camera entity, first-person camera — that vanilla can
 * answer by itself.
 *
 * @see info.mudbourn.mmscompat.mixin.headhide.LivingEntityRendererMixin
 */
public interface FirstPersonSelfDuck {

    boolean mmsCompat$isFirstPersonSelf();

    void mmsCompat$setFirstPersonSelf(boolean firstPersonSelf);
}
