package dev.wims.mixin;

import dev.wims.WimsMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Mixin to intercept ItemRenderer item rendering and apply 35% alpha opacity for ghost items.
 *
 * <p>Uses a dual-environment configuration with {@code require = 0} to ensure 100% crash-safe execution
 * across both Yarn (development) and Intermediary (production) runtimes without requiring a refmap.</p>
 */
@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    private static final Map<RenderLayer, Boolean> PROMOTION_CACHE = new WeakHashMap<>();

    /**
     * Redirects getBuffer for all rendering paths in ItemRenderer.
     * Combined into a single static handler to prevent redirection conflicts and ensure
     * compatibility with both static and instance methods in 1.21.4.
     */
    @Redirect(
        method = {
            "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/item/ItemDisplayContext;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V",
            "method_23178", "method_23177", "method_23179", "method_23181",
            "method_62476",
            "renderItem(Lnet/minecraft/client/render/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;III[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V",
            "getItemGlintConsumer", "getSpecialItemGlintConsumer", "getDynamicDisplayGlintConsumer", "getArmorGlintConsumer",
            "method_23180", "method_23182"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
        ),
        require = 0
    )
    private static VertexConsumer redirectGetBuffer(VertexConsumerProvider provider, RenderLayer layer) {
        if (WimsMod.renderingGhostItem) {
            RenderLayer targetLayer = layer;
            Boolean shouldPromote = PROMOTION_CACHE.get(layer);
            if (shouldPromote == null) {
                String layerName = layer.toString().toLowerCase();
                shouldPromote = layerName.contains("solid") || layerName.contains("cutout");
                PROMOTION_CACHE.put(layer, shouldPromote);
            }
            if (shouldPromote) {
                targetLayer = getTranslucentLayer();
            }
            return createGhostConsumer(provider.getBuffer(targetLayer));
        }
        return provider.getBuffer(layer);
    }

    private static RenderLayer getTranslucentLayer() {
        for (String name : new String[]{"getTranslucent", "translucent", "method_23583"}) {
            try {
                java.lang.reflect.Method method = RenderLayer.class.getMethod(name);
                return (RenderLayer) method.invoke(null);
            } catch (NoSuchMethodException e) {
                // Ignore and try next
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Failed to find translucent RenderLayer method");
    }

    private static VertexConsumer createGhostConsumer(VertexConsumer delegate) {
        return (VertexConsumer) java.lang.reflect.Proxy.newProxyInstance(
            VertexConsumer.class.getClassLoader(),
            new Class<?>[]{VertexConsumer.class},
            new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    // color(int r, int g, int b, int a) OR method_1336 (intermediary)
                    if (parameterTypes.length == 4 
                            && parameterTypes[0] == int.class 
                            && parameterTypes[1] == int.class 
                            && parameterTypes[2] == int.class 
                            && parameterTypes[3] == int.class) {
                        
                        int a = (Integer) args[3];
                        args[3] = (int) (a * WimsMod.ghostItemAlpha);
                    }
                    // color(int argb)
                    else if (parameterTypes.length == 1 && parameterTypes[0] == int.class) {
                        // We check for any 1-int method that might be color(argb) - risky but usually color is the only one
                        // Better: check for "color" or intermediary "method_22915"
                        String name = method.getName();
                        if (name.contains("color") || name.equals("method_22915")) {
                            int argb = (Integer) args[0];
                            int a = (argb >> 24) & 0xFF;
                            if (a == 0) a = 255; // If alpha is missing (RGB), assume 255
                            
                            int r = (argb >> 16) & 0xFF;
                            int g = (argb >> 8) & 0xFF;
                            int b = argb & 0xFF;
                            
                            a = (int) (a * WimsMod.ghostItemAlpha);
                            args[0] = (a << 24) | (r << 16) | (g << 8) | b;
                        }
                    }
                    // color(float r, float g, float b, float a)
                    else if (parameterTypes.length == 4 
                            && parameterTypes[0] == float.class 
                            && parameterTypes[1] == float.class 
                            && parameterTypes[2] == float.class 
                            && parameterTypes[3] == float.class) {
                        
                        float a = (Float) args[3];
                        args[3] = a * WimsMod.ghostItemAlpha;
                    }

                    Object result = method.invoke(delegate, args);
                    if (result == delegate) {
                        return proxy;
                    }
                    return result;
                }
            }
        );
    }
}
