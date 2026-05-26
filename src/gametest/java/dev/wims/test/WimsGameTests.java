package dev.wims.test;

import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import dev.wims.cache.DeathInventoryCache;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WimsGameTests {

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testCacheLogic(TestContext helper) {
        // Obtain PlayerInventory from a mock player to avoid version-dependent constructor differences
        PlayerInventory inventory = helper.createMockPlayer(net.minecraft.world.GameMode.SURVIVAL).getInventory();
        
        // Put 5 diamonds in slot 9
        ItemStack diamondStack = new ItemStack(Items.DIAMOND, 5);
        inventory.setStack(9, diamondStack);
        
        // Save snapshot
        DeathInventoryCache.reset();
        DeathInventoryCache.saveSnapshot(inventory);
        
        // Verify rolling snapshot worked
        DeathInventoryCache.freezeSnapshot();
        assertTrue(DeathInventoryCache.has(9), "Cache should have item at slot 9");
        assertTrue(DeathInventoryCache.get(9).getItem() == Items.DIAMOND, "Item should be Diamond");
        assertTrue(DeathInventoryCache.get(9).getCount() == 5, "Count should be 5");
        
        // Clear slot
        DeathInventoryCache.clearSlot(9);
        assertFalse(DeathInventoryCache.has(9), "Cache should be cleared at slot 9");
        
        // Verify it was moved to fading cache
        assertTrue(DeathInventoryCache.getFading(9).getItem() == Items.DIAMOND, "Fading cache should have Diamond");
        assertTrue(DeathInventoryCache.getFadeAlpha(9) == 0.35f, "Fade alpha should start at 0.35");
        
        // Mark test as succeeded
        helper.complete();
    }

    @GameTest(templateName = "fabric-gametest-api-v1:empty")
    public void testCreativeModeBypass(TestContext helper) {
        // Obtain PlayerInventory from a mock player in CREATIVE mode
        PlayerInventory inventory = helper.createMockPlayer(net.minecraft.world.GameMode.CREATIVE).getInventory();
        
        // Put 5 diamonds in slot 9
        ItemStack diamondStack = new ItemStack(Items.DIAMOND, 5);
        inventory.setStack(9, diamondStack);
        
        // Try to save snapshot
        DeathInventoryCache.reset();
        DeathInventoryCache.saveSnapshot(inventory);
        
        // Verify no snapshot was saved
        DeathInventoryCache.freezeSnapshot();
        assertFalse(DeathInventoryCache.has(9), "Cache should NOT have item at slot 9 for creative player");
        assertTrue(DeathInventoryCache.isEmpty(), "Cache should be empty for creative player");
        
        // Mark test as succeeded
        helper.complete();
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new RuntimeException(message);
        }
    }
}
