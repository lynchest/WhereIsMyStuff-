# WIMS — Roadmap

## v0.1.0 — MVP · Minecraft 1.21.4 (Current Target)

**Goal:** Çalışan, Modrinth'e yüklenebilir bir mod. Başka hiçbir şey değil.

| Feature | Status |
|---------|--------|
| Client-side only operation | 🎯 In scope |
| Single death memory | 🎯 In scope |
| Ghost rendering at 35% alpha in inventory | 🎯 In scope |
| Item count / partial recovery tracking | 🎯 In scope |
| Works on vanilla servers (Hypixel, Aternos) | 🎯 In scope |
| Multi-version support | ❌ Not in scope |

---

## v0.1.1 — Backport · Minecraft 1.20.x

**Goal:** 1.20 → 1.20.4 arası versiyonlarda çalıştır. Yeni özellik yok.

**Yapılacaklar:**
- [ ] `backport/1.20` branch'i aç
- [ ] `gradle.properties` → 1.20.4 versiyonlarına çek
- [ ] Yarn mapping farklarını kontrol et (`HandledScreen.drawSlot` imzası)
- [ ] Build + test
- [ ] Modrinth'e 1.20.x için ayrı dosya yükle

> 1.19.x ve öncesi: `DrawContext` API'si yok, render kodu tamamen farklı. Bu sürümler roadmap'te yok.

---

**Goal:** Fix edge cases found after initial user feedback.

- [ ] Handle death in creative mode (skip cache or show warning)
- [ ] Handle `/clear` command erasing items from inventory mid-tracking
- [ ] Graceful behavior when cache slot ID doesn't match current screen's slot layout
- [ ] Add keybind to manually clear ghost cache (`WIMS: Clear Ghost Memory`)
- [ ] Basic config screen (opacity slider, toggle on/off)

---

## v0.3.0 — HUD & Feedback

**Goal:** Make item recovery feel more satisfying.

- [ ] Ghost fade-out animation when a slot is cleared
- [ ] Small toast/notification: "All lost items recovered!" when cache empties completely
- [ ] Hotbar ghost rendering (show ghosts below hotbar when hotbar screen not open)
- [ ] Optional: ghost item count renders in a distinct color (e.g. red) to distinguish from real items

---

## v0.4.0 — Death History

**Goal:** Let players review past deaths without relying on memory.

- [ ] Store up to 5 most recent deaths in a `List<DeathRecord>`
- [ ] Death history GUI (accessible via keybind or inventory button)
- [ ] Each record shows: timestamp, death location (XYZ), list of lost items
- [ ] Persistent storage across sessions (`deaths.json` in `.minecraft/wims/`)
- [ ] Option to re-activate ghost rendering from a past death record

---

## v0.5.0 — Server Sync (Advanced)

**Goal:** Add despawn timers and server awareness where supported.

- [ ] Optional companion server-side mod (separate jar)
- [ ] When server mod present: receive despawn countdown per item entity
- [ ] HUD overlay showing "Item A: 3:42 remaining" for tracked drops
- [ ] Warning pulse on ghost icon when item is about to despawn (< 30 seconds)

---

## Backlog (No Version Assigned)

- Shulker box sub-inventory tracking (track items inside containers at death)
- Integration with map mods (Xaero's, JourneyMap) to pin death location
- Modrinth auto-update check via metadata API
- Death sound cue customization
