# WIMS — Progress Tracker

## Status: 🟡 Phase 4 Completed

**Target:** Minecraft 1.21.4 · Fabric · Java 21  
**Goal:** Çalışan bir mod yap, Modrinth'e yükle. Sürüm desteği sonra.

---

## Phase 0 — Dev Environment Setup

- [x] Java 21 JDK kurulu (`java -version` → `21.x.x`)
- [x] VS Code veya IntelliJ IDEA kurulu
- [x] Fabric example mod template indirildi:
  `git clone https://github.com/FabricMC/fabric-example-mod`
- [x] `gradle.properties` WIMS sürümleriyle güncellendi
- [x] `./gradlew genSources` çalıştı (Yarn mappings indiriliyor, ilk seferde 5-10 dk sürer)
- [x] Seçtiğiniz IDE'de proje açıldı, Gradle sync/import tamamlandı
- [x] `./gradlew build` hatasız geçti (template kodu derlendi)
- [x] Minecraft açıldı, mod yüklendi (Mods menüsünde görünüyor)

---

## Phase 1 — Scaffold
- [x] `gradle.properties` → exact 1.21.4 versions
- [x] `fabric.mod.json` → `"environment": "client"` declared
- [x] `wims.mixins.json` → 3 mixin registered
- [x] `WimsMod.java` → entrypoint compiles cleanly
- [x] Build passes: `./gradlew build`

---

## Phase 2 — Core Logic
- [x] `DeathInventoryCache.java` → tüm metodlar implement edildi
- [x] Basit doğrulama: cache'e bir ItemStack yaz, oku, temizle — çalışıyor

---

## Phase 3 — Mixins
- [x] `PlayerDeathMixin` → `/kill` ile ölünce cache doluyor (log ile doğrula)
- [x] `PlayerDeathMixin` → düşerek ölünce de cache doluyor
- [x] `HandledScreenMixin` → ghost ikonlar doğru slotta görünüyor
- [x] `HandledScreenMixin` → alpha 0.35, renk reset doğru çalışıyor
- [x] `InventorySyncMixin` → eşya alınınca slot temizleniyor
- [x] `InventorySyncMixin` → kısmi recovery'de ghost kalıyor

---

## Phase 4 — Integration Testing
- [x] Senaryo 1: `/kill`, envanter aç → ghost'lar görünüyor
- [x] Senaryo 2: Kısmi item recovery → ghost kalıyor
- [x] Senaryo 3: Tam recovery → vanilla'ya dönüyor
- [x] Senaryo 4: İkinci ölüm → cache doğru şekilde üzerine yazıyor
- [x] Senaryo 5: Sandık/chest ekranı açılınca → ghost yok (sadece player inventory)

---

## Phase 5 — Release
- [ ] `README.md` yazıldı (Modrinth description)
- [ ] Mod ikonu eklendi (`icon.png`, 512×512)
- [ ] `CHANGELOG.md` → v0.1.0 girişi
- [x] Build temiz: `./gradlew build` → `.jar` üretildi
- [x] Singleplayer'da test edildi
- [ ] Vanilla local server'da test edildi
- [ ] Modrinth proje sayfası oluşturuldu
- [ ] v0.1.0 `.jar` Modrinth'e yüklendi

---

## Backlog (Phase 5 Sonrası)
- [ ] 1.20 → 1.20.4 backport (branch: `backport/1.20`)
- [ ] 1.21.1, 1.21.2, 1.21.3 uyumluluk testi

---

## Known Issues / Blockers

_None yet._

---

## Session Log

| Date | Work Done |
|------|-----------|
| —    | Project docs created, 1.21.4 versioned |
