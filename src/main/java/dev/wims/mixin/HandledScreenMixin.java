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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    // 1.21.4 — drawSlot(DrawContext, Slot)
    @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V", at = @At("TAIL"), require = 0, remap = false)
    private void drawSlot_1_21_4(DrawContext context, Slot slot, CallbackInfo ci) {
        if (DeathInventoryCache.isEmpty() || slot.hasStack()) return;
        if (!(slot.inventory instanceof PlayerInventory)) return;

        ItemStack ghost = DeathInventoryCache.get(slot.getIndex());
        if (ghost == null || ghost.isEmpty()) return;

        context.drawItem(ghost, slot.x, slot.y);

        // Draw the dark dimming overlay to indicate a ghost slot (cross-version safe)
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x8C000000);

        context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, ghost, slot.x, slot.y);
    }

    // 1.21.11 — drawSlot(DrawContext, Slot, int, int)
    @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;II)V", at = @At("TAIL"), require = 0, remap = false)
    private void drawSlot_1_21_11(DrawContext context, Slot slot, int x, int y, CallbackInfo ci) {
        if (DeathInventoryCache.isEmpty() || slot.hasStack()) return;
        if (!(slot.inventory instanceof PlayerInventory)) return;

        ItemStack ghost = DeathInventoryCache.get(slot.getIndex());
        if (ghost == null || ghost.isEmpty()) return;

        // 1.21.11 Rendering — No RenderSystem changes, no ItemRendererMixin (renderingGhostItem is false)
        context.drawItem(ghost, slot.x, slot.y);
        
        // Draw the dark dimming overlay to indicate a ghost slot on 1.21.11
        context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x8C000000);
        
        context.drawStackOverlay(MinecraftClient.getInstance().textRenderer, ghost, slot.x, slot.y);
    }
}