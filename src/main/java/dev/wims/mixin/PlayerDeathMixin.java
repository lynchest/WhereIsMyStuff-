package dev.wims.mixin;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin targeting LivingEntity to capture the player's inventory on client-side death.
 */
@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class PlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onPlayerDeath(DamageSource source, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || (Object) this != client.player) {
            return;
        }
        System.out.println("[WIMS DEBUG] onDeath called on client player.");
        // Try standard capture first, fallback to pre-death if it's empty
        boolean hasItems = false;
        for (int i = 0; i <= 40; i++) {
            if (!client.player.getInventory().getStack(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        if (hasItems) {
            DeathInventoryCache.capture(client.player.getInventory());
        } else {
            DeathInventoryCache.captureFromPreDeath();
        }
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        if (status == 3) { // EntityStatuses.DEATH
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || (Object) this != client.player) {
                return;
            }
            System.out.println("[WIMS DEBUG] handleStatus(3) (DEATH) received for client player. Capturing inventory.");
            DeathInventoryCache.captureFromPreDeath();
        }
    }
}
