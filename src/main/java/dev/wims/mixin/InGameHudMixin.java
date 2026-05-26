package dev.wims.mixin;

import dev.wims.WimsMod;
import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (DeathInventoryCache.isEmpty()) {
            return;
        }

        PlayerEntity player = this.client.player;
        if (player == null || DeathInventoryCache.isPlayerCreative(player) || DeathInventoryCache.isPlayerSpectator(player)) {
            return;
        }

        // Only render hotbar ghosts if no screen is open (or if it is just a chat screen)
        if (this.client.currentScreen != null && !(this.client.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen)) {
            return;
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int xStart = width / 2 - 91;
        int y = height - 19; // Vanilla item rendering y position on hotbar

        for (int i = 0; i < 9; i++) {
            // Hotbar slots in PlayerInventory are 0 to 8
            if (DeathInventoryCache.has(i)) {
                ItemStack current = player.getInventory().getStack(i);
                if (current == null || current.isEmpty()) {
                    ItemStack ghost = DeathInventoryCache.get(i);
                    float alphaVal = 0.35f;
                    boolean isFading = false;

                    if (ghost == null || ghost.isEmpty()) {
                        ghost = DeathInventoryCache.getFading(i);
                        if (ghost != null && !ghost.isEmpty()) {
                            alphaVal = DeathInventoryCache.getFadeAlpha(i);
                            isFading = true;
                        }
                    }

                    if (ghost != null && !ghost.isEmpty()) {
                        int x = xStart + i * 20 + 3; // Vanilla placement offset
                        try {
                            WimsMod.renderingGhostItem = true;
                            WimsMod.ghostItemAlpha = alphaVal;

                            // Draw slot dimming overlay
                            long time = System.currentTimeMillis();
                            float pulse = isFading ? alphaVal : (float) (Math.sin(time / 280.0) * 0.08 + 0.38);
                            int alpha = (int) (pulse * 255) & 0xFF;
                            int fillColor = (alpha << 24) | 0x220a0d; // Pulsating dark reddish/purple background
                            int borderColor = (alpha << 24) | 0x7c151c; // Dark red border

                            // Fill slot background
                            context.fill(x - 1, y - 1, x + 15, y + 15, fillColor);
                            // Draw 1px border
                            context.fill(x - 1, y - 1, x + 15, y, borderColor);
                            context.fill(x - 1, y + 14, x + 15, y + 15, borderColor);
                            context.fill(x - 1, y, x, y + 14, borderColor);
                            context.fill(x + 14, y, x + 15, y + 14, borderColor);

                            context.drawItem(ghost, x, y);
                            invokeDrawStackOverlay(context, this.client.textRenderer, ghost, x, y);

                            // Flush buffer
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
                                // Ignore
                            }
                        } finally {
                            WimsMod.renderingGhostItem = false;
                        }
                    }
                }
            }
        }
    }

    private static java.lang.reflect.Method drawStackOverlayMethod = null;
    private static boolean checkedDrawStackOverlay = false;

    private static void invokeDrawStackOverlay(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
        if (!checkedDrawStackOverlay) {
            checkedDrawStackOverlay = true;
            for (String mName : new String[]{"drawStackOverlay", "drawItemInSlot", "method_51431"}) {
                try {
                    drawStackOverlayMethod = DrawContext.class.getMethod(mName, TextRenderer.class, ItemStack.class, int.class, int.class);
                    drawStackOverlayMethod.setAccessible(true);
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
        }
        if (drawStackOverlayMethod != null) {
            try {
                drawStackOverlayMethod.invoke(context, textRenderer, stack, x, y);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
