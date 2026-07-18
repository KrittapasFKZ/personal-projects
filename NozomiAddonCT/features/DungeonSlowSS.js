import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { TriggerTitle } from "../utils/customTitle";
import { stripRank } from "../../BloomCore/utils/Utils";

let start = false
let debounce = false
let protect_my_ear = false

let currentGoldorPhase = 0;

registerWhen(register("chat", message => {
    if (!Dungeon.inDungeon) return;
    if (message === "[BOSS] Storm: I should have known that I stood no chance.") currentGoldorPhase = 1;
    if ((message.includes("(7/7)") || message.includes("(8/8)")) && !message.includes(":")) currentGoldorPhase += 1;
}).setCriteria("${message}"), () => Settings.positionmessage && Dungeon.inDungeon);

registerWhen(register("HitBlock", (event) => {
    if (Settings.slowSS) {
        let x = event.getX();
        let y = event.getY();
        let z = event.getZ();
        if (event.getType().name == "Block of Emerald") {
            if (x === 111 && y == 121 && z == 96) {
                handleProc()
            } else {
                if (x === 111 && y == 122 && z == 96) {
                    handleProc()
                } else {
                    if (x === -1 && y == 64 && z == 135) {
                        handleProc()
                    } else {
                        if (x === -1 && y == 100 && z == 32) {
                            handleProc()
                        } else {
                            if (x === 111 && y == 122 && z == 91) {
                                handleProc()
                            } else {

                            };
                        };
                    };
                };
            };
        };
    };
}), () => Settings.slowSS && Dungeon.inDungeon);

let ResetArray = ["ssr", "rs", "reset", "ssslow", "slow", "ssrs", "ss rs", "ss slow", "ss reset"]

registerWhen(register("chat", (sender, message) => {
    if (!Settings.slowSS) return;
    if (!Dungeon.inDungeon) return;
    const strippedPlayer = stripRank(sender.removeFormatting());
    if (strippedPlayer === Player.getName()) return;
    const playerClass = Dungeon.classes[strippedPlayer];
    if (playerClass !== "Healer") return;
    if (!start) return;
    if (currentGoldorPhase == 1) {
        let chatMessage = message.removeFormatting().toLowerCase()
        if (ResetArray.some(word => chatMessage.includes(word))) {
            if (!protect_my_ear) {
                ChatLib.chat(`${Settings.prefix} &c&lSlowSS! &cGO SAFE SPOT!`);
                TriggerTitle("&c&lSS SLOW")
                World.playSound("mob.ghast.scream", 100, 1);
            };
        };
    };
}).setCriteria("&r&9Party &8> ${sender}: ${message}&r"), () => Settings.slowSS && Dungeon.inDungeon)

registerWhen(register("chat", () => {
    if (!Settings.slowSS) return
    if (start) {
        start = false;
    };
}).setCriteria("The Core entrance is opening!"), () => Settings.slowSS && Dungeon.inDungeon)

registerWhen(onChatPacket(() => {
    if (!Settings.slowSS) return
    start = true
}).setCriteria("[BOSS] Storm: I should have known that I stood no chance."), () => Settings.slowSS && Dungeon.inDungeon)

function handleProc() {
    if (debounce) return
    debounce = true
    protect_my_ear = true
    ChatLib.chat(`${Settings.prefix} &aAnnounced &b&lSlowSS!`);
    ChatLib.command("pc NA » SS SLOW! Go safe spot!", false);
    World.playSound("mob.irongolem.hit", 100, 1);
    setTimeout(() => {
        debounce = false
        protect_my_ear = false
    }, 750);
};

registerWhen(register("worldUnload", () => {
    start = false
    currentGoldorPhase = 0
}), () => Settings.slowSS)