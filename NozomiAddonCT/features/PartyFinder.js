import Settings from "../config";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { TriggerTitle } from "../utils/customTitle";
import { highlightSlot } from "../../BloomCore/utils/Utils"
import { getWorld } from "../utils/worlds";
import { getTime, getSecs, registerWhen, stripRank } from "../utils/utils";
import { registerHudElement } from "../utils/HUDManager";
 
const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let createParty = false
let debounce = false
let partyAmount = 0
let partyStartTime = 0;

let playerName = "None"
let partylists = null
let isPartyFinder = false
let checkdebounce = false
let trycheck = false
let totalParties = 0
let markParties = 0
let markClass = "None"
let findingClass = "Healer"
let HUD_1 = new Text(" ", 5, 5).setScale(1).setShadow(true);
let HUD_2 = new Text(" ", 5, 5).setScale(1).setShadow(true);
let HUD_3 = new Text(" ", 5, 5).setScale(1).setShadow(true);
let HUD_4 = new Text(" ", 5, 5).setScale(1).setShadow(true);
let HUD_5 = new Text(" ", 5, 5).setScale(1).setShadow(true);
let mainText = new Text(" ", 5, 5).setScale(1).setShadow(true);

let partyArray = []
let overviewStr = " "

let classColors = {
    "Healer": "&dHealer",
    "Tank": "&aTank",
    "Mage": "&bMage",
    "Berserk": "&cBerserk",
    "Archer": "&6Archer",
    "None": "&7None"
};

function getLevelColor(level) {
    if (level >= 50) return "&4&l";
    if (level >= 45) return "&c";
    if (level >= 40) return "&6";
    if (level >= 35) return "&d";
    if (level >= 30) return "&9";
    if (level >= 25) return "&b";
    if (level >= 20) return "&2";
    if (level >= 15) return "&a";
    if (level >= 10) return "&e";
    if (level >= 5) return "&f";
    return "&7";
};

function TimeTook() {
    let elapsedMs = Date.now() - partyStartTime;
    let seconds = Math.floor(elapsedMs / 1000) % 60;
    let minutes = Math.floor(elapsedMs / (1000 * 60)) % 60;
    let hours = Math.floor(elapsedMs / (1000 * 60 * 60));
    let timeTaken = `${hours}h ${minutes}m ${seconds}s`;

    createParty = false
    partyAmount = 0
    partyStartTime = 0;

    ChatLib.chat(`${Settings.prefix} &aQueued for &b${timeTaken}`);
};

function CheckFull() {
    if (!trycheck) return;
    if (partyAmount >= 5) {
        TimeTook();
        trycheck = false
        TriggerTitle("&6&lPARTY FULL (5/5)");
        for (let i = 0; i <= 5; i++) {
            setTimeout(() => {
                World.playSound("note.pling", 100, 2);
            }, i * 200);
        };
    };
};

