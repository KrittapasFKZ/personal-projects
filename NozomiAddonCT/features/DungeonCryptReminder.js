import Dungeon from "../../BloomCore/dungeons/Dungeon";
import Settings from "../config";
import { registerHudElement } from "../utils/HUDManager";
import { registerWhen } from "../utils/utils";

let reminderSent = 0;
let complete = false
let cryptsNeeded = 0
let hideAt = 0;
let text = `&cNeed &e${cryptsNeeded} &cmore crypts!`;

registerWhen(register("step", () => {
    if (!Dungeon.inDungeon) return;
    if (!Settings.cryptreminder) return;

    const cryptsFound = Dungeon.crypts;
    const dungeonTime = Dungeon.seconds;

    if (!complete) {
        if (cryptsFound >= 5) {
            let elapsedMs = Date.now() - Dungeon.runStarted;
            let seconds = Math.floor(elapsedMs / 1000) % 60;
            let minutes = Math.floor(elapsedMs / (1000 * 60)) % 60;

            let timeTaken = `${minutes}m ${seconds}s`;

            complete = true
            reminderSent = 2;
            ChatLib.chat(`${Settings.prefix} &cCrypts Done! &f(&a${timeTaken}&f)`);
            ChatLib.command(`pc NA » Crypts Done! (${cryptsFound}/5) (${timeTaken})`, false);
            World.playSound("random.orb", 100, 1);
            text = `&aCrypts Done!`;
            hideAt = Date.now() + 3000;
        }
    }

    if (reminderSent <= 1) {
        if (cryptsFound >= 5) return;
        if ((reminderSent === 0 && dungeonTime >= 60 && dungeonTime < 70) ||
            (reminderSent === 1 && dungeonTime >= 90 && dungeonTime < 100)) {

            cryptsNeeded = 5 - cryptsFound;
            reminderSent += 1;

            ChatLib.command(`pc NA » We need ${cryptsNeeded} more Crypts! #${reminderSent}`, false);
            World.playSound("random.orb", 100, 1);
            text = `&cNeed &e${cryptsNeeded} &cmore crypts!`;
            hideAt = Date.now() + 3000;
        };
    };

}).setFps(1), () => Settings.cryptreminder && Dungeon.inDungeon);

registerHudElement(() => {
    if (!Settings.cryptreminder) return
    if (!Dungeon.inDungeon) return;
    if (Date.now() > hideAt) return;

    const scale = 3;
    const w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(text.removeFormatting()) * scale / 2;
    const h = Renderer.screen.getHeight() / 4;

    Renderer.scale(scale, scale);
    Renderer.drawStringWithShadow(text, w / scale, (h / scale));
    Renderer.scale(1 / scale, 1 / scale);
}, () => Settings.cryptreminder);

registerWhen(register("worldUnload", () => {
    reminderSent = 0
    complete = false
}), () => Settings.cryptreminder);