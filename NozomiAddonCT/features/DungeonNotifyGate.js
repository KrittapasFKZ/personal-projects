import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerHudElement } from "../utils/HUDManager";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerWhen } from "../utils/utils";

let start = false
let destroyed = false
let phase = 0
let titleMessage = " "
let hideAt = 0

export const CheckPhase = () => { return phase }
export const CheckStart = () => { return start }

registerWhen(register("chat", () => {
    if (Settings.notifyp3gate && start) {
        titleMessage = `&f&l&kA&r &d&lCORE &f&l&kA`
        hideAt = Date.now() + 2000;
        World.playSound("random.anvil_use", 100, 1);
        ChatLib.chat(`${Settings.prefix} &9&lTerminal Phase Completed!`)
        start = false
        phase = 99
    };
}).setCriteria("The Core entrance is opening!"), () => Settings.notifyp3gate && Dungeon.inDungeon);

registerWhen(register("chat", (message) => {
    if (message.includes(`(7/7)`) || message.includes(`(8/8)`)) {
        if (Settings.notifyp3gate && start) {
            if (!Dungeon.inDungeon) return;
            if (destroyed) {
                if (Settings.notifyp3gate_CustomTitle) {
                    titleMessage = `&a&lSAFE`
                    hideAt = Date.now() + 2000;
                };
                ChatLib.chat(`${Settings.prefix} &a&lSAFE! &dP&f${phase}`)
                World.playSound("random.burp", 100, 1);
                destroyed = false
            } else {
                if (phase < 3) {
                    phase += 1
                    ChatLib.chat(`${Settings.prefix} &c&lGATE NOT DESTROY! &dP&f${phase}`)
                    for (let i = 0; i < 3; i++) {
                        World.playSound("random.anvil_land", 100, 1);
                        if (Settings.notifyp3gate_CustomTitle) {
                            titleMessage = `&c&lGATE NOT DESTROY!`
                            hideAt = Date.now() + 2000;
                        };
                    };
                    destroyed = false
                };
            };
        };
    };
}).setCriteria("${message}"), () => Settings.notifyp3gate && Dungeon.inDungeon);

registerWhen(register("chat", () => {
    if (Settings.notifyp3gate && start) {
        destroyed = true;
        phase += 1
        ChatLib.chat(`${Settings.prefix} &a&lGATE DESTROYED!`);
        World.playSound("mob.irongolem.death", 100, 1);
        titleMessage = `&a&lGATE DESTROYED!`
        hideAt = Date.now() + 2000;
    };
}).setCriteria("&r&aThe gate has been destroyed!&r"), () => Settings.notifyp3gate && Dungeon.inDungeon);

registerWhen(onChatPacket(() => {
    if (!Settings.notifyp3gate) return
    destroyed = false
    start = true
    phase = 0
    ChatLib.chat(`${Settings.prefix} &9&lEntering Terminal Phase!`)
}).setCriteria("[BOSS] Storm: I should have known that I stood no chance."), () => Settings.notifyp3gate && Dungeon.inDungeon);

registerHudElement(() => {
    if (!Dungeon.inDungeon) return;
    if (!Settings.notifyp3gate) return
    if (!titleMessage || Date.now() > hideAt) return;

    let displayMessage = `${titleMessage}`;
    let scale = 3;
    let w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(displayMessage.removeFormatting()) * scale / 2;
    let h = Renderer.screen.getHeight() / 2.5 - 20;

    Renderer.scale(scale, scale);
    Renderer.drawStringWithShadow(displayMessage, w / scale, (h / scale));
    Renderer.scale(1 / scale, 1 / scale);
}, () => Settings.notifyp3gate);

registerWhen(register("worldUnload", () => {
    destroyed = false
    start = false
    phase = 0
}), () => Settings.notifyp3gate);