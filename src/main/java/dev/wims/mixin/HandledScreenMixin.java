package dev.wims.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
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

/**
 * Mixin to render ghost items in empty player inventory slots under client-side.
 */
@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void renderGhostItem(DrawContext context, Slot slot, CallbackInfo ci) {
        if (DeathInventoryCache.isEmpty()) {
            return;
        }
        if (slot.hasStack()) {
            return;
        }
        if (!(slot.inventory instanceof PlayerInventory)) {
            return;
        }

        int slotId = slot.getIndex();
        if (!DeathInventoryCache.has(slotId)) {
            return;
        }

        ItemStack ghost = DeathInventoryCache.get(slotId);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);

        MinecraftClient client = MinecraftClient.getInstance();
        context.drawItem(ghost, slot.x, slot.y);
        context.drawStackOverlay(client.textRenderer, ghost, slot.x, slot.y);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
