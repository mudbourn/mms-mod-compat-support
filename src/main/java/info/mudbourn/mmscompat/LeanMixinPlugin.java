package info.mudbourn.mmscompat;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the body-lean mixin on Custom Player Animations being <em>absent</em>.
 *
 * <p>Inverted relative to {@link CpaMixinPlugin} on purpose. This feature exists
 * to replace CPA's lean, so if CPA is ever installed alongside — a client that
 * kept it, or a rollback — both would apply and the player would lean twice as
 * far. Standing down when CPA is present makes that combination harmless instead
 * of a visual bug someone has to diagnose.
 */
public class LeanMixinPlugin implements IMixinConfigPlugin {

    private final boolean cpaAbsent = !FabricLoader.getInstance().isModLoaded("cpa");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return cpaAbsent; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
