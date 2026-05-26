package dev.wims;

import dev.wims.cache.DeathInventoryCache;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class WimsModClient implements ClientModInitializer {
    private static int autoRestoreCooldown = 0;
    private static KeyBinding clearCacheKey;

    @Override
    public void onInitializeClient() {
        WimsMod.log("WIMS Client Initialized!");

        // Register the keybind to clear ghost slots manually (DELETE key)
        clearCacheKey = KeyBindingHelper.registerKeyBinding(createKeyBinding(
            "key.whereismystuff.clear",
            GLFW.GLFW_KEY_DELETE,
            "category.whereismystuff"
        ));

        // Clear cache and snapshots on disconnect to prevent carrying ghosts across different servers/worlds
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            WimsMod.log("Disconnected from server. Resetting inventory cache.");
            DeathInventoryCache.reset();
            autoRestoreCooldown = 0;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Update recovery fade-out animations every tick
            DeathInventoryCache.tickFade();

            if (client.player == null) {
                autoRestoreCooldown = 0;
                return;
            }

            // Check if keybind was pressed to manually clear cache
            while (clearCacheKey.wasPressed()) {
                if (!DeathInventoryCache.isEmpty()) {
                    DeathInventoryCache.reset();
                    client.player.sendMessage(net.minecraft.text.Text.translatable("chat.whereismystuff.cleared"), true);
                    client.player.playSound(net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 0.45f, 0.5f);
                }
            }

            // Save inventory snapshot every tick while the player is alive and healthy.
            // Rolling history queue in DeathInventoryCache handles death-sequence filtering.
            if (client.player.isAlive() && client.player.getHealth() > 0f) {
                DeathInventoryCache.saveSnapshot(client.player.getInventory());
            }

            // Nothing to do if no ghost items are cached
            if (DeathInventoryCache.isEmpty()) {
                return;
            }

            // Skip syncing while the player is dead or on the death screen.
            if (!client.player.isAlive() || client.player.getHealth() <= 0f 
                    || client.currentScreen instanceof net.minecraft.client.gui.screen.DeathScreen) {
                return;
            }

            // Sync cache with current inventory
            boolean hadItemsBefore = !DeathInventoryCache.isEmpty();
            PlayerInventory inventory = client.player.getInventory();
            boolean anyCleared = false;
            for (int slotId = 0; slotId <= 40; slotId++) {
                if (DeathInventoryCache.has(slotId)) {
                    ItemStack current = inventory.getStack(slotId);
                    ItemStack cached = DeathInventoryCache.get(slotId);

                    if (current != null && !current.isEmpty() 
                            && current.getItem() == cached.getItem() 
                            && current.getCount() >= cached.getCount()) {
                        DeathInventoryCache.clearSlot(slotId);
                        anyCleared = true;
                    }
                }
            }

            if (anyCleared) {
                client.player.playSound(net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, 0.4f, 1.6f);

                if (DeathInventoryCache.isEmpty() && hadItemsBefore) {
                    client.player.playSound(net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.8f);
                    client.player.sendMessage(net.minecraft.text.Text.translatable("chat.whereismystuff.recovered"), true);
                }
            }

            if (DeathInventoryCache.isEmpty()) {
                return;
            }

            if (autoRestoreCooldown > 0) {
                autoRestoreCooldown--;
                return;
            }

            if (client.player.isAlive() 
                    && (client.currentScreen == null || client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen)
                    && client.player.currentScreenHandler == client.player.playerScreenHandler
                    && client.player.currentScreenHandler.getCursorStack().isEmpty()
                    && client.interactionManager != null) {

                Map<net.minecraft.item.Item, Integer> itemToSourceSlot = new HashMap<>();
                for (int slotId = 0; slotId <= 40; slotId++) {
                    ItemStack stack = inventory.getStack(slotId);
                    if (stack == null || stack.isEmpty()) {
                        continue;
                    }

                    boolean isValidSource = false;
                    if (!DeathInventoryCache.has(slotId)) {
                        isValidSource = true;
                    } else {
                        ItemStack cached = DeathInventoryCache.get(slotId);
                        if (stack.getItem() != cached.getItem()) {
                            isValidSource = true;
                        }
                    }

                    if (isValidSource) {
                        itemToSourceSlot.putIfAbsent(stack.getItem(), slotId);
                    }
                }

                boolean actionTaken = false;
                for (int targetSlotId = 0; targetSlotId <= 40; targetSlotId++) {
                    if (DeathInventoryCache.has(targetSlotId)) {
                        ItemStack cached = DeathInventoryCache.get(targetSlotId);
                        ItemStack currentTarget = inventory.getStack(targetSlotId);

                        if (currentTarget == null || currentTarget.isEmpty() 
                                || (currentTarget.getItem() == cached.getItem() && currentTarget.getCount() < cached.getCount())) {

                            Integer sourceSlotId = itemToSourceSlot.get(cached.getItem());
                            if (sourceSlotId != null) {
                                int sourceHandlerId = getPlayerScreenHandlerSlotId(sourceSlotId);
                                int targetHandlerId = getPlayerScreenHandlerSlotId(targetSlotId);

                                if (sourceHandlerId != -1 && targetHandlerId != -1) {
                                    int syncId = client.player.currentScreenHandler.syncId;

                                    client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);
                                    client.interactionManager.clickSlot(syncId, targetHandlerId, 0, SlotActionType.PICKUP, client.player);

                                    if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                                        client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);
                                    }

                                    actionTaken = true;
                                    break;
                                }
                            }
                        } else if (currentTarget != null && !currentTarget.isEmpty() && currentTarget.getItem() != cached.getItem()) {
                            Integer sourceSlotId = itemToSourceSlot.get(cached.getItem());
                            if (sourceSlotId != null) {
                                int sourceHandlerId = getPlayerScreenHandlerSlotId(sourceSlotId);
                                int targetHandlerId = getPlayerScreenHandlerSlotId(targetSlotId);

                                if (sourceHandlerId != -1 && targetHandlerId != -1) {
                                    int syncId = client.player.currentScreenHandler.syncId;

                                    WimsMod.log("Swapping occupying item " + currentTarget.getItem() + " in slot " + targetSlotId 
                                             + " with correct item " + cached.getItem() + " from slot " + sourceSlotId);

                                    client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);
                                    client.interactionManager.clickSlot(syncId, targetHandlerId, 0, SlotActionType.PICKUP, client.player);
                                    client.interactionManager.clickSlot(syncId, sourceHandlerId, 0, SlotActionType.PICKUP, client.player);

                                    actionTaken = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (actionTaken) {
                        autoRestoreCooldown = 5;
                        break;
                    }
                }
            }
        });
    }

    private static int getPlayerScreenHandlerSlotId(int playerInventorySlot) {
        if (playerInventorySlot >= 0 && playerInventorySlot <= 8) {
            return 36 + playerInventorySlot;
        } else if (playerInventorySlot >= 9 && playerInventorySlot <= 35) {
            return playerInventorySlot;
        } else if (playerInventorySlot >= 36 && playerInventorySlot <= 39) {
            return 44 - playerInventorySlot;
        } else if (playerInventorySlot == 40) {
            return 45;
        }
        return -1;
    }

    private static KeyBinding createKeyBinding(String translationKey, int code, String categoryName) {
        try {
            for (java.lang.reflect.Constructor<?> constructor : KeyBinding.class.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 4 
                        && parameterTypes[0] == String.class 
                        && parameterTypes[1] == net.minecraft.client.util.InputUtil.Type.class 
                        && parameterTypes[2] == int.class) {
                    
                    Class<?> categoryClass = parameterTypes[3];
                    if (categoryClass == String.class) {
                        return (KeyBinding) constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, categoryName);
                    } else {
                        Object categoryObj = null;
                        
                        // Try static factory methods by signature (independent of remapped names)
                        for (java.lang.reflect.Method method : categoryClass.getDeclaredMethods()) {
                            if (java.lang.reflect.Modifier.isStatic(method.getModifiers()) 
                                    && method.getReturnType() == categoryClass) {
                                Class<?>[] pTypes = method.getParameterTypes();
                                if (pTypes.length == 1) {
                                    if (pTypes[0] == String.class) {
                                        method.setAccessible(true);
                                        categoryObj = method.invoke(null, categoryName);
                                        break;
                                    } else if (pTypes[0] == net.minecraft.util.Identifier.class) {
                                        method.setAccessible(true);
                                        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of("whereismystuff", "clear");
                                        categoryObj = method.invoke(null, id);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        // Try constructors if static factory method was not found
                        if (categoryObj == null) {
                            for (java.lang.reflect.Constructor<?> catConstr : categoryClass.getDeclaredConstructors()) {
                                Class<?>[] catPTypes = catConstr.getParameterTypes();
                                if (catPTypes.length == 1) {
                                    if (catPTypes[0] == String.class) {
                                        catConstr.setAccessible(true);
                                        categoryObj = catConstr.newInstance(categoryName);
                                        break;
                                    } else if (catPTypes[0] == net.minecraft.util.Identifier.class) {
                                        catConstr.setAccessible(true);
                                        net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of("whereismystuff", "clear");
                                        categoryObj = catConstr.newInstance(id);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (categoryObj != null) {
                            return (KeyBinding) constructor.newInstance(translationKey, InputUtil.Type.KEYSYM, code, categoryObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to find or instantiate compatible KeyBinding constructor");
    }
}
