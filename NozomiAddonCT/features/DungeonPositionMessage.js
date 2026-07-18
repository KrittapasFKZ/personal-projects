import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { isPlayerInArea, registerWhen } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";
import { stripRank } from "../../BloomCore/utils/Utils";

let positionMessage = " "
let hideAt = 0;

let colorClasses = {
    Tank: "&2",
    Healer: "&d",
    Archer: "&6",
    Berserk: "&4",
    Mage: "&b"
};
 
function playSound() {
    let count = 0;
    const playNext = () => {
        if (count >= 120) return;
        World.playSound("random.orb", 0.2, 1.25);
        count++;
        setTimeout(playNext, 3);
    };
    playNext();
}

const BossStatus = Java.type("net.minecraft.entity.boss.BossStatus");

function getIsInBoss(boss) {
    const bossName = BossStatus.field_82827_c;
    if (!bossName) return false;
    return bossName.removeFormatting().includes(boss);
};

let currentGoldorPhase = 0;

registerWhen(register("chat", message => {
    if (!Dungeon.inDungeon) return;
    if (message === "[BOSS] Storm: I should have known that I stood no chance.") currentGoldorPhase = 1;
    if ((message.includes("(7/7)") || message.includes("(8/8)")) && !message.includes(":")) currentGoldorPhase += 1;
}).setCriteria("${message}"), () => Settings.positionmessage && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    currentGoldorPhase = 0
    positionMessage = " "
    hideAt = 0;
}), () => Settings.positionmessage);

const positionDefinitions = [
    {
        id: 'AtP2',
        messageText: 'At P2!',
        onScreenText: 'At P2!',
        checkCondition: (playerClass) => getIsInBoss("Maxor") && playerClass !== "Healer",
        checkPosition: (entity) => entity.getY() < 205 && entity.getY() > 164,
        validMessages: ["at p2", "in p2"]
    },
    {
        id: 'AtSS',
        messageText: 'At SS!',
        onScreenText: 'At SS!',
        checkCondition: () => getIsInBoss("Storm") || getIsInBoss("Goldor"),
        checkPosition: (entity) => isPlayerInArea(106, 110, 118, 122, 92, 96, entity),
        validMessages: ["at ss", "at simon says"]
    },
    {
        id: 'AtEE2',
        messageText: 'At Pre Enter 2!',
        onScreenText: 'At Pre Enter 2!',
        checkCondition: () => currentGoldorPhase === 1,
        checkPosition: (entity) => isPlayerInArea(49, 58, 108, 115, 129, 133, entity),
        validMessages: ["early enter 2", "pre enter 2", "at ee2", "entered 3.2"]
    },
    {
        id: 'AtEE3',
        messageText: 'At Pre Enter 3!',
        onScreenText: 'At Pre Enter 3!',
        checkCondition: () => currentGoldorPhase === 2,
        checkPosition: (entity) => isPlayerInArea(0, 4, 108, 115, 98, 107, entity),
        validMessages: ["early enter 3", "pre enter 3", "at ee3", "entered 3.3"]
    },
    {
        id: 'AtCore',
        messageText: 'At Core!',
        onScreenText: 'At Core!',
        checkCondition: () => currentGoldorPhase === 2 || currentGoldorPhase === 3,
        checkPosition: (entity) => isPlayerInArea(52, 56, 113, 117, 49, 53, entity),
        validMessages: ["at core", "pre enter 4", "early enter 4", "at ee4", "entered 3.4"]
    },
    {
        id: 'InGoldorTunnel',
        messageText: 'Inside Goldor Tunnel!',
        onScreenText: 'Inside Goldor Tunnel!',
        checkCondition: () => currentGoldorPhase === 4,
        checkPosition: (entity) => isPlayerInArea(41, 68, 110, 150, 59, 117, entity),
        validMessages: ["in goldor tunnel", "inside goldor tunnel", "in core", "entered 3.5", "at ee5", "at pre enter 5"]
    },
    {
        id: 'AtMid',
        messageText: 'At Mid!',
        onScreenText: 'At Mid!',
        checkCondition: () => getIsInBoss("Necron"),
        checkPosition: (entity) => isPlayerInArea(47, 61, 64, 75, 69, 83, entity),
        validMessages: ["at mid", "in mid"]
    },
    {
        id: 'Ati4Entry',
        messageText: 'At i4 Entry!',
        onScreenText: 'At i4 Entry!',
        checkCondition: (playerClass) => getIsInBoss("Storm") && playerClass !== "Healer",
        checkPosition: (entity) => isPlayerInArea(91, 93, 129, 133, 44, 46, entity),
        validMessages: ["i4 entry"]
    },
    {
        id: 'AtP5',
        messageText: 'At P5!',
        onScreenText: 'At P5!',
        checkCondition: (playerClass) => getIsInBoss("Necron") && playerClass === "Healer",
        checkPosition: (entity) => entity.getY() < 50 && entity.getY() > 4,
        validMessages: ["at p5", "in p5"]
    }
];

