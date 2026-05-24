package dev.wims;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
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
                DeathInventoryCache.updatePreDeath(client.player.getInventory(), client.player.hurtTime > 0);
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

            // Sync cache with current inventory first (if item already matches and count is sufficient)
            PlayerInventory inventory = client.player.getInventory();
            for (int slotId = 0; slotId <= 40; slotId++) {
                if (DeathInventoryCache.has(slotId)) {
                    ItemStack current = inventory.getStack(slotId);
                    ItemStack cached = DeathInventoryCache.get(slotId);

                    if (current != null && !current.isEmpty() 
                            && current.getItem() == cached.getItem() 
                            && current.getCount() >= cached.getCount()) {
                        DeathInventoryCache.clearSlot(slotId);
                    }
                }
            }

            // If it's empty now, return
            if (DeathInventoryCache.isEmpty()) {
                return;
            }

            // Auto-restore items from other slots into their ghost slots
            // Ensure:
            // 1. Player is alive
            // 2. No other screens (chest, anvil, etc.) are open (currentScreenHandler is playerScreenHandler)
            // 3. Cursor stack is empty (not holding anything with mouse)
            if (client.player.isAlive() 
                    && client.player.currentScreenHandler == client.player.playerScreenHandler
                    && client.player.currentScreenHandler.getCursorStack().isEmpty()
                    && client.interactionManager != null) {

                boolean actionTaken = false;
                for (int targetSlotId = 0; targetSlotId <= 40; targetSlotId++) {
                    if (DeathInventoryCache.has(targetSlotId)) {
                        ItemStack cached = DeathInventoryCache.get(targetSlotId);
                        ItemStack currentTarget = inventory.getStack(targetSlotId);

                        // We only try to restore if target slot is empty or has a smaller count of the same item
                        if (currentTarget == null || currentTarget.isEmpty() 
                                || (currentTarget.getItem() == cached.getItem() && currentTarget.getCount() < cached.getCount())) {

                            // Look for a source slot containing the same item
                            for (int sourceSlotId = 0; sourceSlotId <= 40; sourceSlotId++) {
                                if (sourceSlotId == targetSlotId) {
                                    continue;
                                }

                                ItemStack sourceStack = inventory.getStack(sourceSlotId);
                                if (sourceStack != null && !sourceStack.isEmpty() && sourceStack.getItem() == cached.getItem()) {
                                    
                                    // Make sure we don't steal from a ghost slot that has already been correctly restored
                                    boolean isValidSource = false;
                                    if (!DeathInventoryCache.has(sourceSlotId)) {
                                        isValidSource = true;
                                    } else {
                                        ItemStack sourceCached = DeathInventoryCache.get(sourceSlotId);
                                        if (sourceStack.getItem() != sourceCached.getItem()) {
                                            isValidSource = true;
                                        }
                                    }

                                    if (isValidSource) {
                                        int sourceHandlerId = getPlayerScreenHandlerSlotId(sourceSlotId);
                                        int targetHandlerId = getPlayerScreenHandlerSlotId(targetSlotId);

                                        if (sourceHandlerId != -1 && targetHandlerId != -1) {
                                            int syncId = client.player.currentScreenHandler.syncId;

                                            System.out.println("[WIMS DEBUG] Auto-restoring item " + sourceStack.getItem().toString() 
                                                    + " from slot " + sourceSlotId + " to slot " + targetSlotId);

                                            // 1. Pick up from source
                                            client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);

                                            // 2. Put into target
                                            client.interactionManager.clickSlot(syncId, targetHandlerId, 0, SlotActionType.PICKUP, client.player);

                                            // 3. Put remaining back in source if needed
                                            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                                                client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);
                                            }

                                            actionTaken = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (actionTaken) {
                        break;
                    }
                }
            }
        });
    }

    /**
     * Maps a PlayerInventory slot index (0-40) to the corresponding PlayerScreenHandler slot index.
     *
     * @param playerInventorySlot the PlayerInventory slot index
     * @return the PlayerScreenHandler slot index, or -1 if invalid
     */
    private static int getPlayerScreenHandlerSlotId(int playerInventorySlot) {
        if (playerInventorySlot >= 0 && playerInventorySlot <= 8) {
            // Hotbar: PlayerInventory 0-8 -> PlayerScreenHandler 36-44
            return 36 + playerInventorySlot;
        } else if (playerInventorySlot >= 9 && playerInventorySlot <= 35) {
            // Main Inventory: PlayerInventory 9-35 -> PlayerScreenHandler 9-35
            return playerInventorySlot;
        } else if (playerInventorySlot >= 36 && playerInventorySlot <= 39) {
            // Armor: PlayerInventory 36-39 -> PlayerScreenHandler 8-5
            // 36 (boots) -> 8, 37 (legs) -> 7, 38 (chest) -> 6, 39 (helmet) -> 5
            return 44 - playerInventorySlot;
        } else if (playerInventorySlot == 40) {
            // Offhand: PlayerInventory 40 -> PlayerScreenHandler 45
            return 45;
        }
        return -1;
    }
}
