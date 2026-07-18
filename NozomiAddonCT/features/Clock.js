import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";

let mainText = new Text(`&f00:00:00`, 5, 5).setScale(1).setShadow(true);
let text = `&f00:00:00`

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

register("step", () => {
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, "0");
    const minutes = now.getMinutes().toString().padStart(2, "0");
    const seconds = now.getSeconds().toString().padStart(2, "0");
    text = `&f${hours}:${minutes}:${seconds}`
}).setDelay(1);

registerHudElement(() => {
    if (!Settings.realtimeclock) return;

    const x = Number(Settings.realtimeclock_pos_x);
    const y = Number(Settings.realtimeclock_pos_y);
    const scale = Number(Settings.realtimeclock_scale);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("Realtime Clock")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };

    mainText.setString(text);
    mainText.setScale(scale);
    mainText.draw(x, y);
}, () => Settings.realtimeclock);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.realtimeclock) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("Realtime Clock", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.realtimeclock_pos_x);
    let textY = Number(Settings.realtimeclock_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "Realtime Clock"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("Realtime Clock")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.realtimeclock_pos_x = "0";
        Settings.realtimeclock_pos_y = "0";
        Settings.realtimeclock_scale = "1";
        Settings.save()
    };
}), () => Settings.realtimeclock);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("Realtime Clock")) return
    Settings.realtimeclock_pos_x = x - dragOffsetX;
    Settings.realtimeclock_pos_y = y - dragOffsetY;
}), () => Settings.realtimeclock);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("Realtime Clock")) return
    Settings.realtimeclock_scale = Number(Settings.realtimeclock_scale)
    if (dir == 1) Settings.realtimeclock_scale += 0.05
    else Settings.realtimeclock_scale -= 0.05
}), () => Settings.realtimeclock);