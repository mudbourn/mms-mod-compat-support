package info.mudbourn.mmscompat.mixin.heldpose;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.List;
import java.util.Set;

/**
 * Gates the held-pose mixin on both mods it stands between: Better Combat supplies
 * the pose to preserve, EMF Compat: Core owns the store that preserves it. With
 * either absent the target class still loads, so the gate is about not referencing
 * classes that are not there.
 */
public class HeldPoseMixinGate implements IMixinConfigPlugin {

    private final boolean present = FabricLoader.getInstance().isModLoaded("bettercombat")
            && FabricLoader.getInstance().isModLoaded("emf_compat_core");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return present; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, org.spongepowered.asm.mixin.extensibility.IMixinInfo mixinInfo) {}
}
