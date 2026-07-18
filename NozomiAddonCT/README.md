# 📦 NozomiAddonCT (Legacy)

> ⚠️ **Notice:** This project is currently **deprecated**. The logic and features have been fully rewritten and optimized in Java as **[NozomiAddon](../NozomiAddon)**.

## 📝 About This Project
NozomiAddonCT was the original, script-based iteration of my client-side utility framework, built using **JavaScript** and the **ChatTriggers** modding framework. It utilized event-driven hooks to customize game states and automate real-time interactions.

### 🔄 Why transition to Java/Fabric?
While ChatTriggers provided an excellent rapid-prototyping environment, running heavy visual loops and complex parsing in JavaScript within the game engine eventually introduced performance bottlenecks. 

I made the architectural decision to deprecate this version and rewrite the entire system natively in **Java (Fabric API)**. This transition allowed for:
*   Direct access to native game code (Mixins).
*   Significantly lower memory overhead and faster NBT data parsing.
*   Better utilization of multi-threading and asynchronous event handling.

This repository is maintained to showcase my progression from scripting-based modifications to robust, natively compiled software engineering.

## 🛠️ Technologies
*   **Language:** JavaScript
*   **Framework:** ChatTriggers
