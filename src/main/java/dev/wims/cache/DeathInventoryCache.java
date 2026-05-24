package dev.wims.cache;

import dev.wims.WimsMod;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static client-side cache storing the player's inventory snapshot upon death.
 *
 * <p>The design is intentionally simple:
 * <ul>
 *   <li>{@code SNAPSHOT} — Updated every tick while the player is alive. A rolling backup.</li>
 *   <li>{@code CACHE} — Frozen copy of SNAPSHOT, set when death is detected (DeathScreen opens).
 *       This is what ghost items are rendered from.</li>
 * </ul>
 */
public final class DeathInventoryCache {
    /** The active ghost-item cache rendered on-screen. */
    private static final Map<Integer, ItemStack> CACHE = new ConcurrentHashMap<>();

    /** Rolling snapshot of the player's inventory, updated every tick while alive. */
    private static final Map<Integer, ItemStack> SNAPSHOT = new ConcurrentHashMap<>();

    /** True if the SNAPSHOT has at least one item. */
    private static boolean snapshotActive = false;

    private DeathInventoryCache() {
        // Prevent instantiation
    }

    // ──────────────────────────────────────────────────────────
    //  Snapshot — rolling backup (updated every alive tick)
    // ──────────────────────────────────────────────────────────

    /**
     * Takes a deep-copy snapshot of the player's current inventory.
     * Called every tick while the player is alive and hasn't taken damage recently.
     *
     * @param inventory the player's inventory
     */
    public static void saveSnapshot(PlayerInventory inventory) {
        if (inventory == null) return;

        // Count items FIRST — if empty, don't touch the snapshot.
        // This prevents the race condition where slot-clear packets arrive
        // before the health-update packet in a different network batch.
        boolean hasItems = false;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            // Inventory is empty but player appears alive — don't overwrite.
            // The snapshot from the last tick with items is still valid.
            WimsMod.log("saveSnapshot: Inventory is empty, skipping snapshot update to protect previous.");
            return;
        }

        SNAPSHOT.clear();
        int count = 0;
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                SNAPSHOT.put(slotId, stack.copy());
                count++;
            }
        }
        snapshotActive = true;
        WimsMod.log("saveSnapshot: Saved " + count + " slots. active = " + snapshotActive);
    }

    // ──────────────────────────────────────────────────────────
    //  Death trigger — freeze snapshot into active cache
    // ──────────────────────────────────────────────────────────

    /**
     * Freezes the current snapshot into the display cache.
     * Should be called exactly once when death is confirmed (e.g. DeathScreen opens).
     */
    public static void freezeSnapshot() {
        WimsMod.log("freezeSnapshot called. snapshotActive = " + snapshotActive + ", snapshot size = " + SNAPSHOT.size());
        if (!snapshotActive) {
            WimsMod.log("freezeSnapshot: Snapshot is empty — nothing to freeze.");
            return;
        }
        CACHE.clear();
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : SNAPSHOT.entrySet()) {
            CACHE.put(entry.getKey(), entry.getValue().copy());
            count++;
        }
        WimsMod.log("freezeSnapshot: Frozen " + count + " slots into display cache.");
    }

    // ──────────────────────────────────────────────────────────
    //  Cache accessors — used by rendering & recovery logic
    // ──────────────────────────────────────────────────────────

    /**
     * Clears the cached item stack at the given slot ID.
     *
     * @param slotId the inventory slot to clear
     */
    public static void clearSlot(int slotId) {
        WimsMod.log("clearSlot: Cleared slot " + slotId);
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
