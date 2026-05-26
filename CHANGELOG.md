# Changelog

All notable changes to the WhereIsMyStuff? (WIMS) mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.2] - 2026-05-26

### Fixed
- **Ghost Item Invisibility:** Fixed a bug where ghost items were completely invisible on the inventory and container screens by reverting the rendering enjjection from `drawForeground` (which is overridden by screen subclasses) back to separate, cross-version safe `drawSlot` dual-injection hooks (`method_2385`) for 1.21.4 and 1.21.11.
- **1.21.11 Launch Crash:** Fixed a `ClassCastException` crash during startup on Minecraft 1.21.11 by replacing class-loading class checks with non-classloading `ClassLoader.getResource` classpath checks inside `WimsMixinConfigPlugin`.
- **1.21.6 Compilation:** Fixed compilation failure on 1.21.6 caused by mapping differences in `DrawContext.draw` by introducing dynamic reflection-based execution.

### Added
- **Compatibility Verification:** Confirmed and verified full runtime stability on Minecraft versions `1.21.4`, `1.21.8`, `1.21.9`, `1.21.10`, and `1.21.11` via manual tests and automated builds.

## [0.1.1] - 2026-05-25

### Optimized
- **Auto-Restore Complexity:** Optimized the auto-restore item relocation lookup from $O(n^2)$ down to $O(n)$ by pre-indexing valid source slots in a local map.
- **HashMap Promotion:** Replaced `ConcurrentHashMap` with standard non-synchronized `HashMap` in `DeathInventoryCache` since all caching and rendering runs entirely on Minecraft's main client thread.
- **Single-Pass Inventory Snapshots:** Combined item-existence check and deep copy iteration in `saveSnapshot` into a single loop pass to reduce per-tick lookup cost.
- **Render Layer Caching:** Added a `WeakHashMap` cache for the `ItemRendererMixin` getBuffer redirection to avoid costly string creation (`toString().toLowerCase()`) and character searching during the hot rendering path.
- **Shadowed client field:** Shared the existing `client` field from `Screen` class in `HandledScreenMixin` to avoid redundant static calls to `MinecraftClient.getInstance()` during slot drawing.

### Removed
- **Cooldown Log Spam:** Disabled the tick-based console/disk log outputs that printed on every tick during the damage/death protection cooldown.

## [0.1.0] - 2026-05-24

### Added
- **Core Death Caching System (`DeathInventoryCache`):** Event-driven client-side caching of player inventory slots 0-40 (hotbar, inventory, offhand, armor) immediately upon death.
- **Visual Ghost Rendering:** 35% translucent representation of cached items inside empty inventory slots.
- **ItemRendererMixin:** Special custom translucent blending redirection to ensure block models and 3D meshes in inventory slots render transparently rather than opaque.
- **Smart Slot Routing & Sync (`InventorySyncMixin`):** Automated recovery that maps picked-up items back to their original death slots.
- **Partial Stack Recovery Support:** Ghost elements remain active with updated counts if items are only partially recovered.
- **Dual-Phase Death Protection:** Secure race-condition handling utilizing snapshot protection alongside specialized death screen interceptors.
- **Vanilla Server Compatibility:** 100% client-side implementation which runs seamlessly on vanilla servers, realms, and LAN.
- **Asset Integration:** Premium mod icon (`icon.png` 512x512) and README showcasing screenshots and design.
- **Featherweight Footprint Optimization:** Extremely lightweight code and pixel-perfect Nearest Neighbor color asset compression resulting in a tiny ~57 KB compiled jar.
