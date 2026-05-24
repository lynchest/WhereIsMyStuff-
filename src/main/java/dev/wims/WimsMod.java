package dev.wims;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for WhereIsMyStuff? (WIMS) client-side mod.
 */
public class WimsMod implements ModInitializer {
    public static final String MOD_ID = "whereismystuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("WIMS (WhereIsMyStuff?) Mod Initialized!");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            // Rolling backup of inventory while player is alive
            if (client.player.isAlive()) {
                DeathInventoryCache.updatePreDeath(client.player.getInventory());
            } else {
                // Fallback capture when player is dead if handleStatus didn't catch it yet
                if (DeathInventoryCache.isEmpty() && DeathInventoryCache.hasPreDeath()) {
                    System.out.println("[WIMS DEBUG] Player is dead in tick. Triggering fallback pre-death capture.");
                    DeathInventoryCache.captureFromPreDeath();
                }
            }

            if (DeathInventoryCache.isEmpty()) {
                return;
            }

            for (int slotId = 0; slotId <= 40; slotId++) {
                if (DeathInventoryCache.has(slotId)) {
                    ItemStack current = client.player.getInventory().getStack(slotId);
                    ItemStack cached = DeathInventoryCache.get(slotId);

                    if (current != null && !current.isEmpty() 
                            && current.getItem() == cached.getItem() 
                            && current.getCount() >= cached.getCount()) {
                        DeathInventoryCache.clearSlot(slotId);
                    }
                }
            }
        });
    }
}