registerWhen(register("chat", (event) => {
    if (!Settings.dungeonpfinder) return
    if (createParty) return
    createParty = true
    debounce = false
    partyAmount = 0
    partyStartTime = Date.now();
    ChatLib.command("pl", false);
    ChatLib.chat(`${Settings.prefix} &aQueued to &dParty Finder`);
    TriggerTitle("&aQueued!");
    trycheck = true
    World.playSound("tile.piston.out", 100, 1);
    cancel(event)
}).setCriteria("&dParty Finder &r&f> &r&aYour party has been queued in the dungeon finder!&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", () => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    setTimeout(() => {
        CheckFull()
    }, 500)
}).setCriteria("Party Finder > Your dungeon group is full! Click here to warp to the dungeon!"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (player_name, player_class, event) => {
    if (!Settings.dungeonpfinder) return
    if (createParty) {
        if (partyAmount <= 4) {
            partyAmount += 1

            let match = player_class.match(/(\w+)\s+Level\s+(\d+)/);
            let formatted_class = player_class;
            if (match) {
                let cls = match[1].replace("b", "").trim();
                let lvl = parseInt(match[2]);
                let classColor = classColors[cls];
                let levelColor = getLevelColor(lvl);
                formatted_class = `${classColor} ${levelColor}${lvl}`;
            };

            let norank = stripRank(player_name.removeFormatting());
            const alreadyInParty = partyArray.some(p => p.name === norank);
            if (!alreadyInParty) {
                partyArray.push({ name: norank, color: "&a" });
            };

            ChatLib.chat(`${Settings.prefix} ${player_name} &8(${formatted_class}&8) &ejoined! &b${partyAmount}/5`);
            TriggerTitle(`${player_name} &ajoined! &b${partyAmount}/5`);
            World.playSound("random.door_open", 100, 2);
            setTimeout(() => {
                CheckFull()
            }, 500)
        };
    } else {
        createParty = true
        trycheck = true
        debounce = false
        partyAmount = 0
        partyStartTime = Date.now();
        ChatLib.command("pl", false);
        ChatLib.chat(`${Settings.prefix} &aJoined &dParty Finder`);
        TriggerTitle("&aJoined!");
        World.playSound("tile.piston.out", 100, 1);
    };
    cancel(event)
}).setCriteria("&dParty Finder &r&f> ${player_name} &r&ejoined the dungeon group! ${player_class}"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (player_name, player_class, event) => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    if (partyAmount <= 4) {
        let norank = stripRank(player_name.removeFormatting());
        const alreadyInParty = partyArray.some(p => p.name === norank);
        if (!alreadyInParty) {
            partyArray.push({ name: norank, color: "&a" });
        };
        partyAmount += 1
        ChatLib.chat(`${Settings.prefix} ${player_name} &ejoined! &b${partyAmount}/5`);
        TriggerTitle(`${player_name} &ajoined! &b${partyAmount}/5`);
        World.playSound("random.door_open", 100, 2);
        setTimeout(() => {
            CheckFull()
        }, 500)
    };
    cancel(event)
}).setCriteria("${player_name} &r&ejoined the party.&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (player_name, player_class, event) => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    let norank = stripRank(player_name.removeFormatting());
    const alreadyInParty = partyArray.some(p => p.name === norank);
    if (alreadyInParty) {
        partyArray = partyArray.filter(p => p.name !== norank);
    };
    partyAmount -= 1
    TriggerTitle(`${player_name} &cleft! &b${partyAmount}/5`);
    ChatLib.chat(`${Settings.prefix} ${player_name} &cleft the party! &b${partyAmount}/5`);
    World.playSound("note.snare", 100, 1);
    cancel(event)
}).setCriteria("${player_name} &r&ehas left the party.&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (player_name, player_class, event) => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    let norank = stripRank(player_name.removeFormatting());
    const alreadyInParty = partyArray.some(p => p.name === norank);
    if (alreadyInParty) {
        partyArray = partyArray.filter(p => p.name !== norank);
    };
    partyAmount -= 1
    TriggerTitle(`${player_name} &cleft! &b${partyAmount}/5`);
    ChatLib.chat(`${Settings.prefix} ${player_name} &cleft the party! &b${partyAmount}/5`);
    World.playSound("note.snare", 100, 1);
    cancel(event)
}).setCriteria("${player_name} &r&ehas been removed from the party.&r"), () => Settings.dungeonpfinder);

function Delisted(event, msg) {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    partyArray = []
    cancel(event)
    TimeTook()
    trycheck = false
    TriggerTitle(`&cDe-listed!`);
    ChatLib.chat(`${Settings.prefix} ${msg}`);
    World.playSound("tile.piston.in", 100, 1);
};

