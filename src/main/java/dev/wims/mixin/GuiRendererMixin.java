package dev.wims.mixin;

import dev.wims.WimsMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = {"net.minecraft.client.gui.render.GuiRenderer", "net.minecraft.class_11228"})
public class GuiRendererMixin {

    @Inject(method = {"prepareItemInitially", "method_70889"}, at = @At("HEAD"), require = 0)
    private void onPrepareItemStart(@org.spongepowered.asm.mixin.injection.Coerce Object state, MatrixStack matrices, int x, int y, int size, CallbackInfo ci) {
        if (state != null) {
            Float alpha = WimsMod.ghostRenderStates.get(state);
            if (alpha != null) {
                WimsMod.renderingGhostItem = true;
                WimsMod.ghostItemAlpha = alpha;
            }
        }
    }

    @Inject(method = {"prepareItemInitially", "method_70889"}, at = @At("RETURN"), require = 0)
    private void onPrepareItemEnd(@org.spongepowered.asm.mixin.injection.Coerce Object state, MatrixStack matrices, int x, int y, int size, CallbackInfo ci) {
        WimsMod.renderingGhostItem = false;
        WimsMod.ghostItemAlpha = 0.35f;
    }
}
