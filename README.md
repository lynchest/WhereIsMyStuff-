<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/a86f3a6697bac060335cb751c4b4154f85576d34_0.webp" alt="WhereIsMyStuff? Banner" width="800">
</p>

# <img src="https://cdn.modrinth.com/data/cached_images/78a4d60f719c3ae86938705c7504e8192a3da363_0.webp" alt="WIMS Icon" width="40" align="center"> WhereIsMyStuff? (WIMS)

<p align="center">
  <a href="https://modrinth.com/mod/wims">
    <img src="https://img.shields.io/badge/Modrinth-v0.1.0-green.svg" alt="Modrinth">
  </a>
  <a href="https://minecraft.net/">
    <img src="https://img.shields.io/badge/Minecraft-1.21.4--1.21.11-blue.svg" alt="Minecraft">
  </a>
  <a href="https://fabricmc.net/">
    <img src="https://img.shields.io/badge/Fabric-Loader-lightgrey.svg" alt="Fabric">
  </a>
</p>


Tired of losing your items upon death and forgetting exactly which slot your enchanted sword, pickaxe, or food were in? **WhereIsMyStuff? (WIMS)** is the ultimate client-side QoL mod for Minecraft that visually caches your inventory upon death and renders faded **ghost icons** in empty slots until you recover them.

No more inventory clutter, no more sorting hassle, and no more guessing!

---

## 📽️ Preview & Gameplay Showcase

WIMS works seamlessly without any configuration. Here is how it behaves in practice:

<table border="0" align="center">
  <tr>
    <td align="center" valign="top" width="33%">
      <h3>1. Before Death</h3>
      <a href="https://cdn.modrinth.com/data/cached_images/5796f1b0b800b1ab02ae378bf23d3547f60e0444.png" target="_blank">
        <img src="https://cdn.modrinth.com/data/cached_images/5796f1b0b800b1ab02ae378bf23d3547f60e0444.png" alt="Pre-death Inventory" width="100%" style="border-radius: 4px; box-shadow: 0 4px 8px rgba(0,0,0,0.15);">
      </a>
      <p align="left"><em>A typical fully loaded inventory with diamond armor, tools, and valuable materials before death.</em></p>
    </td>
    <td align="center" valign="top" width="33%">
      <h3>2. After Death (Ghosting)</h3>
      <a href="https://cdn.modrinth.com/data/cached_images/ac8d9e4e02f76a53970227caaf63c3305719b467.png" target="_blank">
        <img src="https://cdn.modrinth.com/data/cached_images/ac8d9e4e02f76a53970227caaf63c3305719b467.png" alt="Ghost Inventory Icons" width="100%" style="border-radius: 4px; box-shadow: 0 4px 8px rgba(0,0,0,0.15);">
      </a>
      <p align="left"><em>Upon respawning, empty slots where items were lost display ghost items covered by a 55% opacity dark dimming overlay, showing you exactly where each item belongs.</em></p>
    </td>
    <td align="center" valign="top" width="33%">
      <h3>3. Partial Recovery</h3>
      <a href="https://cdn.modrinth.com/data/cached_images/1e35a99d5212b3987424921b9f13f1b850239255.png" target="_blank">
        <img src="https://cdn.modrinth.com/data/cached_images/1e35a99d5212b3987424921b9f13f1b850239255.png" alt="Partial Recovery Sync" width="100%" style="border-radius: 4px; box-shadow: 0 4px 8px rgba(0,0,0,0.15);">
      </a>
      <p align="left"><em>Picked up items automatically snap back to their original slots. Ghost indicators remain visible for items you haven't recovered yet (like the helmet, leggings, boots, and axe).</em></p>
    </td>
  </tr>
</table>

---

## ✨ Why WIMS? (Comparison)

Many mods attempt to solve death recovery, but they either change vanilla gameplay too much or require server installation. **WIMS** is designed to keep vanilla balance while offering pure client-side convenience.