registerWhen(register("chat", (event) => {
    Delisted(event, "&cYou left the party!!")
}).setCriteria("&eYou left the party.&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (event) => {
    Delisted(event, "&cDe-listed from &dParty Finder")
}).setCriteria("&dParty Finder &r&f> &r&aYour group has been de-listed!&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (event) => {
    Delisted(event, "&cParty was disbanded!")
}).setCriteria("&cThe party was disbanded because all invites expired and the party was empty.&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (leader, event) => {
    Delisted(event, "&cParty was disbanded!")
}).setCriteria("${leader} &r&ehas disbanded the party!&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (event) => {
    Delisted(event, "&cParty has been removed from &dParty Finder")
}).setCriteria("&dParty Finder &r&f> &r&cYour group has been removed from the party finder because the leader left SkyBlock!&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (event) => {
    Delisted(event, "&cParty has been removed from &dParty Finder")
}).setCriteria("&dParty Finder &r&f> &r&cYour group has been removed from the party finder!&r"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (amount) => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) return
    if (debounce) return
    debounce = true
    partyAmount = 0
    ChatLib.chat(`${Settings.prefix} &aCurrent Members &b${amount}/5`);
    partyAmount = Number(amount)
}).setCriteria("Party Members (${amount})"), () => Settings.dungeonpfinder);

registerWhen(register("worldLoad", () => {
    checkdebounce = false;
    playerName = Player.getName()
}), () => Settings.dungeonpfinder);

registerWhen(register("worldUnload", () => {
    checkdebounce = false;
    playerName = Player.getName()
}), () => Settings.dungeonpfinder);

registerWhen(register("guiRender", () => {
    if (isPartyFinder) {
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let mainX = chestX + 100;
        let mainY = chestY - 10;
        mainText.setScale(1);
        mainText.setString(`&e&lParty Highlight\n &fYour Class: ${classColors[markClass]}\n &fMark Missing: ${classColors[findingClass]}\n &fParty Amount: &a${markParties}/${totalParties}\n\n&e&lChange Mark`);
        mainText.draw(mainX, mainY);
        ///// HUD_1
        if (findingClass == "Tank") {
            HUD_1.setString(` &a&l> Tank`);
        } else {
            HUD_1.setString(` &aTank`);
        }
        if (findingClass == "Healer") {
            HUD_2.setString(`  &d&l> Healer`);
        } else {
            HUD_2.setString(` &dHealer`);
        }
        if (findingClass == "Berserk") {
            HUD_3.setString(`  &c&l> Berserk`);
        } else {
            HUD_3.setString(` &cBerserk`);
        }
        if (findingClass == "Archer") {
            HUD_4.setString(`  &6&l> Archer`);
        } else {
            HUD_4.setString(` &6Archer`);
        }
        if (findingClass == "Mage") {
            HUD_5.setString(`  &b&l> Mage`);
        } else {
            HUD_5.setString(` &bMage`);
        }
        HUD_1.draw(mainX, mainY + 60);
        HUD_2.draw(mainX, mainY + 70);
        HUD_3.draw(mainX, mainY + 80);
        HUD_4.draw(mainX, mainY + 90);
        HUD_5.draw(mainX, mainY + 100);
    };
}), () => Settings.dungeonpfinder && getWorld() == "Dungeon Hub");

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (isPartyFinder) {
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let mainX = chestX + 100;
        let textHeight = 10;
        if (isDown && x >= mainX && x <= mainX + 83 && y >= (chestY + 50) && y <= (chestY + 50) + textHeight) {
            if (findingClass == "Tank") return;
            World.playSound("gui.button.press", 50, 1);
            findingClass = "Tank";
        }
        if (isDown && x >= mainX && x <= mainX + 83 && y >= (chestY + 60) && y <= (chestY + 60) + textHeight) {
            if (findingClass == "Healer") return;
            World.playSound("gui.button.press", 50, 1);
            findingClass = "Healer";
        }
        if (isDown && x >= mainX && x <= mainX + 83 && y >= (chestY + 70) && y <= (chestY + 70) + textHeight) {
            if (findingClass == "Berserk") return;
            World.playSound("gui.button.press", 50, 1);
            findingClass = "Berserk";
        }
        if (isDown && x >= mainX && x <= mainX + 83 && y >= (chestY + 80) && y <= (chestY + 80) + textHeight) {
            if (findingClass == "Archer") return;
            World.playSound("gui.button.press", 50, 1);
            findingClass = "Archer";
        }
        if (isDown && x >= mainX && x <= mainX + 83 && y >= (chestY + 90) && y <= (chestY + 90) + textHeight) {
            if (findingClass == "Mage") return;
            World.playSound("gui.button.press", 50, 1);
            findingClass = "Mage";
        }
    };
}), () => Settings.dungeonpfinder && getWorld() == "Dungeon Hub");

