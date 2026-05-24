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
        if (inventory == null) return;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                CACHE.put(slotId, stack.copy());
            }
        }
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
}
