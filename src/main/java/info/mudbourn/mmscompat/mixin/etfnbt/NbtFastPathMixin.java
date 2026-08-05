package info.mudbourn.mmscompat.mixin.etfnbt;

import info.mudbourn.mmscompat.client.etfnbt.NbtFastPath;
import info.mudbourn.mmscompat.client.etfnbt.NbtTuning;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_texture_features.features.property_reading.properties.optifine_properties.NBTProperty;
import traben.entity_texture_features.features.state.ETFEntityRenderState;

import java.util.Map;

/**
 * Answers ETF's {@code nbt(...)} rules without serialising the entity.
 *
 * <p>{@code getEntityNBT} is the narrowest seam that still knows what is being
 * asked for. {@code NBTMethod} builds one {@code NBTProperty} per {@code nbt(...)}
 * call site with a single-entry {@code NBT_MAP}, so the shadowed key set is exactly
 * the paths this rule tests — which is what lets {@link NbtFastPath} decide whether
 * it can answer at all. Injecting further in, at {@code ETFEntityRenderState#nbt()}
 * or {@code Entity#etf$getNbt}, would be too late: the requested path is gone by
 * then and the only options are a full tag or a guess.
 *
 * <p>Cancelling is safe because ETF treats the return as the whole entity tag and
 * walks each rule's path from its root. A tag holding only that path resolves
 * identically. See {@link NbtFastPath} for why the omitted keys are omitted.
 */
@Mixin(NBTProperty.class)
public abstract class NbtFastPathMixin {

    /**
     * The paths this property tests.
     *
     * <p>Declared with a wildcard value type: the real one is {@code NBTProperty}'s
     * private nested {@code NBTTester}, and only the key set is wanted. Field
     * descriptors erase generics, so the shadow still matches.
     */
    @Shadow @Final private Map<String, ?> NBT_MAP;

    @Inject(method = "getEntityNBT", at = @At("HEAD"), cancellable = true)
    private void mms$serveFromLiveState(ETFEntityRenderState state,
                                        CallbackInfoReturnable<CompoundTag> cir) {
        if (!NbtTuning.fastPath || state == null) {
            return;
        }
        // ETFEntity is a duck interface mixed into Entity; block entities implement
        // it too and have none of the paths this covers, so they fall through.
        //
        // entity() is @Deprecated upstream but has no replacement that reaches the
        // live entity, and ETFEntityRenderStateViaReference — the only implementation
        // ETF ships — is a live reference, not a snapshot. If a future ETF drops it
        // this stops compiling, which is the failure worth having: the alternative is
        // reading a stale entity. Nothing here is correct without a live one.
        if (!(state.entity() instanceof Entity entity)) {
            return;
        }

        CompoundTag fast = NbtFastPath.build(this.NBT_MAP.keySet(), entity);
        if (fast == null) {
            // A path nobody has taught this about. Let the real serialisation answer.
            return;
        }

        if (NbtTuning.verify) {
            // Deliberately still pays for the real tag: the point is to prove the two
            // agree in game, not to be fast while doing it.
            CompoundTag real = state.nbt();
            if (real != null) {
                NbtFastPath.verify(this.NBT_MAP.keySet(), fast, real);
            }
        }

        cir.setReturnValue(fast);
    }
}
