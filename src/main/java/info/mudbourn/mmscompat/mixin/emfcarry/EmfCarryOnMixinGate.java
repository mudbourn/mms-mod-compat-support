package info.mudbourn.mmscompat.mixin.emfcarry;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Carry On body-pose capture throttle on both halves of the
 * EMF Compat Carry On stack being present.
 *
 * <p>{@code emf_compat_carry_on} supplies the handler being cancelled;
 * {@code emf_compat_core} supplies {@code BodyPartSync}, which the gate reads
 * to decide whether the capture is wanted. Neither is any use without the
 * other, so a single presence check covers both.</p>
 */
public class EmfCarryOnMixinGate implements IMixinConfigPlugin {

    private final boolean present =
        FabricLoader.getInstance().isModLoaded("emf_compat_carry_on")
            && FabricLoader.getInstance().isModLoaded("emf_compat_core")
            && FabricLoader.getInstance().isModLoaded("entity_model_features");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }

    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return present;
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
