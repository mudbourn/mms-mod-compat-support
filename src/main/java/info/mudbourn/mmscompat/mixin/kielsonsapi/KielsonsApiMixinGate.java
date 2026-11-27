package info.mudbourn.mmscompat.mixin.kielsonsapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the Kielson's API server patch on that mod being loaded on a dedicated server.
 */
public class KielsonsApiMixinGate implements IMixinConfigPlugin {

    private final boolean active =
        FabricLoader.getInstance().isModLoaded("kielsonsapi")
            && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return active; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    // Empties the client-only shield helpers so the verifier never loads client classes.
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        targetClass.methods.removeIf(method -> method.name.startsWith("lambda$") && isShieldHelper(method));
        for (MethodNode method : targetClass.methods) {
            if (isShieldHelper(method)) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                method.maxStack = 0;
            }
        }
    }

    private static boolean isShieldHelper(MethodNode method) {
        return method.name.contains("generateShield") || method.name.contains("registerDynamicShield");
    }
}
