import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerHudElement } from "../utils/HUDManager";
import { registerWhen } from "../utils/utils";

const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let ticksElapsed = 0;
let countdownTicks = 104;
let active = false;
let lastSecond = -1;

registerWhen(register("packetReceived", () => {
    if (!Settings.p3timer) return
    if (!active) return;

    ticksElapsed++;

    const remainingTicks = countdownTicks - ticksElapsed;
    const remainingSeconds = (remainingTicks / 20).toFixed(2);
    const currentSecond = Math.floor(remainingSeconds);

    if (remainingTicks <= 0) {
        active = false;
        return;
    }

    if (currentSecond !== lastSecond) {
        lastSecond = currentSecond;
        World.playSound("note.hat", 100, 1);
    }
}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.p3timer && Dungeon.inDungeon);

registerHudElement(() => {
    if (!Settings.p3timer) return
    if (!active) return;

    const remainingTicks = countdownTicks - ticksElapsed;
    const remainingSeconds = (remainingTicks / 20).toFixed(2);

    let color = "&a";
    if (remainingTicks <= 80) color = "&e";
    if (remainingTicks <= 40) color = "&c";

    const text = `${color}${remainingSeconds}`;
    const scale = 2;
    const w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(text.removeFormatting()) * scale / 2;
    const h = Renderer.screen.getHeight() / 2;

    Renderer.scale(scale, scale);
    Renderer.drawStringWithShadow(text, w / scale, (h / scale) + 10);
    Renderer.scale(1 / scale, 1 / scale);
}, () => Settings.p3timer);

registerWhen(onChatPacket(() => {
    if (!Settings.p3timer) return
    ticksElapsed = 0;
    active = true;
    lastSecond = -1;
}).setCriteria("[BOSS] Storm: I should have known that I stood no chance."), () => Settings.p3timer && Dungeon.inDungeon);