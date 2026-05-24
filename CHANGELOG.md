# Changelog

All notable changes to the WhereIsMyStuff? (WIMS) mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
