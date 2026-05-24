package dev.wims;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
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
    public static boolean renderingGhostItem = false;

    private static float lastHealth = -1f;
    private static int damageCooldown = 0;
    private static int prevCooldown = -1;

    public static void log(String message) {
        LOGGER.info("[WIMS-Debug] {}", message);
    }

    @Override
    public void onInitialize() {
        log("WIMS Mod Initialized!");

        // Clear cache and snapshots on disconnect to prevent carrying ghosts across different servers/worlds
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            log("Disconnected from server. Resetting inventory cache.");
            DeathInventoryCache.reset();
            lastHealth = -1f;
            damageCooldown = 0;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                if (lastHealth != -1f) {
                    log("Player is null. Resetting tracking.");
                }
                lastHealth = -1f;
                damageCooldown = 0;
                return;
            }

            // Health and damage tracking to prevent snapshot corruption during death sequence
            float currentHealth = client.player.getHealth();
            if (lastHealth == -1f) {
                lastHealth = currentHealth;
                log("Initial health set to " + lastHealth);
            }

            boolean tookDamage = currentHealth < lastHealth || client.player.hurtTime > 0;
            if (currentHealth < lastHealth) {
                log("Health decreased! " + lastHealth + " -> " + currentHealth);
            }
            if (client.player.hurtTime > 0 && damageCooldown == 0) {
                log("hurtTime > 0 detected! hurtTime = " + client.player.hurtTime);
            }
            lastHealth = currentHealth;

            if (tookDamage) {
                damageCooldown = 20; // Pause snapshot updates for 20 ticks (1 second) after taking damage
            } else if (damageCooldown > 0) {
                damageCooldown--;
            }

            if (damageCooldown != prevCooldown) {
                log("damageCooldown = " + damageCooldown + ", health = " + currentHealth + ", hurtTime = " + client.player.hurtTime + ", isAlive = " + client.player.isAlive());
                prevCooldown = damageCooldown;
            }

            // Save inventory snapshot every tick while the player is alive and hasn't taken damage recently.
            if (client.player.isAlive() && client.player.getHealth() > 0f) {
                if (damageCooldown == 0) {
                    DeathInventoryCache.saveSnapshot(client.player.getInventory());
                }
            }

            // Nothing to do if no ghost items are cached
            if (DeathInventoryCache.isEmpty()) {
                return;
            }

            // Skip syncing while the player is dead or on the death screen.
            // This prevents a race condition where the death screen opens, and in the very same tick,
            // the client-side tick listener sees the player is still alive with their original inventory,
            // matches them, and immediately clears the newly frozen cache slots.
            if (!client.player.isAlive() || client.player.getHealth() <= 0f 
                    || client.currentScreen instanceof net.minecraft.client.gui.screen.DeathScreen) {
                return;
            }

            // Sync cache with current inventory (if item already matches and count is sufficient)
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
            // 2. No other screens (chest, anvil, etc.) are open
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
                        } else if (currentTarget != null && !currentTarget.isEmpty() && currentTarget.getItem() != cached.getItem()) {
                            // Target slot is blocked by a different item. Look for a source slot containing the correct (cached) item
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

                                            log("Swapping occupying item " + currentTarget.getItem() + " in slot " + targetSlotId 
                                                    + " with correct item " + sourceStack.getItem() + " from slot " + sourceSlotId);

                                            // 1. Pick up correct item from source
                                            client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);

                                            // 2. Place correct item in target and pick up occupying item
                                            client.interactionManager.clickSlot(syncId, targetHandlerId, 0, SlotActionType.PICKUP, client.player);

                                            // 3. Place occupying item in source slot (which is now empty)
                                            client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);

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
