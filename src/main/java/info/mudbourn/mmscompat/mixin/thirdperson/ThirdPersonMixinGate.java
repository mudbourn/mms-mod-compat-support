package info.mudbourn.mmscompat.mixin.thirdperson;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the Leawind's Third Person aim patch on that mod being loaded and on {@link #ENABLED}.
 */
public class ThirdPersonMixinGate implements IMixinConfigPlugin {

    // The Leawind's Third Person compat is broken, so it is switched off. Set true to turn on the aim patch and cramped first person again.
    public static final boolean ENABLED = false;

    private final boolean present =
        ENABLED && FabricLoader.getInstance().isModLoaded("leawind_third_person");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return present; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