const lastLocation = {
    AtP2: false,
    AtSS: false,
    AtEE2: false,
    AtEE3: false,
    AtCore: false,
    InGoldorTunnel: false,
    AtMid: false,
    Ati4Entry: false,
    AtP5: false
};

registerWhen(register("tick", () => {
    if (!World.isLoaded() || Player.getPlayer().func_82150_aj()) return;
    const playerName = Player.getName();
    let playerClass = Dungeon.classes[playerName];
    if (!playerClass) return;
    positionDefinitions.forEach(position => {
        if (!lastLocation[position.id] &&
            position.checkCondition(playerClass) &&
            position.checkPosition(Player)) {
            lastLocation[position.id] = true;
            let fullName = `${colorClasses[playerClass]}${playerName} (${playerClass[0]})`
            setTimeout(() => ChatLib.command(`pc ${position.messageText}`, false), 200);
            ChatLib.chat(`${Settings.prefix} ${fullName} &b${position.messageText}`);
        }
    });
}), () => Settings.positionmessage && Dungeon.inDungeon);

registerWhen(register("tick", () => {
    if (!World.isLoaded() || !Dungeon.inDungeon) return;

    World.getAllPlayers().forEach(entity => {
        if (Dungeon.bossEntry === null) return;
        if (entity.getPing() !== 1) return;

        const playerName = entity.getName();
        if (playerName === Player.getName()) return;

        const playerClass = Dungeon.classes[playerName];
        if (!playerClass) return;

        let fullName = `${colorClasses[playerClass]}${playerName} (${playerClass[0]})`

        positionDefinitions.forEach(position => {
            if (!lastLocation[position.id] &&
                position.checkCondition(playerClass) &&
                position.checkPosition(entity)) {
                lastLocation[position.id] = true;
                positionMessage = `${fullName} &a${position.onScreenText}`
                hideAt = Date.now() + 2000;
                playSound();
                ChatLib.chat(`${Settings.prefix} &e${fullName} &b${position.messageText}`);
            }
        });
    });
}), () => Settings.positionmessage && Dungeon.inDungeon);

registerWhen(register("chat", (player, message) => {
    const strippedPlayer = stripRank(player);
    if (strippedPlayer === Player.getName()) return;

    const msg = message.toLowerCase();
    const playerClass = Dungeon.classes[strippedPlayer];
    if (!playerClass) return;

    let fullName = `${colorClasses[playerClass]}${strippedPlayer} (${playerClass[0]})`

    positionDefinitions.forEach(position => {
        if (!lastLocation[position.id] &&
            position.checkCondition(playerClass) &&
            position.validMessages.some(term => msg.includes(term))) {
            lastLocation[position.id] = true;
            positionMessage = `${fullName} &a${position.onScreenText}`
            hideAt = Date.now() + 2000;
            playSound();
            ChatLib.chat(`${Settings.prefix} &e${fullName} &b${position.messageText}`);
        }
    });
}).setCriteria("Party > ${player}: ${message}"), () => Settings.positionmessage && Dungeon.inDungeon);

registerHudElement(() => {
    if (!Dungeon.inDungeon) return;
    if (!Settings.positionmessage) return
    if (!positionMessage || Date.now() > hideAt) return;

    let displayMessage = `${positionMessage}`;
    let scale = 1.5;
    let w = Renderer.screen.getWidth() / 2 - Renderer.getStringWidth(displayMessage.removeFormatting()) * scale / 2;
    let h = Renderer.screen.getHeight() / 4 - 20;

    Renderer.scale(scale, scale);
    Renderer.drawStringWithShadow(displayMessage, w / scale, (h / scale));
    Renderer.scale(1 / scale, 1 / scale);
}, () => Settings.positionmessage);

registerWhen(register("worldUnload", () => Object.keys(lastLocation).forEach(key => lastLocation[key] = false)), () => Settings.positionmessage);