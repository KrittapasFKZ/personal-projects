import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerHudElement } from "../utils/HUDManager";
import { registerWhen } from "../utils/utils";

let playersStackingMelody = [];
let playerName = "";
let melodyProgress = "";
let furthestAlongMelody = 0;
let lastPhase = 0;

let GoldorPhase = {
    phase: 1,
    start: false
};

let colorClasses = {
    Tank: "&2",
    Healer: "&d",
    Archer: "&6",
    Berserk: "&4",
    Mage: "&b"
};

function resetMelody() {
    playersStackingMelody.length = 0;
    melodyProgress = "";
    furthestAlongMelody = 0;
    playerName = "";
}

registerWhen(register("step", () => {
    if (!Dungeon.inDungeon) return;
    if (!Settings.dungeonmelodywarning) return
    if (GoldorPhase.phase !== lastPhase) {
        resetMelody();
        lastPhase = GoldorPhase.phase;
    }
}).setFps(1), () => Settings.dungeonmelodywarning && Dungeon.inDungeon);

registerWhen(register("chat", (message) => {
    if (!GoldorPhase.start) return;
    if (!Settings.dungeonmelodywarning) return
    if (!Dungeon.inDungeon) return;

    const melodyMatch = message.match(/^Party >[\s\[\w+\]]* (\w+): .*(\d\/\d|\d\d%)$/);

    if (melodyMatch) {
        const playerMatched = melodyMatch[1];

        let playerClass = Dungeon.classes[playerMatched];
        if (!playerClass) {
            return
        };

        if (playerMatched === Player.getName()) return;

        if (!playersStackingMelody.includes(playerMatched)) {
            ChatLib.chat(`${Settings.prefix} ${colorClasses[playerClass]}${playerMatched} (${playerClass[0]}) &ahas Melody!`);
            playersStackingMelody.push(playerMatched);
        }

        let progress = melodyMatch[2];
        if (progress.includes("%")) {
            const percentage = parseInt(progress);
            if (!isNaN(percentage) && percentage >= 25) progress = Math.floor(percentage / 25) + "/4";
        }

        if (progress > furthestAlongMelody || furthestAlongMelody === 0) {
            playerName = playerMatched;
            furthestAlongMelody = progress;
            melodyProgress = progress;
            World.playSound("note.hat", 100, 1);
        }
        return;
    };

    const terminalMatch = message.match(/^(\w+) activated a terminal! \(\d+\/\d+\)$/);
    if (!terminalMatch) return;
    const completedPlayer = terminalMatch[1];

    const index = playersStackingMelody.indexOf(completedPlayer);
    if (index > -1) playersStackingMelody.splice(index, 1);
    if (completedPlayer === playerName) resetMelody();
}).setCriteria("${message}"), () => Settings.dungeonmelodywarning && Dungeon.inDungeon);

registerHudElement(() => {
    if (!Dungeon.inDungeon) return;
    if (!Settings.dungeonmelodywarning) return
    if (!melodyProgress) return;

    let playerClass = Dungeon.classes[playerName];
    if (!playerClass) {
        return
    };

    let displayMessage = `${colorClasses[playerClass]}${playerName} (${playerClass[0]}) &ehas Melody! ${melodyProgress}`;
    let scale = 1.5;
    let w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(displayMessage.removeFormatting()) * scale / 2;
    let h = Renderer.screen.getHeight() / 4;

    Renderer.scale(scale, scale);
    Renderer.drawStringWithShadow(displayMessage, w / scale, (h / scale));
    Renderer.scale(1 / scale, 1 / scale);
}, () => Settings.dungeonmelodywarning);

registerWhen(onChatPacket(() => {
    if (!Settings.dungeonmelodywarning) return
    GoldorPhase.start = true
    GoldorPhase.phase = 1
}).setCriteria("[BOSS] Storm: I should have known that I stood no chance."), () => Settings.dungeonmelodywarning && Dungeon.inDungeon);

registerWhen(register("chat", () => {
    if (!Settings.dungeonmelodywarning) return
    if (GoldorPhase.start) {
        GoldorPhase.start = false
    };
}).setCriteria("The Core entrance is opening!"), () => Settings.dungeonmelodywarning && Dungeon.inDungeon)

registerWhen(register("chat", (message) => {
    if (message.includes(`(7/7)`) || message.includes(`(8/8)`)) {
        if (Settings.dungeonmelodywarning && GoldorPhase.start) {
            if (!Dungeon.inDungeon) return;
            GoldorPhase.phase += 1
        };
    };
}).setCriteria("${message}"), () => Settings.dungeonmelodywarning && Dungeon.inDungeon);

registerWhen(register("worldLoad", () => {
    resetMelody();
    lastPhase = 0;
    GoldorPhase.start = false
    GoldorPhase.phase = 1
}), () => Settings.dungeonmelodywarning);