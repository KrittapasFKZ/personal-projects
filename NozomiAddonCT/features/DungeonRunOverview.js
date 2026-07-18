import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { getBloodTicks } from "./DungeonBlood";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerHudElement } from "../utils/HUDManager";
import { getTime, getSecs, registerWhen } from "../utils/utils";

const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let mainText = new Text(` `, 5, 5).setScale(1).setShadow(true);

let overviewStr = " "
let ticksElapsed = 0;
let splittimer = {
    Step: 0,
    Zone: "Total",
    Total: 0,
    Maxor: 0,
    Storm: 0,
    Terminals: 0,
    Goldor: 0,
    Necron: 0,
    Dragons: 0
};

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

registerWhen(register("packetReceived", () => {
    if (!Settings.runoverview) return
    if (!Dungeon.inDungeon) return;

    let bloodOpened = "?"
    let portalTime = "?"
    let bossEntry = "?"

    if (!Dungeon.bloodOpened && Dungeon.runStarted) bloodOpened = getTime(Date.now() - Dungeon.runStarted)
    else if (Dungeon.bloodOpened) bloodOpened = getTime(Dungeon.bloodOpened - Dungeon.runStarted)
    if (!Dungeon.bloodOpened && Dungeon.runStarted && splittimer.Step == 0) { } else if (Dungeon.bloodOpened && splittimer.Step == 0) { splittimer.Step = 1 };

    if (Dungeon.watcherCleared && !Dungeon.bossEntry) portalTime = `${Math.floor((Date.now() - Dungeon.watcherCleared) / 10) / 100}s`
    if (Dungeon.bossEntry) portalTime = `${Math.floor((Dungeon.bossEntry - Dungeon.watcherCleared) / 10) / 100}s`
    if (Dungeon.watcherCleared && !Dungeon.bossEntry && splittimer.Step == 1) { splittimer.Step = 2 };

    if (Dungeon.bossEntry || !Dungeon.runStarted) bossEntry = getTime(Dungeon.bossEntry - Dungeon.runStarted)
    if (Dungeon.bossEntry && splittimer.Step == 2 || !Dungeon.runStarted && splittimer.Step == 2) { splittimer.Step = 3 };

    let dungeonFloor = "???"
    let dungeonText = "???"
    if (Dungeon.dungeonType == "Master Mode") {
        dungeonFloor = `&4M${Dungeon.floorNumber}`;
        dungeonText = `M${Dungeon.floorNumber}`;
    } else {
        if (Dungeon.floorNumber == 0) { dungeonFloor = "&aE" } else {
            dungeonFloor = `&aF${Dungeon.floorNumber}`;
            dungeonText = `F${Dungeon.floorNumber}`;
        };
    };

    let bloodticks = getBloodTicks()
    let realElapsed = (Date.now() - Dungeon.runStarted) / 1000;
    let tickElapsed = ticksElapsed / 20;
    let loss = ((realElapsed - tickElapsed) * -1).toFixed(1);

    if (Dungeon.runStarted && !Dungeon.runEnded) {
        ticksElapsed++;
        splittimer[splittimer.Zone]++;
        realElapsed = (Date.now() - Dungeon.runStarted) / 1000;
        tickElapsed = ticksElapsed / 20;
        loss = ((realElapsed - tickElapsed) * -1).toFixed(1);
    } else {
        realElapsed = (Dungeon.runEnded - Dungeon.runStarted) / 1000;
        tickElapsed = ticksElapsed / 20;
        loss = ((realElapsed - tickElapsed) * -1).toFixed(1);
    };

    if (!Dungeon.bossEntry && Dungeon.runStarted) {
        overviewStr = `&e&lRun Overview&r ${dungeonFloor} &7(&a${getTime((ticksElapsed / 20) * 1000)}&7) &7(&b${loss}s&7)`
        overviewStr += `\n &8${Dungeon.openedWitherDoors} Doors &7| &4BR ${bloodOpened}`
        if (splittimer.Step >= 1) overviewStr += `\n &cWatcher Clear: ${(bloodticks / 20).toFixed(2)}s`
        if (splittimer.Step >= 2) overviewStr += `\n &dPortal: ${portalTime}`
        if (splittimer.Step >= 3) overviewStr += `\n &aBoss Entry: ${bossEntry}`
    } else {
        if (Dungeon.bossEntry && Dungeon.runStarted) {
            if (dungeonText == "M7" || dungeonText == "F7") {
                overviewStr = `&e&lRun Overview&r ${dungeonFloor} &7(&a${getTime((ticksElapsed / 20) * 1000)}&7) &7(&b${loss}s&7)`
                overviewStr += `\n &8${Dungeon.openedWitherDoors} Doors &7| &4BR ${bloodOpened}`
                if (splittimer.Step >= 1) overviewStr += `\n &cWatcher Clear: ${(bloodticks / 20).toFixed(2)}s`
                if (splittimer.Step >= 2) overviewStr += `\n &dPortal: ${portalTime}`
                if (splittimer.Step >= 3) overviewStr += `\n &aBoss Entry: ${bossEntry}`
                if (splittimer.Step >= 4) overviewStr += `\n &aMaxor: ${(splittimer.Maxor / 20).toFixed(2)}s`
                if (splittimer.Step >= 5) overviewStr += `\n &bStorm: ${(splittimer.Storm / 20).toFixed(2)}s`
                if (splittimer.Step >= 6) overviewStr += `\n &eTerminals: ${(splittimer.Terminals / 20).toFixed(2)}s`
                if (splittimer.Step >= 7) overviewStr += `\n &7Goldor: ${(splittimer.Goldor / 20).toFixed(2)}s`
                if (splittimer.Step >= 8) overviewStr += `\n &cNecron: ${(splittimer.Necron / 20).toFixed(2)}s`
                if (splittimer.Step >= 9) overviewStr += `\n &4Dragons: ${(splittimer.Dragons / 20).toFixed(2)}s`
            } else {
                overviewStr = `&e&lRun Overview&r ${dungeonFloor} &7(&a${getTime((ticksElapsed / 20) * 1000)}&7) &7(&b${loss}s&7)`
                overviewStr += `\n &8${Dungeon.openedWitherDoors} Doors &7| &4BR ${bloodOpened}`
                if (splittimer.Step >= 1) overviewStr += `\n &cWatcher Clear: ${(bloodticks / 20).toFixed(2)}s`
                if (splittimer.Step >= 2) overviewStr += `\n &dPortal: ${portalTime}`
                if (splittimer.Step >= 3) overviewStr += `\n &aBoss Entry: ${bossEntry}`
            };
        } else {
            overviewStr = `&e&lRun Overview &7(${dungeonFloor}&7)`
            overviewStr += `\n &7Waiting...`
        };
    };

}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.runoverview && Dungeon.inDungeon);

onChatPacket(() => { splittimer.Step = 4; splittimer.Zone = "Maxor" }).setCriteria("[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!");
onChatPacket(() => { splittimer.Step = 5; splittimer.Zone = "Storm"; ChatLib.chat(`${Settings.prefix} &aMaxor Done: ${(splittimer.Maxor / 20).toFixed(2)}s`) }).setCriteria("[BOSS] Storm: Pathetic Maxor, just like expected.");
onChatPacket(() => { splittimer.Step = 6; splittimer.Zone = "Terminals"; ChatLib.chat(`${Settings.prefix} &bStorm Done: ${(splittimer.Storm / 20).toFixed(2)}s`) }).setCriteria("[BOSS] Goldor: Who dares trespass into my domain?");
onChatPacket(() => { splittimer.Step = 7; splittimer.Zone = "Goldor"; ChatLib.chat(`${Settings.prefix} &eTerminals Done: ${(splittimer.Terminals / 20).toFixed(2)}s`) }).setCriteria("The Core entrance is opening!");
onChatPacket(() => { splittimer.Step = 8; splittimer.Zone = "Necron"; ChatLib.chat(`${Settings.prefix} &7Goldor Done: ${(splittimer.Goldor / 20).toFixed(2)}s`) }).setCriteria("[BOSS] Necron: You went further than any human before, congratulations.");
onChatPacket(() => { splittimer.Step = 9; splittimer.Zone = "Dragons"; ChatLib.chat(`${Settings.prefix} &cNecron Done: ${(splittimer.Necron / 20).toFixed(2)}s`) }).setCriteria("[BOSS] Necron: All this, for nothing...");

registerHudElement(() => {
    if (!Settings.runoverview) return;
    let x = Number(Settings.runoverview_pos_x);
    let y = Number(Settings.runoverview_pos_y);
    let text = `${overviewStr}`
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("RunOverview")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    mainText.setScale(Settings.runoverview_scale);
    mainText.draw(x, y);
}, () => Settings.runoverview);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.runoverview) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("RunOverview", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.runoverview_pos_x);
    let textY = Number(Settings.runoverview_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "RunOverview"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("RunOverview")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.runoverview_pos_x = "0";
        Settings.runoverview_pos_y = "0";
        Settings.runoverview_scale = "1";
        Settings.save()
    };
}), () => Settings.runoverview);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("RunOverview")) return
    Settings.runoverview_pos_x = x - dragOffsetX;
    Settings.runoverview_pos_y = y - dragOffsetY;
}), () => Settings.runoverview);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("RunOverview")) return
    Settings.runoverview_scale = Number(Settings.runoverview_scale)
    if (dir == 1) Settings.runoverview_scale += 0.05
    else Settings.runoverview_scale -= 0.05
}), () => Settings.runoverview);

registerWhen(register("worldUnload", () => {
    ticksElapsed = 0;
    overviewStr = " "
    splittimer = {
        Step: 0,
        Zone: "Total",
        Total: 0,
        Maxor: 0,
        Storm: 0,
        Terminals: 0,
        Goldor: 0,
        Necron: 0,
        Dragons: 0
    };
}), () => Settings.runoverview);

registerWhen(register("worldLoad", () => {
    ticksElapsed = 0;
    overviewStr = " "
    splittimer = {
        Step: 0,
        Zone: "Total",
        Total: 0,
        Maxor: 0,
        Storm: 0,
        Terminals: 0,
        Goldor: 0,
        Necron: 0,
        Dragons: 0
    };
}), () => Settings.runoverview);