| Feature | Vanilla Default | Gravestone / Corpse Mods | `/keepInventory true` | **<img src="https://cdn.modrinth.com/data/cached_images/78a4d60f719c3ae86938705c7504e8192a3da363_0.webp" alt="WIMS Icon" width="18" align="center"> WIMS (WhereIsMyStuff?)** |
| :--- | :---: | :---: | :---: | :---: |
| **100% Client-Side Only** | Yes | ❌ No (Requires Server) | Yes | **⭐ Yes (Works everywhere!)** |
| **Vanilla Server Compatible** | Yes | ❌ No | Yes | **⭐ Yes (Hypixel, Realms, etc.)** |
| **Keeps Vanilla Death Challenge** | Yes | Yes | ❌ No (No item drop penalty) | **⭐ Yes (Items still drop & despawn)** |
| **Visually Remembers Item Slots** | ❌ No | ❌ No | Yes | **⭐ Yes (Dimmed ghost overlay)** |
| **Automatic Slot Restoration** | ❌ No | ❌ No | Yes | **⭐ Yes (Snaps items back)** |

---

## 🚀 Key Features

*   **👻 Elegant Ghost Slots:** Displays lost items with a highly compatible, cross-version safe **55% opacity dark dimming overlay (`0x8C000000`)** in their exact pre-death slots, completely avoiding any rendering or `RenderSystem` version mismatches.
*   **🔌 Zero Server Dependency:** Plays perfectly on singleplayer, LAN, Realms, vanilla servers, and heavily-modded multiplayer networks. 
*   **🎯 Smart Slot Restore:** Picked-up recovered items are automatically routed back to their original slots, saving you from tedious manual sorting.
*   **🔀 Occupant Relocation (Auto-Swap):** If a ghost slot gets occupied by another item before recovery (e.g. picking up dirt first), picking up the correct item later will **automatically relocate** the temporary occupant to another empty slot, restoring the correct item to its rightful original slot!
*   **🔄 Partial Recovery Support:** Ghost indicators stay visible until the full stack count is recovered. Got back only 2 of your 5 lost diamonds? The ghost item remains with count `3` until they are all recovered!
*   **⚡ High Performance:** Purely client-side, event-driven, and highly optimized. Features clean, on-demand mixin hooks to guarantee zero per-tick overhead or FPS stutter.
*   **🪶 Featherweight Footprint:** The entire compiled jar size is **only ~67 KB**! Utilizing custom pixel-perfect color-quantized asset compression, WIMS provides top-tier visual convenience without bloating your modpack or increasing launch times.
*   **🔒 In-Memory Security:** Death snapshots are stored purely in memory. No disk clutter, no temporary file footprint, safe, temporary, and clean.

---

## 🛠️ How It Works (Step-by-Step)

1.  **Death Snapshot:** The exact tick you die, WIMS captures a client-side clone of your inventory slots (0-40, main inventory, hotbar, armor, offhand).
2.  **Ghosting Stage:** Upon respawn, empty slots containing your lost items render their icons covered by an elegant 55% opacity dark vignette overlay with item count labels.
3.  **Active Syncing & Swapping:** As you pick up items from your death point, WIMS routes them back to their original slots. If a slot is already blocked by a different item, WIMS automatically relocates the blocker to an empty slot to make room for your recovered item!
4.  **Automatic Clear:** Once the slot meets or exceeds the lost stack count, the ghost state is cleared.

---

## 📦 Requirements & Installation

### 🎮 Supported Versions

While WIMS is programmatically compatible with the entire range of Minecraft versions, we manually test specific major releases to guarantee absolute stability:

| Minecraft Version | Compatibility | Manually Tested & Verified | Status |
| :--- | :---: | :---: | :---: |
| **1.21.4** | Yes | ⭐ **Yes** | **Stable** |
| **1.21.5 - 1.21.9** | Yes | 🔄 *Community Supported* | **Compatible** |
| **1.21.10** | Yes | ⭐ **Yes** | **Stable** |
| **1.21.11** | Yes | ⭐ **Yes** | **Stable** |

### 🚀 Getting Started

1.  Make sure you are running a supported Minecraft version from the list above.
2.  Install [Fabric Loader](https://fabricmc.net/) (0.16.9 or newer).
3.  Download and place the [Fabric API](https://modrinth.com/mod/fabric-api) jar in your `mods` folder.
4.  Drop the `whereismystuff-0.1.0.jar` into your `mods` folder and enjoy!

---

## 🏷️ Search Keywords & Tags

*   **Categories:** QoL (Quality of Life), Utility, Inventory, Client-side
*   **Keywords:** death cache, death inventory, corpse finder, keep inventory alternative, client-side gravestone, visual death recovery, slot helper, fabric death mod, lost item tracker, minecraft 1.21 client mods

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. Feel free to use it in any modpack, fork it, or reference it in your own mods, as long as any modifications and derived works remain open-source under the same GPL-3.0 license!
