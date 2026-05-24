# WIMS — Memories

> **KURAL:** Bu dosya bağlam (context) verimliliği için **maksimum kısalıkta ve tek satır formatında** tutulmalıdır.
> Format: `- **Yapıldı:** [Çok kısa açıklama] | **Etki:** [Virgülle ayrılmış göreceli dosya yolları/adları, file:// veya mutlak yol kullanmayın]`


- **Yapıldı:** MEMORIES.md oluşturuldu, AGENTS.md'ye bellek kuralı eklendi. | **Etki:** /, AGENTS.md
- **Yapıldı:** PROGRESS.md'deki IntelliJ gereksinimi kaldırıldı, VS Code seçeneği eklendi. | **Etki:** PROGRESS.md
- **Yapıldı:** Phase 0 (Java 21, Fabric şablonu, 1.21.4 Gradle, genSources & build). | **Etki:** gradle.properties, build.gradle, ExampleClientMixin.java, PROGRESS.md
- **Yapıldı:** Phase 1 (Mod ID, client-config, wims.mixins.json, WimsMod.java, cleanup). | **Etki:** build.gradle, fabric.mod.json, wims.mixins.json, WimsMod.java, PROGRESS.md
- **Yapıldı:** Phase 2 (DeathInventoryCache.java yardımıyla envanter kopyalama mantığı). | **Etki:** DeathInventoryCache.java, PROGRESS.md
- **Yapıldı:** Phase 3 (PlayerDeathMixin, HandledScreenMixin, InventorySyncMixin, tick-based sync). | **Etki:** PlayerDeathMixin.java, HandledScreenMixin.java, InventorySyncMixin.java, WimsMod.java
- **Yapıldı:** IDE derleme yolu hataları için src/client placeholder eklendi, TextRenderer import'u silindi. | **Etki:** HandledScreenMixin.java, src/client
- **Yapıldı:** Phase 4 (Doğrulama, temiz build, walkthrough). | **Etki:** PROGRESS.md, walkthrough.md
- **Yapıldı:** CI derleme hatasını çözmek için org.gradle.java.home kaldırıldı. | **Etki:** gradle.properties
- **Yapıldı:** rootProject.name 'whereismystuff' olarak güncellendi. | **Etki:** settings.gradle
- **Yapıldı:** Ölümden önce envanter yedeği (pre-death cache) için handleStatus ve END_CLIENT_TICK eklendi. | **Etki:** DeathInventoryCache.java, PlayerDeathMixin.java, WimsMod.java
- **Yapıldı:** Alınan eşyaları orijinal ölüm slotlarına yerleştiren slot haritalama mantığı eklendi. | **Etki:** WimsMod.java
- **Yapıldı:** Offhand ghost eşya render sorunu için drawSlot enjeksiyonu TAIL'e taşındı. | **Etki:** HandledScreenMixin.java
- **Yapıldı:** Mob ölümlerinde yedek silinmesini önlemek için hasar koruması ve duplicate capture flag'leri eklendi. | **Etki:** DeathInventoryCache.java, WimsMod.java, PlayerDeathMixin.java
- **Yapıldı:** Tüm ölüm tiplerinde pre-death cache çalışabilmesi için hurtTime kontrolü kaldırıldı. | **Etki:** DeathInventoryCache.java, WimsMod.java
- **Yapıldı:** Mob ölümleri için HealthUpdateMixin (health=0) eklendi; PlayerDeathMixin.onDeath silindi, resetForRespawn() eklendi. | **Etki:** HealthUpdateMixin.java, DeathInventoryCache.java, WimsMod.java, wims.mixins.json | **Silinen:** PlayerDeathMixin.java
- **Yapıldı:** HealthUpdateMixin main thread'e taşındı, lastHealth takibi ve ani envanter kaybı tespiti eklendi. | **Etki:** HealthUpdateMixin.java, WimsMod.java, DeathInventoryCache.java
- **Yapıldı:** Ölüm tespit sistemi basitleştirildi: tick-based snapshot + DeathScreenMixin ile dondurma. complex packet hook'lar kaldırıldı. | **Etki:** DeathInventoryCache.java, DeathScreenMixin.java, WimsMod.java, wims.mixins.json | **Silinen:** HealthUpdateMixin.java, PlayerDeathMixin.java
- **Yapıldı:** Snapshot race condition çözüldü: boş envanter paketleri snapshot'ı ezmesin diye !hasItems kontrolü. | **Etki:** DeathInventoryCache.java
- **Yapıldı:** Hasar anında snapshot'ı geçici dondurarak boş envanter yarışı önleyen damageCooldown eklendi. | **Etki:** WimsMod.java, DeathInventoryCache.java
- **Yapıldı:** Debug loglama (wims_debug.log) ve boş envanter koruma check'i (!hasItems) entegrasyonu. | **Etki:** WimsMod.java, DeathInventoryCache.java, DeathScreenMixin.java
- **Yapıldı:** Render teşhisi için HandledScreenMixin ve clearSlot'a detaylı slot logları eklendi. | **Etki:** HandledScreenMixin.java, DeathInventoryCache.java
- **Yapıldı:** DeathScreen açıkken veya ölü iken envanter sync'i engellendi (double race condition çözümü). | **Etki:** WimsMod.java
- **Yapıldı:** Blok ghost şeffaflığı için shader reset öncesi context.draw() çağrısı eklendi. | **Etki:** HandledScreenMixin.java
- **Yapıldı:** Blok modellerinin opak çizilmesini önleyen ItemRendererMixin (opaque/cutout -> translucent yönlendirmesi) eklendi. | **Etki:** ItemRendererMixin.java, HandledScreenMixin.java, WimsMod.java, wims.mixins.json
- **Yapıldı:** README.md Modrinth SEO'su, karşılaştırma tablosu ve arama anahtar kelimeleriyle optimize edildi. | **Etki:** README.md
- **Yapıldı:** icon.png, banner.png ve icon_ghost.png belirlendi, mod varlıklarına ve README.md'ye eklendi. | **Etki:** src/main/resources/assets/whereismystuff/icon.png, README.md, PROGRESS.md
- **Yapıldı:** media klasöründeki oynanış ekran görüntüleri doğrulandı ve README.md dosyasına entegre edildi. | **Etki:** README.md
- **Yapıldı:** CHANGELOG.md dosyası oluşturuldu ve v0.1.0 sürüm notları yazıldı. | **Etki:** CHANGELOG.md, PROGRESS.md
- **Yapıldı:** Üretim (production) sürümü öncesi disk loglama kaldırıldı, per-tick log flood engellendi. | **Etki:** WimsMod.java, HandledScreenMixin.java, DeathInventoryCache.java
- **Yapıldı:** Oyuncu sunucudan veya dünyadan ayrıldığında hayalet envanteri temizleyen DISCONNECT dinleyicisi eklendi. | **Etki:** WimsMod.java, DeathInventoryCache.java
- **Yapıldı:** Pixel art ikon, en yakın komşu (Nearest Neighbor) ile 256x256 piksele küçültüldü; .jar boyutu %95 küçülerek 57 KB oldu. | **Etki:** icon.png, src/main/resources/assets/whereismystuff/icon.png
- **Yapıldı:** README.md ve CHANGELOG.md dosyalarına modun yüksek performans ve 57 KB tüy sıklet boyutu eklendi. | **Etki:** README.md, CHANGELOG.md


