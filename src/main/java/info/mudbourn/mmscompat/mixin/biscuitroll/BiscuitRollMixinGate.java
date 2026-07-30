package info.mudbourn.mmscompat.mixin.biscuitroll;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Biscuit Roll animation-reload guard on biscuit_roll being loaded.
 *
 * <p>Biscuit Roll Lib ships jar-in-jar inside Useless Reptile
 * ({@code META-INF/jars/biscuit_roll-0.12.0.jar}), so it is present whenever
 * Useless Reptile is, but it is still a separately loaded mod id.</p>
 */
public class BiscuitRollMixinGate implements IMixinConfigPlugin {

    private final boolean present =
        FabricLoader.getInstance().isModLoaded("biscuit_roll");

    private final boolean reptilePresent =
        FabricLoader.getInstance().isModLoaded("uselessreptile");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }

    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // The animation-id fix lives in a Useless Reptile class, not a Biscuit Roll one.
        if (targetClassName.startsWith("nordmods.uselessreptile.")) {
            return reptilePresent;
        }
        return present;
    }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
