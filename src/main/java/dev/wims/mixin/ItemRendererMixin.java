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

/**
 * Mixin to intercept ItemRenderer item rendering and apply 35% alpha opacity for ghost items.
 *
 * <p>Uses a dual-environment configuration with {@code require = 0} to ensure 100% crash-safe execution
 * across both Yarn (development) and Intermediary (production) runtimes without requiring a refmap.</p>
 */
@Environment(EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /**
     * Redirects getBuffer for both development (Yarn) and production (Intermediary) environments.
     * Combines all signature overloads to prevent runtime conflicts and ensure crash-safe execution.
     */
    @Redirect(
        method = {"renderItem", "method_9701", "method_23177", "method_23178"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"
        ),
        require = 0
    )
    private VertexConsumer redirectGetBuffer(VertexConsumerProvider provider, RenderLayer layer) {
        if (WimsMod.renderingGhostItem) {
            RenderLayer targetLayer = layer;
            String layerName = layer.toString().toLowerCase();
            if (layerName.contains("solid") || layerName.contains("cutout")) {
                targetLayer = RenderLayer.getTranslucent();
            }
            return new GhostVertexConsumer(provider.getBuffer(targetLayer));
        }
        return provider.getBuffer(layer);
    }

    @Environment(EnvType.CLIENT)
    private static class GhostVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;

        public GhostVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            delegate.color(r, g, b, (int) (a * 0.35f));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            delegate.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }
    }
}
