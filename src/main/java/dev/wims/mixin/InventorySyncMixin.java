package dev.wims.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin class required for registration in wims.mixins.json.
 * The inventory synchronization is handled via Fabric ClientTickEvents in WimsMod.onInitialize().
 */
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class InventorySyncMixin {
    // Registered dynamically in WimsMod to listen to client-side ticks cleanly.
}
