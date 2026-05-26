package dev.wims.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

public class WimsMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("GuiRenderStateMixin") || mixinClassName.endsWith("GuiRendererMixin")) {
            ClassLoader cl = getClass().getClassLoader();
            boolean isNewGui = false;
            try {
                Class.forName("net.minecraft.client.gui.render.state.GuiRenderState", false, cl);
                isNewGui = true;
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName("net.minecraft.class_11246", false, cl);
                    isNewGui = true;
                } catch (ClassNotFoundException e2) {
                    isNewGui = false;
                }
            }

            if (isNewGui) {
                dev.wims.WimsMod.log("[MixinConfig] Applying 1.21.2+ GUI mixin: " + mixinClassName);
                return true;
            } else {
                dev.wims.WimsMod.log("[MixinConfig] Skipping 1.21.2+ GUI mixin (not 1.21.2+ environment): " + mixinClassName);
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
