package dev.wims.mixin;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected Slot focusedSlot;

    // 1.21.4 — drawSlot(DrawContext, Slot)
    @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V", at = @At("TAIL"), require = 0, remap = false)
    private void drawSlot_1_21_4(DrawContext context, Slot slot, CallbackInfo ci) {
        renderGhostSlot(context, slot);
    }

    // 1.21.11 — drawSlot(DrawContext, Slot, int, int)
    @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;II)V", at = @At("TAIL"), require = 0, remap = false)
    private void drawSlot_1_21_11(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        renderGhostSlot(context, slot);
    }

    private void renderGhostSlot(DrawContext context, Slot slot) {
        if (DeathInventoryCache.isEmpty() || slot.hasStack()) {
            return;
        }
        if (!(slot.inventory instanceof PlayerInventory)) {
            return;
        }

        ItemStack ghost = DeathInventoryCache.get(slot.getIndex());
        float alphaVal = 0.35f;
        boolean isFading = false;

        if (ghost == null || ghost.isEmpty()) {
            ghost = DeathInventoryCache.getFading(slot.getIndex());
            if (ghost != null && !ghost.isEmpty()) {
                alphaVal = DeathInventoryCache.getFadeAlpha(slot.getIndex());
                isFading = true;
            }
        }

        if (ghost == null || ghost.isEmpty()) {
            return;
        }

        try {
            dev.wims.WimsMod.renderingGhostItem = true;
            dev.wims.WimsMod.ghostItemAlpha = alphaVal;

            // Draw a premium ghostly pulsating overlay with a border
            long time = System.currentTimeMillis();
            float pulse = isFading ? alphaVal : (float) (Math.sin(time / 280.0) * 0.08 + 0.38); // pulse between 0.30f and 0.46f
            int alpha = (int) (pulse * 255) & 0xFF;

            int fillColor = (alpha << 24) | 0x220a0d;    // Pulsating dark reddish/purple background
            int borderColor = (alpha << 24) | 0x7c151c; // Dark red border

            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, fillColor);
            // Draw 1px border manually
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 1, borderColor);
            context.fill(slot.x, slot.y + 15, slot.x + 16, slot.y + 16, borderColor);
            context.fill(slot.x, slot.y + 1, slot.x + 1, slot.y + 15, borderColor);
            context.fill(slot.x + 15, slot.y + 1, slot.x + 16, slot.y + 15, borderColor);

            context.drawItem(ghost, slot.x, slot.y);
            invokeDrawStackOverlay(context, MinecraftClient.getInstance().textRenderer, ghost, slot.x, slot.y);

            // To fix transparency in immediate-mode rendering, flush the draw context buffer
            try {
                java.lang.reflect.Method drawMethod = null;
                for (String mName : new String[]{"draw", "method_51452"}) {
                    try {
                        drawMethod = context.getClass().getMethod(mName);
                        break;
                    } catch (NoSuchMethodException ignored) {}
                }
                if (drawMethod != null) {
                    drawMethod.setAccessible(true);
                    drawMethod.invoke(context);
                }
            } catch (Exception e) {
                // Ignore reflection failures
            }
        } finally {
            dev.wims.WimsMod.renderingGhostItem = false;
        }
    }

    private static java.lang.reflect.Method drawStackOverlayMethod = null;
    private static boolean checkedDrawStackOverlay = false;

    private static void invokeDrawStackOverlay(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer, ItemStack stack, int x, int y) {
        if (!checkedDrawStackOverlay) {
            checkedDrawStackOverlay = true;
            for (String mName : new String[]{"drawStackOverlay", "drawItemInSlot", "method_51431"}) {
                try {
                    drawStackOverlayMethod = DrawContext.class.getMethod(mName, net.minecraft.client.font.TextRenderer.class, ItemStack.class, int.class, int.class);
                    drawStackOverlayMethod.setAccessible(true);
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
        }
        if (drawStackOverlayMethod != null) {
            try {
                drawStackOverlayMethod.invoke(context, textRenderer, stack, x, y);
            } catch (Exception e) {
                // Ignore invocation failures
            }
        }
    }

    // Render custom lost item hover tooltips
    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), require = 0, cancellable = true)
    private void drawGhostTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        if (focusedSlot != null && !focusedSlot.hasStack() && focusedSlot.inventory instanceof PlayerInventory) {
            ItemStack ghost = DeathInventoryCache.get(focusedSlot.getIndex());
            if (ghost != null && !ghost.isEmpty()) {
                java.util.List<net.minecraft.text.Text> tooltip = new java.util.ArrayList<>();
                tooltip.add(net.minecraft.text.Text.literal("§c§lKayıp Eşya"));
                tooltip.add(ghost.getName());
                tooltip.add(net.minecraft.text.Text.literal("§8Kurtarılması bekleniyor..."));
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, x, y);
                ci.cancel();
            }
        }
    }
}