package info.mudbourn.mmscompat;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Inspect Animations pose capture on the whole chain being present:
 * Inspect Animations to produce the pose, EMF to overwrite it, and
 * emf_compat_core to restore it. Missing any one of the three and there is
 * nothing to fix.
 */
public class InspectAnimMixinPlugin implements IMixinConfigPlugin {

    private final boolean available =
            FabricLoader.getInstance().isModLoaded("inspectanimations")
                    && FabricLoader.getInstance().isModLoaded("entity_model_features")
                    && FabricLoader.getInstance().isModLoaded("emf_compat_core");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return available; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
