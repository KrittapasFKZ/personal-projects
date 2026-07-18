import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";

const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let mainText = new Text(`&660t`, 5, 5).setScale(1).setShadow(true);
let text = `&6Spirit&f: &a60t`

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

let ticksElapsed = -1
let proctype = "&r"

registerWhen(register("packetReceived", () => {
    if (ticksElapsed > -1) {
        ticksElapsed--;
    };
}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.invincibletimer);

// bonzo

registerWhen(register("chat", () => {
    if (!Settings.invincibletimer) return;
    ticksElapsed = 60
    proctype = "&9Bonzo"
}).setCriteria(/Your (⚚)? Bonzo's Mask saved your life!/), () => Settings.invincibletimer);

registerWhen(register("chat", () => {
    if (!Settings.invincibletimer) return;
    ticksElapsed = 60
    proctype = "&9Bonzo"
}).setCriteria(/Your Bonzo's Mask saved your life!/), () => Settings.invincibletimer);

// spirit

registerWhen(register("chat", () => {
    if (!Settings.invincibletimer) return;
    ticksElapsed = 60
    proctype = "&6Spirit"
}).setCriteria("Second Wind Activated! Your Spirit Mask saved your life!"), () => Settings.invincibletimer);

// phoenix

registerWhen(register("chat", () => {
    ticksElapsed = 60
    proctype = "&cPhoenix"
}).setCriteria("Your Phoenix Pet saved you from certain death!"), () => Settings.invincibletimer);

registerHudElement(() => {
    if (!Settings.invincibletimer) return;
    let x = Number(Settings.invincibletimer_pos_x);
    let y = Number(Settings.invincibletimer_pos_y);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("InvincibleTimer")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    if (ticksElapsed >= 0) {
        text = `${proctype}&f: &a${ticksElapsed}t`
    } else {
        if (Settings.editUI) {
            text = `&6Spirit&f: &a60t`
        } else {
            text = `&r`
        };
    };
    mainText.setString(text);
    mainText.setScale(Settings.invincibletimer_scale);
    mainText.draw(x, y);
}, () => Settings.invincibletimer);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.invincibletimer) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("InvincibleTimer", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.invincibletimer_pos_x);
    let textY = Number(Settings.invincibletimer_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "InvincibleTimer"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("InvincibleTimer")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.invincibletimer_pos_x = "0";
        Settings.invincibletimer_pos_y = "0";
        Settings.invincibletimer_scale = "1";
        Settings.save()
    };
}), () => Settings.invincibletimer);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("InvincibleTimer")) return
    Settings.invincibletimer_pos_x = x - dragOffsetX;
    Settings.invincibletimer_pos_y = y - dragOffsetY;
}), () => Settings.invincibletimer);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("InvincibleTimer")) return
    Settings.invincibletimer_scale = Number(Settings.invincibletimer_scale)
    if (dir == 1) Settings.invincibletimer_scale += 0.05
    else Settings.invincibletimer_scale -= 0.05
}), () => Settings.invincibletimer);

registerWhen(register("worldUnload", () => {
    ticksElapsed = -1
    proctype = "&r"
}), () => Settings.invincibletimer)

registerWhen(register("worldLoad", () => {
    ticksElapsed = -1
    proctype = "&r"
}), () => Settings.invincibletimer)