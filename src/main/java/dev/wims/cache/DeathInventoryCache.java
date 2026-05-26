package dev.wims.cache;

import dev.wims.WimsMod;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<Integer, ItemStack> CACHE = new HashMap<>();

    /** Rolling history of snapshots, keeping the last 3 ticks of inventory data to prevent death-sequence corruption. */
    private static final java.util.LinkedList<Map<Integer, ItemStack>> SNAPSHOT_HISTORY = new java.util.LinkedList<>();

    /** Active fading item cache for smooth slot removal animations. */
    private static final Map<Integer, ItemStack> FADING_CACHE = new HashMap<>();

    /** Fading alphas for active slot recovery animations. */
    private static final Map<Integer, Float> FADE_ALPHAS = new HashMap<>();

    private DeathInventoryCache() {
        // Prevent instantiation
    }

    // ──────────────────────────────────────────────────────────
    //  Snapshot — rolling backup (updated every alive tick)
    // ──────────────────────────────────────────────────────────

    /**
     * Takes a deep-copy snapshot of the player's current inventory.
     * Called every tick while the player is alive.
     *
     * @param inventory the player's inventory
     */
    public static void saveSnapshot(PlayerInventory inventory) {
        if (inventory == null) return;

        // 1. Scan and collect the current inventory items without copying first (to avoid GC allocations)
        // We check if the current layout differs from the latest snapshot in the history.
        Map<Integer, ItemStack> latestSnapshot = SNAPSHOT_HISTORY.isEmpty() ? null : SNAPSHOT_HISTORY.getLast();
        boolean hasChanged = latestSnapshot == null;

        if (!hasChanged) {
            // Compare current inventory with latest snapshot
            for (int slotId = 0; slotId <= 40; slotId++) {
                ItemStack current = inventory.getStack(slotId);
                ItemStack cached = latestSnapshot.get(slotId);

                boolean currentEmpty = current == null || current.isEmpty();
                boolean cachedEmpty = cached == null || cached.isEmpty();

                if (currentEmpty != cachedEmpty) {
                    hasChanged = true;
                    break;
                }

                if (!currentEmpty) {
                    // Compare item, count, and NBT/components safely
                    if (current == null || cached == null || current.getItem() != cached.getItem() 
                            || current.getCount() != cached.getCount() 
                            || !ItemStack.areEqual(current, cached)) {
                        hasChanged = true;
                        break;
                    }
                }
            }
        }

        // If nothing has changed, push the latest snapshot map reference to maintain queue size without GC overhead
        if (!hasChanged) {
            SNAPSHOT_HISTORY.addLast(latestSnapshot);
            if (SNAPSHOT_HISTORY.size() > 3) {
                SNAPSHOT_HISTORY.removeFirst();
            }
            return;
        }

        // 2. Perform deep copy since a change was detected
        Map<Integer, ItemStack> newSnapshot = new HashMap<>();
        for (int slotId = 0; slotId <= 40; slotId++) {
            ItemStack stack = inventory.getStack(slotId);
            if (stack != null && !stack.isEmpty()) {
                newSnapshot.put(slotId, stack.copy());
            }
        }

        if (newSnapshot.isEmpty()) {
            // Inventory is empty but player appears alive — don't overwrite/add empty snapshots
            return;
        }

        SNAPSHOT_HISTORY.addLast(newSnapshot);
        if (SNAPSHOT_HISTORY.size() > 3) {
            SNAPSHOT_HISTORY.removeFirst();
        }
    }

    // ──────────────────────────────────────────────────────────
    //  Death trigger — freeze snapshot into active cache
    // ──────────────────────────────────────────────────────────

    /**
     * Freezes the current snapshot into the display cache.
     * Should be called exactly once when death is confirmed (e.g. DeathScreen opens).
     */
    public static void freezeSnapshot() {
        WimsMod.log("freezeSnapshot called. snapshotHistory size = " + SNAPSHOT_HISTORY.size());
        if (SNAPSHOT_HISTORY.isEmpty()) {
            WimsMod.log("freezeSnapshot: Snapshot history is empty — nothing to freeze.");
            return;
        }
        CACHE.clear();
        // Consume the oldest snapshot in our 3-tick history (which is pre-death)
        Map<Integer, ItemStack> oldestSnapshot = SNAPSHOT_HISTORY.getFirst();
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : oldestSnapshot.entrySet()) {
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
        ItemStack removed = CACHE.remove(slotId);
        if (removed != null && !removed.isEmpty()) {
            FADING_CACHE.put(slotId, removed);
            FADE_ALPHAS.put(slotId, 0.35f); // Start fading from standard ghost alpha (0.35)
        }
    }

    /**
     * Ticks down the alpha values of fading slots to animate slot recovery.
     */
    public static void tickFade() {
        if (FADE_ALPHAS.isEmpty()) return;
        java.util.Iterator<Map.Entry<Integer, Float>> iterator = FADE_ALPHAS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Float> entry = iterator.next();
            float nextAlpha = entry.getValue() - 0.05f; // Dissolve by 0.05 per tick (~7 ticks total fade)
            if (nextAlpha <= 0f) {
                iterator.remove();
                FADING_CACHE.remove(entry.getKey());
            } else {
                entry.setValue(nextAlpha);
            }
        }
    }

    /**
     * Retrieves the fading item stack for slot recovery animation.
     */
    public static ItemStack getFading(int slotId) {
        return FADING_CACHE.getOrDefault(slotId, ItemStack.EMPTY);
    }

    /**
     * Retrieves the current fade alpha value for slot recovery animation.
     */
    public static float getFadeAlpha(int slotId) {
        return FADE_ALPHAS.getOrDefault(slotId, 0f);
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
     * Resets the entire cache and rolling snapshot to a clean state.
     */
    public static void reset() {
        CACHE.clear();
        SNAPSHOT_HISTORY.clear();
        FADING_CACHE.clear();
        FADE_ALPHAS.clear();
        WimsMod.ghostRenderStates.clear();
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
