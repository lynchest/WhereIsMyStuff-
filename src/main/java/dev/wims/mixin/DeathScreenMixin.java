package dev.wims.mixin;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on MinecraftClient to detect when the DeathScreen is opened.
 *
 * <p>This is the most reliable client-side death detection: regardless of
 * damage source (zombie, skeleton, /kill, void, lava, etc.), the DeathScreen
 * is always shown when the player dies. At this point we freeze the last
 * known inventory snapshot into the display cache for ghost rendering.</p>
 */
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class DeathScreenMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            dev.wims.WimsMod.log("DeathScreen detected! Freezing inventory snapshot.");
            DeathInventoryCache.freezeSnapshot();
        }
    }
}
