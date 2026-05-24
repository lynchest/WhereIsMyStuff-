# WIMS — WhereIsMyStuff? — Architecture

## Overview

WIMS is a **client-side only** Fabric mod for Minecraft Java Edition. It requires no server installation and works on vanilla/Hypixel/Aternos servers out of the box. The mod captures the player's inventory at the moment of death, then renders ghost (transparent) icons in the corresponding empty slots until those items are recovered.

---

## Project Structure

```
wims/
├── src/main/java/dev/wims/
│   ├── WimsMod.java               # Mod entrypoint
│   ├── cache/
│   │   └── DeathInventoryCache.java   # Singleton; holds Map<Integer, ItemStack>
│   ├── mixin/
│   │   ├── PlayerDeathMixin.java      # Captures inventory on death
│   │   ├── HandledScreenMixin.java    # Renders ghost items in empty slots
│   │   └── InventorySyncMixin.java    # Clears cache as items are recovered
│   └── util/
│       └── ItemStackHelper.java       # Clone / compare helpers
├── src/main/resources/
│   ├── fabric.mod.json
│   └── wims.mixins.json
└── build.gradle
```

---

## Component Breakdown

### 1. `DeathInventoryCache` (Singleton)

Central in-memory state store. Lives for the duration of the game session.

```java
public class DeathInventoryCache {
    private static final Map<Integer, ItemStack> cache = new HashMap<>();

    public static void capture(PlayerInventory inventory) { ... }   // slots 0–40
    public static void clearSlot(int slotId) { ... }
    public static boolean has(int slotId) { ... }
    public static ItemStack get(int slotId) { ... }
    public static void clearAll() { ... }
    public static boolean isEmpty() { ... }
}
```

**Rules:**
- Only one death is stored at a time; a new death overwrites the previous one via `clearAll()` then `capture()`.
- `ItemStack` values are **cloned** (`itemStack.copy()`) at capture time to prevent mutation.

---

### 2. `PlayerDeathMixin` — Data Capture

**Target:** `ClientPlayerEntity` — detect when the player's health reaches 0.

**Approach (1.21.4):**

Inject into `LivingEntity.onDeath(DamageSource source)` and filter for the local player:
```java
if (!(entity instanceof ClientPlayerEntity)) return;
if (MinecraftClient.getInstance().player != entity) return;
```

**Logic:**
```
onDeath detected →
    DeathInventoryCache.clearAll()
    DeathInventoryCache.capture(player.getInventory())
```

---

### 3. `HandledScreenMixin` — Ghost Rendering

**Target:** `HandledScreen.drawSlot(DrawContext context, Slot slot)`  
**Inject at:** `@At("HEAD")`

```java
@Inject(method = "drawSlot", at = @At("HEAD"))
private void renderGhostItem(DrawContext context, Slot slot, CallbackInfo ci) {
    if (!slot.hasStack() && DeathInventoryCache.has(slot.id)) {
        ItemStack ghost = DeathInventoryCache.get(slot.id);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.35f);
        context.drawItem(ghost, slot.x, slot.y);
        context.drawItemInSlot(client.textRenderer, ghost, slot.x, slot.y);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // always reset
    }
}
```

**Scope:** Applies to `InventoryScreen` (player inventory). Hotbar slots (0–8) are also covered as they share slot IDs.

---

### 4. `InventorySyncMixin` — Cache Clearing

**Target:** `ClientPlayNetworkHandler` — intercept `ScreenHandlerSlotUpdateS2CPacket` or listen via `ClientTickEvents`.

**Logic per tick / per packet:**
```
for each slot in player.inventory:
    if DeathInventoryCache.has(slot.id):
        ItemStack current = player.inventory.getStack(slot.id)
        ItemStack cached  = DeathInventoryCache.get(slot.id)

        if current.getItem() == cached.getItem()
            AND current.getCount() >= cached.getCount():
                DeathInventoryCache.clearSlot(slot.id)
```

This handles **partial recovery**: if the player had 5 diamonds and picks up only 3, the ghost remains until all 5 are recovered.

---

## Data Flow Diagram

```
[Player dies]
      │
      ▼
PlayerDeathMixin
   clearAll() → capture(inventory[0..40])
      │
      ▼
DeathInventoryCache
   Map<slotId, ItemStack (cloned)>
      │
      ├──────────────────────────────────────────┐
      ▼                                          ▼
HandledScreenMixin                     InventorySyncMixin
  (every frame, open inventory)          (every tick / packet)
  if slot empty && cache.has(id)         if current >= cached:
    → draw ghost @ 35% alpha               → clearSlot(id)
```

---

## Slot ID Reference

| Range | Contents |
|-------|----------|
| 0     | Offhand  |
| 1–4   | Armor (feet → head) |
| 5–8   | Crafting grid |
| 9–35  | Main inventory |
| 36–44 | Hotbar |

> **Note:** Fabric's `PlayerInventory` uses `getMain()` (0–35) and `offHand` separately. Map accordingly in `capture()`.

---

## Target Version

**Primary target: Minecraft 1.21.4**  
Multi-version support (1.20.x backport) is a post-release task. Do not attempt it before v0.1.0 is published.

```properties
# gradle.properties — exact versions for 1.21.4
minecraft_version=1.21.4
yarn_mappings=1.21.4+build.8
loader_version=0.16.9
fabric_version=0.110.0+1.21.4
java_version=21
```

```gradle
// build.gradle — dependencies block
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
}
```

No external libraries required. Pure Fabric API + Mixin.

> **Java version:** The project requires **Java 21**. Minecraft 1.21.x does not run on Java 17.

---

## Key Constraints

- **Client-side only:** `fabric.mod.json` must declare `"environment": "client"` for all mixins.
- **No persistent storage:** Cache is in-memory only; does not survive game restarts (acceptable for MVP).
- **Alpha rendering:** `RenderSystem.setShaderColor` alpha only works if the item shader supports it. Test against default Minecraft item renderer; may need `RenderSystem.enableBlend()` / `RenderSystem.defaultBlendFunc()` before drawing.
- **Thread safety:** All cache reads/writes occur on the render/game thread. No async concerns for MVP.
