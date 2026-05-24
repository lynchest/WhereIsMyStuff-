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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to intercept item rendering and force translucent render layers for ghost items.
 */
@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /**
     * Intercepts and wraps the VertexConsumerProvider to redirect opaque and cutout layers
     * to translucent versions when rendering ghost items.
     *
     * @param original the original VertexConsumerProvider
     * @return the wrapped or original VertexConsumerProvider
     */
    @ModifyVariable(method = "renderItem", at = @At("HEAD"), argsOnly = true)
    private static VertexConsumerProvider wrapVertexConsumers(VertexConsumerProvider original) {
        if (WimsMod.renderingGhostItem) {
            return new VertexConsumerProvider() {
                @Override
                public VertexConsumer getBuffer(RenderLayer layer) {
                    String layerName = layer.toString().toLowerCase();
                    if (layerName.contains("solid") || layerName.contains("cutout")) {
                        return original.getBuffer(RenderLayer.getTranslucent());
                    }
                    return original.getBuffer(layer);
                }
            };
        }
        return original;
    }
}
