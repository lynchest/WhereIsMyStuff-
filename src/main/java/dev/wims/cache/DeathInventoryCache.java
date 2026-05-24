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

    private DeathInventoryCache() {
        // Prevent instantiation
    }

    /**
     * Captures and copies non-empty item stacks from the player's inventory (slots 0-40).
     *
     * @param inventory the player's inventory to capture
     */
    public static void capture(PlayerInventory inventory) {
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
     */
    public static void updatePreDeath(PlayerInventory inventory) {
        if (inventory == null) return;
        boolean hasItems = false;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                PRE_DEATH_CACHE.put(slotId, stack.copy());
                hasItems = true;
            } else {
                PRE_DEATH_CACHE.remove(slotId);
            }
        }
        if (hasItems) {
            preDeathActive = true;
        }
    }

    /**
     * Captures items from the pre-death cache into the active death cache.
     */
    public static void captureFromPreDeath() {
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
