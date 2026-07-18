# ⚔️ NozomiAddon

**Status:** 🟢 Active Development

A high-performance, client-side utility modification developed using **Java** and the **Fabric API** (targeted for modern versions 26.x). Engineered specifically for high-complexity game environments like Hypixel Skyblock, this mod injects low-latency logic to process granular game data in real-time.

## 🚀 Core Features & Technical Highlights
*   **Advanced NBT Data Parsing:** Efficiently extracts and manipulates Named Binary Tag (NBT) components to process complex game data without heavily impacting the client thread.
*   **Dynamic HUD & Timers:** Features custom, responsive Heads-Up Display elements and asynchronous gameplay timers (e.g., MaskTimer) that update fluidly during high-action scenarios.
*   **Real-Time Entity Highlighting:** Implements optimized spatial tracking (e.g., StarredMobHighlight) to render custom visual cues around specific entities, improving situational awareness in cluttered environments.
*   **Asynchronous Event Handling:** Orchestrates multi-threaded event logic to ensure the client-side modifications do not cause frame drops or hinder native runtime performance.

## 🛠️ Technologies & Tools
*   **Language:** Java
*   **Framework:** Fabric API (v1.21.x)
*   **Architecture:** Event-driven Client Modification, Object-Oriented Design
