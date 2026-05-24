# 🔍 WhereIsMyStuff? (WIMS) — Client-Side Death Inventory Ghosting QoL Mod

[![Modrinth](https://img.shields.io/badge/Modrinth-v0.1.0-green.svg)](https://modrinth.com/mod/wims)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader-lightgrey.svg)](https://fabricmc.net/)

Tired of losing your items upon death and forgetting exactly which slot your enchanted sword, pickaxe, or food were in? **WhereIsMyStuff? (WIMS)** is the ultimate client-side QoL mod for Minecraft that visually caches your inventory upon death and renders faded **ghost icons** in empty slots until you recover them.

No more inventory clutter, no more sorting hassle, and no more guessing!

---

## 📽️ Preview / Screenshots

*(Placeholder for gameplay GIF: Player dying, opening inventory to see translucent ghost items, picking up items, and watching them snap back into place)*

---

## ✨ Why Choose WIMS? (Comparison)

Many mods attempt to solve death recovery, but they either change vanilla gameplay too much or require server installation. **WIMS** is designed to keep vanilla balance while offering pure client-side convenience.

| Feature | Vanilla Default | Gravestone / Corpse Mods | `/keepInventory true` | **WIMS (WhereIsMyStuff?)** |
| :--- | :---: | :---: | :---: | :---: |
| **100% Client-Side Only** | Yes | ❌ No (Requires Server) | Yes | **⭐ Yes (Works everywhere!)** |
| **Vanilla Server Compatible** | Yes | ❌ No | Yes | **⭐ Yes (Hypixel, Realms, etc.)** |
| **Keeps Vanilla Death Challenge** | Yes | Yes | ❌ No (No item drop penalty) | **⭐ Yes (Items still drop & despawn)** |
| **Visually Remembers Item Slots** | ❌ No | ❌ No | Yes | **⭐ Yes (35% Ghost items)** |
| **Automatic Slot Restoration** | ❌ No | ❌ No | Yes | **⭐ Yes (Snaps items back)** |

---

## 🚀 Key Features

*   **👻 Translucent Ghost Items:** Displays lost items at **35% opacity** in their exact pre-death slots when viewing any player inventory screen.
*   **🔌 Zero Server Dependency:** Plays perfectly on singleplayer, LAN, Realms, vanilla servers, and heavily-modded multiplayer networks. 
*   **🎯 Smart Slot Restore:** Picked-up recovered items are automatically routed back to their original slots, saving you from tedious manual sorting.
*   **🔄 Partial Recovery Support:** Ghost indicators stay visible until the full stack count is recovered. Got back only 2 of your 5 lost diamonds? The ghost item remains with count `3` until they are all recovered!
*   **⚡ High Performance:** Purely client-side and event-driven. Optimized algorithms ensure zero tick-based performance drops or FPS stutter.
*   **🔒 In-Memory Security:** Stored in-memory to prevent disk clutter. Safe, temporary, and clean.

---

## 🛠️ How It Works (Step-by-Step)

1.  **Death Snapshot:** The exact tick you die, WIMS captures a client-side clone of your inventory slots (0-40, main inventory, hotbar, armor, offhand).
2.  **Ghosting Stage:** Upon respawn, empty slots containing your lost items render semi-transparent ghost icons and item count labels.
3.  **Active Syncing:** As you pick up items from your death point, WIMS verifies if the item type matches. If it does, the items snap back into their original slots.
4.  **Automatic Clear:** Once the slot meets or exceeds the lost stack count, the ghost state is cleared.

---

## 📦 Requirements & Installation

Getting started is quick and easy:

1.  Make sure you are running Minecraft **1.21.4**.
2.  Install [Fabric Loader](https://fabricmc.net/) (0.16.9 or newer).
3.  Download and place the [Fabric API](https://modrinth.com/mod/fabric-api) jar in your `mods` folder.
4.  Drop the `whereismystuff-0.1.0.jar` into your `mods` folder and enjoy!

---

## 🏷️ Search Keywords & Tags

*   **Categories:** QoL (Quality of Life), Utility, Inventory, Client-side
*   **Keywords:** death cache, death inventory, corpse finder, keep inventory alternative, client-side gravestone, visual death recovery, slot helper, fabric death mod, lost item tracker, minecraft 1.21.4 client mods

---

## 📄 License

This project is licensed under the **CC0 License** (Public Domain). Feel free to use it in any modpack, fork it, or reference it in your own mods!
