package dev.wims.mixin;

import dev.wims.WimsMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = {"net.minecraft.client.gui.render.state.GuiRenderState", "net.minecraft.class_11246"})
public class GuiRenderStateMixin {

    @Inject(method = {"addItem(Ljava/lang/Object;)V", "method_71065(Ljava/lang/Object;)V"}, at = @At("HEAD"), require = 0)
    private void onAddItem(Object element, CallbackInfo ci) {
        captureGhostState(element);
    }

    @org.spongepowered.asm.mixin.Dynamic
    @Inject(method = {"addItem(Ljava/util/function/Consumer;Lnet/minecraft/client/gui/render/state/GuiRenderState$ItemRenderState;)V", "method_71065(Ljava/util/function/Consumer;Lnet/minecraft/class_11246$class_11276;)V"}, at = @At("HEAD"), require = 0)
    private void onAddItem2(java.util.function.Consumer<?> consumer, @org.spongepowered.asm.mixin.injection.Coerce Object renderState, CallbackInfo ci) {
        captureGhostState(renderState);
    }

    private void captureGhostState(Object element) {
        if (element == null) return;
        if (WimsMod.renderingGhostItem) {
            try {
                Object stateObj = null;
                // Try finding by method return type name containing KeyedItemRenderState, ItemRenderState or their intermediary equivalents (class_11540, class_10444)
                for (java.lang.reflect.Method method : element.getClass().getMethods()) {
                    if (method.getParameterCount() == 0) {
                        Class<?> returnType = method.getReturnType();
                        String typeName = returnType.getName();
                        if (typeName.contains("KeyedItemRenderState") || typeName.contains("ItemRenderState") || 
                            typeName.contains("class_11540") || typeName.contains("class_10444")) {
                            method.setAccessible(true);
                            stateObj = method.invoke(element);
                            if (stateObj != null) break;
                        }
                    }
                }
                
                // If not found by method, try finding by field type
                if (stateObj == null) {
                    for (java.lang.reflect.Field field : element.getClass().getFields()) {
                        Class<?> fieldType = field.getType();
                        String typeName = fieldType.getName();
                        if (typeName.contains("KeyedItemRenderState") || typeName.contains("ItemRenderState") || 
                            typeName.contains("class_11540") || typeName.contains("class_10444")) {
                            field.setAccessible(true);
                            stateObj = field.get(element);
                            if (stateObj != null) break;
                        }
                    }
                }

                if (stateObj != null) {
                    WimsMod.ghostRenderStates.put(stateObj, WimsMod.ghostItemAlpha);
                    if (System.currentTimeMillis() % 2000 < 20) {
                        WimsMod.log("GuiRenderState: Captured ghost state " + stateObj.getClass().getSimpleName());
                    }
                }
            } catch (Exception e) {
                // Ignore reflection failures
            }
        }
    }
}
