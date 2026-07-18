import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { createSkull } from "../../tska/utils/InventoryUtils"
import { registerHudElement } from "../utils/HUDManager";

let mainText = new Text(`&5ThunderBottle: &c???&6/&c???`, 5, 5).setScale(1).setShadow(true);
let bottles = ["Empty Storm Bottle", "Empty Thunder Bottle", "Empty Hurricane Bottle"]
let maxbottles = ["Storm in a Bottle", "Thunder in a Bottle", "Hurricane in a Bottle"]
let Icon = createSkull()

let maxChargeMap = {
    "Empty Thunder Bottle": 50000,
    "Empty Storm Bottle": 500000,
    "Empty Hurricane Bottle": 5000000,
    "Thunder in a Bottle": 50000,
    "Storm in a Bottle": 500000,
    "Hurricane in a Bottle": 5000000
};

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;
let reminder = false
let itsfull = false
let BottleName = ""

function comma(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

let tickCounter = 0;
let useWhite = true;

registerWhen(register("step", () => {
    tickCounter++;
    if (tickCounter >= 20) {
        useWhite = !useWhite;
        tickCounter = 0;
    }
}), () => Settings.thunderbottle);

function getFullColor() {
    return useWhite ? "&f" : "&a";
};

registerHudElement(() => {
    if (!Settings.thunderbottle) return;
    let x = Number(Settings.thunderbottle_pos_x);
    let y = Number(Settings.thunderbottle_pos_y);
    if (Settings.editUI) {
        if (Settings.editUIName.includes("ThunderBottle")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, 120 * Settings.thunderbottle_scale, 20 * Settings.thunderbottle_scale);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, 120 * Settings.thunderbottle_scale, 20 * Settings.thunderbottle_scale);
        };
    };

    let hasBottle = false
    let fullBottle = false
    let TargetBottle = null
    let maxCharge = 0
    let text = ""
    Player.getInventory().getItems().forEach(item => {
        let name = item?.getName()?.removeFormatting()
        if (bottles.includes(name)) {
            TargetBottle = item
            fullBottle = false
            itsfull = false
            hasBottle = true
        } else if (maxbottles.includes(name)) {
            TargetBottle = item
            fullBottle = true
            itsfull = true
            hasBottle = true
        }
    })
    if (!hasBottle) return
    BottleName = TargetBottle.getName()
    let nbt = TargetBottle.getNBT();
    let attri = nbt?.getCompoundTag("tag").getCompoundTag("ExtraAttributes")
    let charge = attri?.get("thunder_charge")
    let skullOwner = nbt?.get("tag")?.get("SkullOwner");
    let textures = skullOwner?.get("Properties")?.get("textures");
    let jsonObj = String(textures) || "";
    let skinTexture = "";
    let match = jsonObj?.match(/Value:"([^"]+)"/);
    if (match) {
        skinTexture = match[1];
    };
    Icon = createSkull(skinTexture)

    if (fullBottle) {
        maxCharge = maxChargeMap[BottleName.removeFormatting()] || 0;
        let color = getFullColor() || "&a"
        text = `${color}&lFULL CHARGE!`;
    } else {
        maxCharge = maxChargeMap[BottleName.removeFormatting()] || 0;
        text = `&e${charge ? comma(charge) : 0}&6/&e${maxCharge ? comma(maxCharge) : "&c???"}`;
    };

    mainText.setScale(Settings.thunderbottle_scale);
    mainText.setString(text);

    Renderer.retainTransforms(true)
    Renderer.translate(x, y)
    Renderer.scale(Number(Settings.thunderbottle_scale))
    Icon.draw(0, 0)
    Renderer.drawStringWithShadow(text, 16, 4)
    Renderer.retainTransforms(false)
}, () => Settings.thunderbottle);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.thunderbottle) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("ThunderBottle", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.thunderbottle_pos_x);
    let textY = Number(Settings.thunderbottle_pos_y);
    let textWidth = 120 * Settings.thunderbottle_scale
    let textHeight = 20 * Settings.thunderbottle_scale

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "ThunderBottle"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("ThunderBottle")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.thunderbottle_pos_x = "0";
        Settings.thunderbottle_pos_y = "0";
        Settings.thunderbottle_scale = "1";
        Settings.save()
    };
}), () => Settings.thunderbottle);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("ThunderBottle")) return
    Settings.thunderbottle_pos_x = x - dragOffsetX;
    Settings.thunderbottle_pos_y = y - dragOffsetY;
}), () => Settings.thunderbottle);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("ThunderBottle")) return
    Settings.thunderbottle_scale = Number(Settings.thunderbottle_scale)
    if (dir == 1) Settings.thunderbottle_scale += 0.05
    else Settings.thunderbottle_scale -= 0.05
}), () => Settings.thunderbottle);

registerWhen(register("worldLoad", () => {
    reminder = true
    Client.scheduleTask(2, () => {
        if (reminder && itsfull) {
            reminder = false;
            ChatLib.chat(`${Settings.prefix} &aYour ${BottleName} &ahas fully charge!`);
        };
    });
}), () => Settings.thunderbottle);