registerWhen(register("guiRender", () => {
    if (isPartyFinder) {
        let gui = Client.currentGui.get();
        totalParties = partylists.length
        markParties = 0
        partylists.map((a, index) => {
            let MarkParty = false
            let HasMe = false
            let itemName = a.getName();
            let itemLore = a.getLore();
            let originalSlot = index + 10;
            if (originalSlot >= 17) {
                originalSlot = originalSlot + 2
            };
            if (originalSlot >= 26) {
                originalSlot = originalSlot + 2
            };
            for (let i = itemLore.length - 1; i >= 0; i--) {
                let line = itemLore[i].removeFormatting();
                if (line.includes(`: ${findingClass} (`)) {
                    MarkParty = true
                };
                if (line.includes(playerName)) {
                    HasMe = true
                };
            };
            if (HasMe) {
                highlightSlot(gui, originalSlot, 0, 0.66, 0.66, 1, true);
            } else {
                if (!MarkParty) {
                    markParties += 1
                    highlightSlot(gui, originalSlot, 0, 1, 0, 1, true);
                } else {
                    highlightSlot(gui, originalSlot, 1, 0, 0, 1, true);
                };
            }
        })
    };
}), () => Settings.dungeonpfinder && getWorld() == "Dungeon Hub");

registerWhen(register("guiClosed", () => {
    playerName = Player.getName()
    isPartyFinder = false
    partylists = null
    totalParties = 0
    markParties = 0
}), () => Settings.dungeonpfinder && getWorld() == "Dungeon Hub");

registerWhen(register("guiOpened", () => {
    isPartyFinder = false
    playerName = Player.getName()
    if (Settings.dungeonpfinder) {
        if (checkdebounce) return;
        checkdebounce = true;
        Client.scheduleTask(2, () => { 
            try {
                let inv = Player.getContainer();
                if (!inv) return;
                let invName = inv.getName();
                let newName = invName.removeFormatting()
                if (invName == "container") return;
                if (newName.includes("Party Finder")) {
                    isPartyFinder = true
                    let items = inv.getItems().filter(a => a && a?.getID() == 397)
                    partylists = items.filter(a => a && a?.getName().removeFormatting().includes("'s Party"))
                };
                if (newName.includes("Catacombs Gate")) {
                    let nugget = inv.getStackInSlot(45)
                    let lore = nugget.getLore()
                    let classline = lore[3].removeFormatting()
                    let getclass = classline.replace("Currently Selected: ", "").trim()
                    markClass = getclass
                };
            } finally {
                checkdebounce = false;
            }
        });
    };
}), () => Settings.dungeonpfinder && getWorld() == "Dungeon Hub");

