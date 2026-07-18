import Settings from "../config";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerHudElement } from "../utils/HUDManager";
import { getTime, getSecs, registerWhen } from "../utils/utils";
import { getWorld } from "../utils/worlds";
 
const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let mainText = new Text(` `, 5, 5).setScale(1).setShadow(true);
let overviewStr = " " 
let ticksElapsed = 0;
let runstart = false
let splittimer = {
    Zone: "Total",
    Total: 0,
    Supplies: 0,
    Build: 0,
    FuelStun: 0,
    Kuudra: 0
};

let isDragging = false;
let dragOffsetX = 0;
let dragOffsetY = 0;

registerWhen(register("packetReceived", () => {
    if (!Settings.kuudrarunoverview) return
    if (getWorld() !== "Kuudra") return;

    overviewStr = [
        `&e&lRun Overview &7(&cKuudra&7) &7(&a${getTime((ticksElapsed / 20) * 1000)}&7)`,
        ` &aSupplies: ${(splittimer.Supplies / 20).toFixed(2)}s`,
        ` &2Build: ${(splittimer.Build / 20).toFixed(2)}s`,
        ` &bFuel/Stun: ${(splittimer.FuelStun / 20).toFixed(2)}s`,
        ` &cKuudra: ${(splittimer.Kuudra / 20).toFixed(2)}s`
    ].join("\n")

    if (runstart) {
        ticksElapsed++;
        splittimer[splittimer.Zone]++;
    };

}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.kuudrarunoverview);

registerWhen(onChatPacket(() => {
    if (!Settings.kuudrarunoverview) return;
    ChatLib.chat(`${Settings.prefix} &c&lKuudra started!`)
    World.playSound("random.orb", 100, 1.25);
    runstart = true
}).setCriteria("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!"), () => Settings.kuudrarunoverview)

onChatPacket(() => { splittimer.Zone = "Supplies" }).setCriteria("[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!");
onChatPacket(() => { splittimer.Zone = "Build" }).setCriteria("[NPC] Elle: OMG! Great work collecting my supplies!");
onChatPacket(() => { splittimer.Zone = "FuelStun" }).setCriteria("[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!");
onChatPacket(() => { splittimer.Zone = "Kuudra" }).setCriteria("[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!");
onChatPacket(() => { splittimer.Zone = "Total"; runstart = false }).setCriteria("                               KUUDRA DOWN!");
onChatPacket(() => { splittimer.Zone = "Total"; runstart = false }).setCriteria("                                   DEFEAT");

registerHudElement(() => {
    if (!Settings.kuudrarunoverview) return;
    let x = Number(Settings.kuudrarunoverview_pos_x);
    let y = Number(Settings.kuudrarunoverview_pos_y);
    let text = `${overviewStr}`
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("KRO")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    mainText.setScale(Settings.kuudrarunoverview_scale);
    mainText.draw(x, y);
}, () => Settings.kuudrarunoverview);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.kuudrarunoverview) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("KRO", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.kuudrarunoverview_pos_x);
    let textY = Number(Settings.kuudrarunoverview_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "KRO"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("KRO")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.kuudrarunoverview_pos_x = "0";
        Settings.kuudrarunoverview_pos_y = "0";
        Settings.kuudrarunoverview_scale = "1";
        Settings.save()
    };
}), () => Settings.kuudrarunoverview);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("KRO")) return
    Settings.kuudrarunoverview_pos_x = x - dragOffsetX;
    Settings.kuudrarunoverview_pos_y = y - dragOffsetY;
}), () => Settings.kuudrarunoverview);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("KRO")) return
    Settings.kuudrarunoverview_scale = Number(Settings.kuudrarunoverview_scale)
    if (dir == 1) Settings.kuudrarunoverview_scale += 0.05
    else Settings.kuudrarunoverview_scale -= 0.05
}), () => Settings.kuudrarunoverview);

registerWhen(register("worldUnload", () => {
    ticksElapsed = 0;
    overviewStr = " "
    runstart = false
    splittimer = {
        Zone: "Total",
        Total: 0,
        Supplies: 0,
        Build: 0,
        FuelStun: 0,
        Kuudra: 0
    };
}), () => Settings.kuudrarunoverview);