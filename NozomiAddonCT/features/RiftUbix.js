import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";

let mainText = new Text(`&dUbixTimer: &cUNKNOWN`, 5, 5).setScale(1).setShadow(true);
let data = JSON.parse(FileLib.read("NozomiAddon", "data/data.json"));

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;
let focus = false; 

function getUbixTimeLeftString() {
    const data = JSON.parse(FileLib.read("NozomiAddon", "data/data.json"));
    const now = Date.now();
    const diff = data.UbixTime - now;

    if (diff <= 0) return "&dUbixTimer: &a&lREADY!";

    const totalSeconds = Math.floor(diff / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return `&dUbixTimer: &f${hours}h ${minutes}m ${seconds}s`;
};

registerWhen(register("chat", (type, mote) => {
    const now = Date.now() + (2 * 60 * 60 * 1000);
    data = JSON.parse(FileLib.read("NozomiAddon", "data/data.json"));
    data.UbixTime = now;
    FileLib.write("NozomiAddon", "data/data.json", JSON.stringify(data, null, 2));
}).setCriteria("ROUND 1: You chose ${type} and gained ${mote} Motes!"), () => Settings.riftubix);

registerHudElement(() => {
    if (!Settings.riftubix) return;
    let x = Number(Settings.riftubix_pos_x);
    let y = Number(Settings.riftubix_pos_y);
    let text = getUbixTimeLeftString()
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("UbixTimer")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    mainText.setScale(Settings.riftubix_scale);
    mainText.draw(x, y);
}, () => Settings.riftubix);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.riftubix) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("UbixTimer", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.riftubix_pos_x);
    let textY = Number(Settings.riftubix_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "UbixTimer"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("UbixTimer")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.riftubix_pos_x = "0";
        Settings.riftubix_pos_y = "0";
        Settings.riftubix_scale = "1";
        Settings.save()
    };
}), () => Settings.riftubix);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("UbixTimer")) return
    Settings.riftubix_pos_x = x - dragOffsetX;
    Settings.riftubix_pos_y = y - dragOffsetY;
}), () => Settings.riftubix);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("UbixTimer")) return
    Settings.riftubix_scale = Number(Settings.riftubix_scale)
    if (dir == 1) Settings.riftubix_scale += 0.05
    else Settings.riftubix_scale -= 0.05
}), () => Settings.riftubix);