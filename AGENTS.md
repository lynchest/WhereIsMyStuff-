# WIMS — Agent Coding Instructions

## Role

You are a Fabric mod developer. Your task is to implement the **WhereIsMyStuff? (WIMS)** mod exactly as described in `ARCHITECTURE.md`. This is a client-side QoL mod that renders ghost (transparent) inventory icons after the player dies, until items are recovered.

---

## Target

**Minecraft 1.21.4 — Fabric — Java 21**

Multi-version support is explicitly out of scope until after v0.1.0 is released on Modrinth.

---

## Absolute Rules

- **Never add server-side code.** Every class, mixin, and event must be client-side only.
- **Never use deprecated Fabric API methods.** Check current Yarn mappings for the target MC version.
- **Always clone ItemStack on capture:** use `itemStack.copy()`. Never store references.
- **Always reset RenderSystem color** after drawing ghost items: `RenderSystem.setShaderColor(1f, 1f, 1f, 1f)`.
- **Never write to disk.** DeathInventoryCache is in-memory only.
- **Do not add config files or config screens** for MVP. All constants are hardcoded.
- **Ghost opacity is fixed at 0.35f.** Do not parameterize it.
- **Always update [MEMORIES.md](file:///Users/lynchest/Desktop/WhereIsMyStuff?/MEMORIES.md) after every CRITICAL change.** Follow the rule: Keep entries extremely brief ("What was done?" and "Where was affected?") with no comments or long explanations to maximize context efficiency.

---

## Implementation Order

Complete tasks in this exact order. Do not skip ahead.

### Step 1 — Project Scaffold

Use the official Fabric example mod template as a base: https://github.com/FabricMC/fabric-example-mod

Set `gradle.properties` to these **exact** versions:
```properties
minecraft_version=1.21.4
yarn_mappings=1.21.4+build.8
loader_version=0.16.9
fabric_version=0.110.0+1.21.4
java_version=21
mod_version=0.1.0
maven_group=dev.wims
archives_base_name=whereismystuff
```

Then:
1. Adapt `build.gradle` from the template (no major changes needed).
2. Generate `src/main/resources/fabric.mod.json` with:
   - `"environment": "client"`
   - Mixin config path pointing to `wims.mixins.json`
3. Generate `src/main/resources/wims.mixins.json` listing all three mixins.
4. Generate `WimsMod.java` entrypoint (implement `ModInitializer`, body can be empty for MVP).

### Step 2 — DeathInventoryCache

Implement `DeathInventoryCache.java` as a static utility class (no instantiation).

Required methods:
```
capture(PlayerInventory inventory)   // copies all non-empty stacks, slots 0-40
clearSlot(int slotId)
has(int slotId) → boolean
get(int slotId) → ItemStack
clearAll()
isEmpty() → boolean
```

### Step 3 — PlayerDeathMixin

Detect client-side player death and call `DeathInventoryCache.clearAll()` then `DeathInventoryCache.capture(...)`.

**Use this approach for 1.21.4:**
Inject into `LivingEntity.onDeath(DamageSource source)`. At the top of the injection, verify this is the local player and bail out otherwise:

```java
MinecraftClient client = MinecraftClient.getInstance();
if (client.player == null || (Object) this != client.player) return;
```

Do **not** use a tick-based health monitor — that adds unnecessary per-tick overhead and is an inferior solution.

### Step 4 — HandledScreenMixin

Inject into `HandledScreen.drawSlot(DrawContext, Slot)` at `HEAD`.

Draw logic:
1. Return early if `DeathInventoryCache.isEmpty()`.
2. Return early if `slot.hasStack()`.
3. Return early if `!DeathInventoryCache.has(slot.id)`.
4. Enable blend: `RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();`
5. Set color: `RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);`
6. Draw item and count label.
7. Reset color: `RenderSystem.setShaderColor(1f, 1f, 1f, 1f);`

### Step 5 — InventorySyncMixin

Listen for slot changes via `ClientTickEvents.END_CLIENT_TICK`.

Each tick:
- Skip if `DeathInventoryCache.isEmpty()`.
- Iterate over all slot IDs present in the cache.
- For each: get current stack from `player.getInventory()`. If item type matches AND count >= cached count → `clearSlot(id)`.

---

## Code Style

- Java only. No Kotlin, no Groovy.
- No Lombok, no external utility libraries.
- Keep classes small. Each mixin does exactly one thing.
- Use `@Environment(EnvType.CLIENT)` on all mixin classes.
- Add a brief Javadoc comment on each public method.
- Use `MinecraftClient.getInstance()` for accessing the client player; never store a reference to the player across ticks.

---

## Test Checklist

After implementation, verify against these scenarios before marking any step complete:

| # | Scenario | Expected Result |
|---|----------|-----------------|
| 1 | Player has 5 diamonds in slot 14, uses `/kill` | Slot 14 shows diamond ghost at 35% opacity |
| 2 | Player opens inventory before picking up items | Ghost visible in correct slot |
| 3 | Player picks up 3 of 5 diamonds | Ghost remains (partial recovery not enough) |
| 4 | Player picks up all 5 diamonds | Ghost disappears, slot looks vanilla |
| 5 | Player dies a second time with different items | Old cache overwritten, new ghosts shown |
| 6 | Player opens chest screen | No ghost items rendered (only player inventory) |

---

## Files To Create

```
src/main/java/dev/wims/WimsMod.java
src/main/java/dev/wims/cache/DeathInventoryCache.java
src/main/java/dev/wims/mixin/PlayerDeathMixin.java
src/main/java/dev/wims/mixin/HandledScreenMixin.java
src/main/java/dev/wims/mixin/InventorySyncMixin.java
src/main/resources/fabric.mod.json
src/main/resources/wims.mixins.json
build.gradle
gradle.properties
```

---

## Out of Scope (Do Not Implement)

- **Multi-version support (1.20.x, 1.19.x)** — backport happens after v0.1.0 ships
- Despawn timers or server sync
- Death history / GUI
- Shulker box sub-inventory tracking
- Animations or particle effects on ghost removal
- Config screen or keybind
- Persistence across game sessions
