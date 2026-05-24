package dev.wims.cache;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static client-side cache storing the player's inventory snapshot upon death.
 */
public final class DeathInventoryCache {
    private static final Map<Integer, ItemStack> CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, ItemStack> PRE_DEATH_CACHE = new ConcurrentHashMap<>();
    private static boolean preDeathActive = false;
    private static boolean capturedThisDeath = false;

    private DeathInventoryCache() {
        // Prevent instantiation
    }

    /**
     * Checks if a capture has already been successfully stored for the current death.
     *
     * @return true if already captured, false otherwise
     */
    public static boolean isCapturedThisDeath() {
        return capturedThisDeath;
    }

    /**
     * Captures and copies non-empty item stacks from the player's inventory (slots 0-40).
     *
     * @param inventory the player's inventory to capture
     */
    public static void capture(PlayerInventory inventory) {
        if (capturedThisDeath) {
            System.out.println("[WIMS DEBUG] capture: Already captured this death. Skipping.");
            return;
        }
        clearAll();
        if (inventory == null) {
            System.out.println("[WIMS DEBUG] capture called with null inventory!");
            return;
        }
        System.out.println("[WIMS DEBUG] capture started.");
        int capturedCount = 0;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                CACHE.put(slotId, stack.copy());
                System.out.println("[WIMS DEBUG] CACHED Slot " + slotId + ": " + stack);
                capturedCount++;
            }
        }
        capturedThisDeath = true;
        System.out.println("[WIMS DEBUG] capture finished. Total slots captured: " + capturedCount);
    }

    /**
     * Clears the cached item stack at the given slot ID.
     *
     * @param slotId the inventory slot to clear
     */
    public static void clearSlot(int slotId) {
        CACHE.remove(slotId);
    }

    /**
     * Checks if the cache contains a valid non-empty item stack for the given slot ID.
     *
     * @param slotId the inventory slot to check
     * @return true if a stack exists in the cache, false otherwise
     */
    public static boolean has(int slotId) {
        ItemStack stack = CACHE.get(slotId);
        return stack != null && !stack.isEmpty();
    }

    /**
     * Retrieves the cached item stack for the given slot ID.
     *
     * @param slotId the inventory slot to retrieve
     * @return the cached ItemStack, or ItemStack.EMPTY if empty/not present
     */
    public static ItemStack get(int slotId) {
        return CACHE.getOrDefault(slotId, ItemStack.EMPTY);
    }

    /**
     * Clears all cached items.
     */
    public static void clearAll() {
        CACHE.clear();
    }

    /**
     * Checks if the cache is currently empty.
     *
     * @return true if there are no cached items, false otherwise
     */
    public static boolean isEmpty() {
        return CACHE.isEmpty();
    }

    /**
     * Updates the pre-death cache with the player's current inventory if they are alive.
     *
     * @param inventory the player's inventory to backup
     * @param isHurt true if the player took damage recently (hurtTime > 0)
     */
    public static void updatePreDeath(PlayerInventory inventory, boolean isHurt) {
        if (inventory == null) return;

        // Reset the capture flag since the player is alive and inventory backup is active
        if (capturedThisDeath) {
            System.out.println("[WIMS DEBUG] Player is alive and backing up inventory. Resetting capturedThisDeath.");
            capturedThisDeath = false;
        }

        boolean hasItems = false;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        // If the current inventory is completely empty, but we previously had items:
        // ONLY protect it if the player was hurt/damaged recently or is dead/dying.
        // This avoids the edge case where a player legitimately empties their inventory in safety.
        if (!hasItems && preDeathActive && !PRE_DEATH_CACHE.isEmpty()) {
            if (isHurt) {
                System.out.println("[WIMS DEBUG] updatePreDeath: Current inventory is empty and player is hurt. Protecting pre-death cache.");
                return;
            }
        }

        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                PRE_DEATH_CACHE.put(slotId, stack.copy());
            } else {
                PRE_DEATH_CACHE.remove(slotId);
            }
        }
        preDeathActive = hasItems;
    }

    /**
     * Captures items from the pre-death cache into the active death cache.
     */
    public static void captureFromPreDeath() {
        if (capturedThisDeath) {
            System.out.println("[WIMS DEBUG] captureFromPreDeath: Already captured this death. Skipping.");
            return;
        }
        if (!preDeathActive) {
            System.out.println("[WIMS DEBUG] captureFromPreDeath called but pre-death cache is not active!");
            return;
        }
        clearAll();
        System.out.println("[WIMS DEBUG] captureFromPreDeath started.");
        int capturedCount = 0;
        for (Map.Entry<Integer, ItemStack> entry : PRE_DEATH_CACHE.entrySet()) {
            CACHE.put(entry.getKey(), entry.getValue().copy());
            System.out.println("[WIMS DEBUG] CACHED (Pre-Death) Slot " + entry.getKey() + ": " + entry.getValue());
            capturedCount++;
        }
        preDeathActive = false;
        PRE_DEATH_CACHE.clear();
        capturedThisDeath = true;
        System.out.println("[WIMS DEBUG] captureFromPreDeath finished. Total slots captured: " + capturedCount);
    }

    /**
     * Checks if the pre-death cache has active items available to capture.
     *
     * @return true if pre-death cache is active, false otherwise
     */
    public static boolean hasPreDeath() {
        return preDeathActive;
    }
}
