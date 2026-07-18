import Settings from "../config";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";

let mainText = new Text(` `, 5, 5).setScale(1).setShadow(true);
let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

let currentsupplies = 0
let runstart = false

registerWhen(onChatPacket(() => {
    if (!Settings.kuudrasupplies) return;
    runstart = true
}).setCriteria("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!"), () => Settings.kuudrasupplies)

registerWhen(onChatPacket(() => {
    if (!Settings.kuudrasupplies) return;
    ChatLib.chat(`${Settings.prefix} &d&lBalista Completed!`);
    Client.showTitle(" ", `&d&lBallista Completed!`, 0, 50, 0);
    World.playSound("random.anvil_use", 100, 1);
    runstart = false
}).setCriteria("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!"), () => Settings.kuudrasupplies)

registerWhen(register("chat", (player, current_chest, event) => {
    if (!Settings.kuudrasupplies) return;
    if (!runstart) return;
    if (currentsupplies >= current_chest) return
    currentsupplies = current_chest;
    ChatLib.chat(`${Settings.prefix} &a&lSupply recovered! &f(&a${current_chest}&f/&a6&f)`);
    World.playSound("random.orb", 100, 1.25);
    cancel(event);
}).setCriteria("${player} recovered one of Elle's supplies! (${current_chest}/6)"), () => Settings.kuudrasupplies)

registerHudElement(() => {
    if (!Settings.kuudrasupplies) return;
    let x = Number(Settings.kuudrasupplies_pos_x);
    let y = Number(Settings.kuudrasupplies_pos_y);
    let text = `&c&l${currentsupplies}&f&l/&a&l6`;
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("KuudraSupplies")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    if (!runstart) return;
    if (currentsupplies >= 6) return
    mainText.setScale(Settings.kuudrasupplies_scale);
    mainText.draw(x, y);
}, () => Settings.kuudrasupplies);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.kuudrasupplies) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("KuudraSupplies", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.kuudrasupplies_pos_x);
    let textY = Number(Settings.kuudrasupplies_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "KuudraSupplies"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("KuudraSupplies")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.kuudrasupplies_pos_x = "0";
        Settings.kuudrasupplies_pos_y = "0";
        Settings.kuudrasupplies_scale = "1";
        Settings.save()
    };
}), () => Settings.kuudrasupplies);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("KuudraSupplies")) return
    Settings.kuudrasupplies_pos_x = x - dragOffsetX;
    Settings.kuudrasupplies_pos_y = y - dragOffsetY;
}), () => Settings.kuudrasupplies);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("KuudraSupplies")) return
    Settings.kuudrasupplies_scale = Number(Settings.kuudrasupplies_scale)
    if (dir == 1) Settings.kuudrasupplies_scale += 0.05
    else Settings.kuudrasupplies_scale -= 0.05
}), () => Settings.kuudrasupplies);

registerWhen(register("worldUnload", () => {
    currentsupplies = 0
    runstart = false
}), () => Settings.kuudrasupplies) 