registerWhen(register("packetReceived", () => {
    if (!Settings.dungeonpfinder) return
    if (!createParty) {
        overviewStr = " ";
        return;
    };

    CheckFull()

    overviewStr = [
        partyArray.length >= 5 ? (`&d&lParty Finder &7(&a${partyArray.length}&7/&a5&7) &7(&b${getTime(Date.now() - partyStartTime)}&7)`) : (`&d&lParty Finder &7(&c${partyArray.length}&7/&a5&7) &7(&b${getTime(Date.now() - partyStartTime)}&7)`),
        partyArray.map((p, i) => ` &7${i + 1}) ${p.color}${p.name}`).join("\n")
    ].join("\n")

}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.dungeonpfinder);

registerWhen(register("chat", (player, event) => {
    if (!createParty) return
    partyArray = []
    let norank = stripRank(player.removeFormatting().trim());
    const alreadyInParty = partyArray.some(p => p.name === norank);
    if (!alreadyInParty) {
        partyArray.push({ name: norank, color: "&6" });
    };
}).setCriteria("Party Leader: ${player} ●"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (members, event) => {
    if (!createParty) return
    let memberList = members
        .split("●")
        .map(name => stripRank(name.removeFormatting().trim()))
        .filter(name => name.length > 0);
    memberList.forEach(name => {
        const alreadyInParty = partyArray.some(p => p.name === name);
        if (!alreadyInParty) {
            partyArray.push({ name: name, color: "&a" });
        }
    });
}).setCriteria("Party Members: ${members}"), () => Settings.dungeonpfinder);

registerWhen(register("chat", (members, event) => {
    if (!createParty) return
    let memberList = members
        .split("●")
        .map(name => stripRank(name.removeFormatting().trim()))
        .filter(name => name.length > 0);
    memberList.forEach(name => {
        const alreadyInParty = partyArray.some(p => p.name === name);
        if (!alreadyInParty) {
            partyArray.push({ name: name, color: "&b" });
        }
    });
}).setCriteria("Party Moderators: ${members}"), () => Settings.dungeonpfinder);

registerHudElement(() => {
    if (!Settings.dungeonpfinder) return;
    let x = Number(Settings.dungeonpfinder_pos_x);
    let y = Number(Settings.dungeonpfinder_pos_y);
    let text = `${overviewStr}`
    mainText.setString(text);
    if (Settings.editUI) {
        let textWidth = mainText.getWidth();
        let textHeight = mainText.getHeight();
        if (Settings.editUIName.includes("PFinderAddon")) {
            Renderer.drawRect(Renderer.color(0, 255, 0, 100), x - 3, y - 3, textWidth + 6, textHeight + 6);
        } else {
            Renderer.drawRect(Renderer.color(255, 255, 255, 150), x - 3, y - 3, textWidth + 6, textHeight + 6);
        };
    };
    mainText.setScale(Settings.dungeonpfinder_scale);
    mainText.draw(x, y);
}, () => Settings.dungeonpfinder);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.dungeonpfinder) return;
    if (!Settings.editUI) return
    if (!isDown) {
        Settings.editUIName = Settings.editUIName.replace("PFinderAddon", " ").trim();
        isDragging = false;
        return;
    };

    let textX = Number(Settings.dungeonpfinder_pos_x);
    let textY = Number(Settings.dungeonpfinder_pos_y);
    let textWidth = mainText.getWidth();
    let textHeight = mainText.getHeight();

    if (button === 0) {
        if (x >= textX && x <= textX + textWidth && y >= textY && y <= textY + textHeight) {
            isDragging = true;
            dragOffsetX = x - textX;
            dragOffsetY = y - textY;
            if (Settings.editUIName == "") Settings.editUIName += "PFinderAddon"
        };
    } else if (button === 1) {
        if (!Settings.editUIName.includes("PFinderAddon")) return
        World.playSound("tile.piston.in", 100, 1);
        Settings.dungeonpfinder_pos_x = "0";
        Settings.dungeonpfinder_pos_y = "0";
        Settings.dungeonpfinder_scale = "1";
        Settings.save()
    };
}), () => Settings.dungeonpfinder);

registerWhen(register("dragged", (dx, dy, x, y) => {
    if (!isDragging) return;
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("PFinderAddon")) return
    Settings.dungeonpfinder_pos_x = x - dragOffsetX;
    Settings.dungeonpfinder_pos_y = y - dragOffsetY;
}), () => Settings.dungeonpfinder);

registerWhen(register("scrolled", (x, y, dir) => {
    if (!Settings.editUI) return
    if (!Settings.editUIName.includes("PFinderAddon")) return
    Settings.dungeonpfinder_scale = Number(Settings.dungeonpfinder_scale)
    if (dir == 1) Settings.dungeonpfinder_scale += 0.05
    else Settings.dungeonpfinder_scale -= 0.05
}), () => Settings.dungeonpfinder);