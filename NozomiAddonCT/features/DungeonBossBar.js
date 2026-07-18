import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { TriggerTitle } from "../utils/customTitle";

let inP5 = false

let mainText = new Text(` `, 5, 5).setScale(1).setShadow(true);
let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;
let text = " "

function ShortNumber(number) {
    if (number < 1000) {
        return number.toString();
    };
    const units = ["K", "M", "B", "T"];
    let unitIndex = Math.floor((number.toString().length - 1) / 3) - 1;
    let abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);
    if (abbreviatedNumber < 1 && unitIndex > 0) {
        unitIndex--;
        abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);
    };
    return `${abbreviatedNumber}${units[unitIndex]}`;
};

let bossHealth = {
    "F7": {
        "Maxor": 100000000,
        "Storm": 400000000,
        "Goldor": 750000000,
        "Necron": 1000000000
    },
    "M7": {
        "Maxor": 800000000,
        "Storm": 1000000000,
        "Goldor": 1200000000,
        "Necron": 1400000000
    },
}

let bossSpawn = {
    "Maxor": false,
    "Storm": false,
    "Goldor": false,
    "Necron": false
}

let trackedBosses = {}

registerWhen(register("tick", () => {
    trackedBosses = {}
    if (!Settings.witherbossbar) return;
    if (!Dungeon.inDungeon) return;
    if (!Dungeon.floorNumber) return;
    if (inP5) return;

    let dungeonText = (Dungeon.dungeonType === "Master Mode")
        ? `M${Dungeon.floorNumber}`
        : `F${Dungeon.floorNumber}`

    if (!bossHealth[dungeonText]) return

    World.getAllEntitiesOfType(Java.type("net.minecraft.entity.boss.EntityWither")).forEach(wither => {
        let name = wither.getName().removeFormatting()
        let x = wither.getRenderX();
        let y = wither.getRenderY();
        let z = wither.getRenderZ();

        if (!bossHealth[dungeonText].hasOwnProperty(name)) return

        let maxHP = bossHealth[dungeonText][name]
        let currentHP = wither.getEntity().func_110143_aJ()
        let percentage = (maxHP / 300)
        if ((currentHP / 300) >= 1) {
            if (bossSpawn[name] == false) {
                ChatLib.chat(`${Settings.prefix} &4&l${name} &chas spawned!`);
                bossSpawn[name] = true;
            };
        } else {
            if ((currentHP / 300) <= 0.01) {
                if (bossSpawn[name] == true) {
                    TriggerTitle(`&4&l${name} DIED`)
                    ChatLib.chat(`${Settings.prefix} &4&l${name} &chas died!`);
                    bossSpawn[name] = false;
                };
            };
        }
        if (percentage > 0.05) {
            trackedBosses[name] = {
                text: `${ShortNumber((currentHP * percentage).toFixed(2))}`,
                x: x,
                y: y,
                z: z
            }
        } else {
            trackedBosses[name] = {
                text: `0`,
                x: x,
                y: y,
                z: z
            }
        }
    })
}), () => Settings.witherbossbar && Dungeon.inDungeon)

registerWhen(register("renderOverlay", () => {
    if (!Dungeon.inDungeon) return;
    if (inP5) return;
    for (let name in trackedBosses) {
        text = `&c&l${name}&r &a${trackedBosses[name].text} &c❤`
    }
}), () => Settings.witherbossbar && Dungeon.inDungeon)

registerWhen(onChatPacket((message) => {
    if (!Settings.witherbossbar) return
    if (!Dungeon.inDungeon) return;
    if (message != "All this, for nothing...") return
    inP5 = true
    text = " "
}).setCriteria(/^\[BOSS\] Necron: (.+)$/), () => Settings.witherbossbar && Dungeon.inDungeon)

registerHudElement(() => {
    if (!Settings.witherbossbar) return;
    let x = Number(Settings.witherbossbar_pos_x);
    let y = Number(Settings.witherbossbar_pos_y);
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("WitherBossBar")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    mainText.setScale(Settings.witherbossbar_scale);
    mainText.draw(x, y);
}, () => Settings.witherbossbar);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.witherbossbar) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("WitherBossBar", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.witherbossbar_pos_x);
    let textY = Number(Settings.witherbossbar_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "WitherBossBar"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("WitherBossBar")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.witherbossbar_pos_x = "0";
        Settings.witherbossbar_pos_y = "0";
        Settings.witherbossbar_scale = "1";
        Settings.save()
    };
}), () => Settings.witherbossbar);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("WitherBossBar")) return
    Settings.witherbossbar_pos_x = x - dragOffsetX;
    Settings.witherbossbar_pos_y = y - dragOffsetY;
}), () => Settings.witherbossbar);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("WitherBossBar")) return
    Settings.witherbossbar_scale = Number(Settings.witherbossbar_scale)
    if (dir == 1) Settings.witherbossbar_scale += 0.05
    else Settings.witherbossbar_scale -= 0.05
}), () => Settings.witherbossbar);

registerWhen(register("worldUnload", () => {
    text = " "
    inP5 = false
    bossSpawn = {
        "Maxor": false,
        "Storm": false,
        "Goldor": false,
        "Necron": false
    }
}), () => Settings.witherbossbar)

registerWhen(register("worldLoad", () => {
    text = " "
    inP5 = false
}), () => Settings.witherbossbar)