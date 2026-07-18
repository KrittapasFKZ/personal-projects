import Settings from "../config";
import { getSkyblockItemID, getItemIDByName } from "../utils/getItems";
import { highlightSlot } from "../../BloomCore/utils/Utils"
import { checkRng } from "./DungeonRNGJumpscare"
import { registerWhen } from "../utils/utils";

let titletext = new Text("&r").setAlign("CENTER").setScale(1).setShadow(true);

let chestName = ["Wood", "Gold", "Diamond", "Emerald", "Obsidian", "Bedrock"];
let chestNameOb = ["Wood Chest", "Gold Chest", "Diamond Chest", "Emerald Chest", "Obsidian Chest", "Bedrock Chest"];
let textitems = "&r"
let textprofit = "&r"
let rnglist = "&r"
let rngItem = null
let itemsWithPrice = [];
let runcroesus = [];
let sortWithPrice = [];
let ischest = false
let ischestMain = false
let iscroesus = false
let textitemsMain = "&r"
let openedChest = 0
let debounce = false

let blacklistItems = [
    "DUNGEON_DISC_1",
    "DUNGEON_DISC_2",
    "DUNGEON_DISC_3",
    "DUNGEON_DISC_4",
    "DUNGEON_DISC_5",
    "GOLDOR_THE_FISH",
    "MAXOR_THE_FISH",
    "STORM_THE_FISH"
];

let shards = {
    "Wither Shard": "SHARD_WITHER",
    "Power Dragon Shard": "SHARD_POWER_DRAGON",
    "Apex Dragon Shard": "SHARD_APEX_DRAGON",
    "Thorn Shard": "SHARD_THORN",
    "Necron Dye": "DYE_NECRON",
    "Livid Dye": "DYE_LIVID"
};

let dungeonChestName = {
    "Wood Chest": "&fWood Chest",
    "Gold Chest": "&6Gold Chest",
    "Diamond Chest": "&bDiamond Chest",
    "Emerald Chest": "&2Emerald Chest",
    "Obsidian Chest": "&5Obsidian Chest",
    "Bedrock Chest": "&8Bedrock Chest",
    "Wood": "&fWood Chest",
    "Gold": "&6Gold Chest",
    "Diamond": "&bDiamond Chest",
    "Emerald": "&2Emerald Chest",
    "Obsidian": "&5Obsidian Chest",
    "Bedrock": "&8Bedrock Chest"
};

function ShortNumber(number) {
    if (number < 1000) {
        return number.toString();
    };
    const units = ["k", "m", "b", "t"];
    let unitIndex = Math.floor((number.toString().length - 1) / 3) - 1;
    let abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);
    if (abbreviatedNumber < 1 && unitIndex > 0) {
        unitIndex--;
        abbreviatedNumber = (number / Math.pow(1000, unitIndex + 1)).toFixed(1);
    };
    return `${abbreviatedNumber}${units[unitIndex]}`;
};

function normalizeItemName(name) {
    return name
        .removeFormatting()
        .replace(/^(Shiny |✪ |✪✪ |✪✪✪ |✪✪✪✪ |✪✪✪✪✪)/i, "")
        .replace(/\s+/g, " ")
        .trim();
};

const loadUI = (inv) => {
    let nugget = inv.getStackInSlot(31)
    let invName = inv.getName();
    let newName = invName.removeFormatting()
    let items = inv.getItems().slice(9, 27).filter(a => a && a?.getID() !== 160)
    let lore = nugget.getLore()
    let bzFile = FileLib.read("NozomiAddon", "data/bz.json")
    let bzData = JSON.parse(bzFile)
    let ahFile = FileLib.read("NozomiAddon", "data/auction.json")
    let ahData = JSON.parse(ahFile)
    if (nugget && lore.length >= 7) {
        ischest = true
        let cleanprice = 0
        lore.forEach(line => {
            let text = line.removeFormatting();
            let match = text.match(/^([\d,]+) Coins$/);
            if (match) {
                cleanprice += parseInt(match[1].replace(/,/g, ""));
            };
            if (text == "Dungeon Chest Key") {
                cleanprice += Number((bzData["DUNGEON_CHEST_KEY"].buy).toFixed(2));
            };
        });
        cleanprice = Number(cleanprice);
        let totalProfit = 0
        let newprofit = "0"
        let profittext = ""
        items.map(a => {
            let itemID = getSkyblockItemID(a);
            let itemName = a.getName();
            let itemLore = a.getLore()
            let cleanName = itemName.removeFormatting()
            let multiplier = 1;
            if (cleanName.includes("Enchanted Book")) itemName = itemLore[1];
            if (!itemID) {
                if (cleanName.includes("Wither Essence")) {
                    itemID = "ESSENCE_WITHER";
                } else if (cleanName.includes("Undead Essence")) {
                    itemID = "ESSENCE_UNDEAD";
                }
            }
            const match = cleanName.match(/x(\d+)/);
            if (match) {
                multiplier = Number(match[1]);
            }
            let isRNG = checkRng(itemID, itemName, dungeonChestName[newName])
            if (isRNG) {
                rnglist += `&d&k&lA&r ${itemName} &d&k&lA&r `
                rngItem = itemName
            };
            let itemPrice = 0
            if (bzData && bzData[itemID]) {
                itemPrice = Number(bzData[itemID].sell * multiplier).toFixed(2)
            } else {
                if (ahData && ahData[itemID]) {
                    itemPrice = Number(ahData[itemID] * multiplier).toFixed(2)
                } else {
                    itemPrice = 0
                }
            }
            if (blacklistItems.includes(itemID)) itemPrice = 0;
            totalProfit += Number(itemPrice)
            textitems += `${itemName} &6${ShortNumber(itemPrice)}\n`
        });
        totalProfit = Number((Number(totalProfit) - Number(cleanprice)).toFixed(2))
        if (totalProfit == 0) {
            newprofit = ShortNumber(totalProfit)
            profittext = `&7${newprofit} Coins`
        } else {
            if (totalProfit >= 1) {
                newprofit = ShortNumber(totalProfit)
                profittext = `&a+${newprofit} Coins`
            } else {
                if (totalProfit < 0) {
                    totalProfit = totalProfit * -1
                    newprofit = ShortNumber(totalProfit)
                    profittext = `&c-${newprofit} Coins`
                }
            }
        }
        textprofit = profittext
        textitems += `\n&fProfit: ${profittext}\n`
    };
};

const loadUIMain = (inv) => {
    try {
        let nugget = inv.getStackInSlot(31)
        let items = inv.getItems().slice(9, 18)
        let nuggetname = nugget.getName().removeFormatting()
        let bzFile = FileLib.read("NozomiAddon", "data/bz.json")
        let bzData = JSON.parse(bzFile)
        let ahFile = FileLib.read("NozomiAddon", "data/auction.json")
        let ahData = JSON.parse(ahFile)
        if (nugget && nuggetname.includes("Close")) {
            ischestMain = true
            itemsWithPrice = [];
            items.map((a, index) => {
                let itemName = a.getName();
                let itemLore = a.getLore()
                let originalSlot = index + 9;
                let cleanprice = 0
                let useKey = false
                let stopEnd = false
                let totalProfit = 0
                let newprofit = "0"
                let profittext = ""
                let isProfit = false
                let betterName = itemLore[0].replace(" (#0397/3)", "");
                let lastLore = itemLore[itemLore.length - 1].removeFormatting()
                if (lastLore.includes("Already opened!")) {
                    textitemsMain += `${itemLore[0]}&f: &aAlready opened!\n`;
                    return;
                };
                if (lastLore.includes("Can't open another chest!")) {
                    openedChest += 1
                    textitemsMain += `${itemLore[0]}&f: &cCan't open another chest!\n`;
                } else {
                    for (let i = itemLore.length - 1; i >= 0; i--) {
                        let line = itemLore[i].removeFormatting();
                        if (line.includes("Coins")) {
                            let match = line.match(/^([\d,]+) Coins$/);
                            if (match) cleanprice += parseInt(match[1].replace(/,/g, ""));
                        } else {
                            if (line.includes("Dungeon Chest Key")) {
                                if (!useKey) {
                                    useKey = true;
                                    cleanprice += Number((bzData["DUNGEON_CHEST_KEY"].buy).toFixed(2));
                                };
                            };
                        };
                    };
                    for (let j = 0; j < itemLore.length; j++) {
                        if (stopEnd) return;
                        let line = itemLore[j].removeFormatting();
                        if (chestName.includes(line)) { } else {
                            if (line.includes("Contents")) continue;
                            if (line.includes("Shiny")) line = normalizeItemName(line);
                            let itemID = getItemIDByName(line)
                            if (shards[line]) {
                                itemID = shards[line]
                            };
                            let isRNG = checkRng(itemID, itemLore[j], itemLore[0])
                            if (isRNG) {
                                rnglist += `&d&k&lA&r ${itemLore[j]} &d&k&lA&r `
                                rngItem = itemLore[j];
                            };
                            let itemPrice = 0
                            let multiplier = 1;
                            if (itemID == null) {
                                if (line.includes("Wither Essence")) {
                                    itemID = "ESSENCE_WITHER";
                                    const match = line.match(/x(\d+)/);
                                    if (match) {
                                        multiplier = Number(match[1]);
                                    };
                                } else if (line.includes("Undead Essence")) {
                                    itemID = "ESSENCE_UNDEAD";
                                    const match = line.match(/x(\d+)/);
                                    if (match) {
                                        multiplier = Number(match[1]);
                                    };
                                }
                            };
                            if (bzData && bzData[itemID]) {
                                itemPrice = Number(bzData[itemID].sell * multiplier).toFixed(2)
                            } else {
                                if (ahData && ahData[itemID]) {
                                    itemPrice = Number(ahData[itemID] * multiplier).toFixed(2)
                                } else {
                                    itemPrice = 0
                                }
                            };
                            if (blacklistItems.includes(itemID)) itemPrice = 0;
                            totalProfit += Number(itemPrice)
                            if (line.includes("Cost")) {
                                stopEnd = true;
                                totalProfit = Number((Number(totalProfit) - Number(cleanprice)).toFixed(2))
                                if (totalProfit == 0) {
                                    newprofit = ShortNumber(totalProfit)
                                    profittext = `&7${newprofit}`
                                    isProfit = false
                                } else {
                                    if (totalProfit >= 1) {
                                        newprofit = ShortNumber(totalProfit)
                                        profittext = `&a+${newprofit}`
                                        isProfit = true
                                    } else {
                                        if (totalProfit < 0) {
                                            totalProfit = totalProfit * -1
                                            newprofit = ShortNumber(totalProfit)
                                            profittext = `&c-${newprofit}`
                                            totalProfit = totalProfit * -1
                                            isProfit = false
                                        }
                                    }
                                }
                                itemsWithPrice.push({
                                    name: `${betterName}`,
                                    profit: isProfit,
                                    slot: originalSlot,
                                    price: totalProfit,
                                    priceText: profittext,
                                    rng: rngItem
                                });
                                return;
                            };
                        };
                    };
                };
            });
            sortWithPrice = [...itemsWithPrice];
            sortWithPrice.sort((a, b) => b.price - a.price);
            itemsWithPrice.forEach(item => {
                if (sortWithPrice[0].name === item.name && sortWithPrice[0].profit) {
                    textitemsMain += `${item.name}&f: ${item.priceText} &d&lMOST PROFIT`;
                    if (item.rng) {
                        textitemsMain += `\n &f⤷ ${item.rng}\n`
                    } else {
                        textitemsMain += `\n`
                    }
                } else {
                    textitemsMain += `${item.name}&f: ${item.priceText}`;
                    if (item.rng) {
                        textitemsMain += `\n &f⤷ ${item.rng}\n`
                    } else {
                        textitemsMain += `\n`
                    }
                }
            });
        };
    } finally { }
};

const loadUICroesus = (inv) => {
    try {
        let items = inv.getItems().slice(10, 45)
        iscroesus = true
        runcroesus = []
        items.map((a, index) => {
            let itemID = a.getID()
            let itemName = a.getName();
            let itemLore = a.getLore();
            let itemOpened = true;
            let itemEmpty = false;
            let itemUsedKismet = false;
            let originalSlot = index + 10;
            if (itemID != 397) return;
            for (let i = itemLore.length - 1; i >= 0; i--) {
                let line = itemLore[i];
                if (line.includes("§cNo chests opened yet!") & itemOpened) itemOpened = false;
                if (line.includes("§aNo more chests to open!") & !itemEmpty) itemEmpty = true;
                if (line.includes("§8§mKismet Feather") & !itemUsedKismet) itemUsedKismet = true;
            };
            runcroesus.push({
                name: `${a.getName}`,
                opened: itemOpened ? false : true,
                kismet: itemUsedKismet ? true : false,
                empty: itemEmpty ? true : false,
                slot: originalSlot,
            });
        });
    } finally { }
};

registerWhen(register("guiRender", () => {
    if (ischest) {
        /// Chest Main List 
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let mainX = chestX + 100;
        let mainY = chestY - 10;
        titletext.setString(rnglist)
        titletext.draw((screenWidth / 2), chestY - 30)
        Renderer.drawStringWithShadow(textitems, mainX, mainY);
    };
    if (ischestMain) {
        /// Chest Main List 
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let mainX = chestX + 100;
        let mainY = chestY + 5;
        titletext.setString(rnglist)
        titletext.draw((screenWidth / 2), chestY - 10)
        Renderer.drawStringWithShadow(textitemsMain, mainX, mainY);
        /// Highlight
        if (openedChest >= 1) return;
        itemsWithPrice.sort((a, b) => b.price - a.price);
        let profit = itemsWithPrice[0].profit
        let slot = itemsWithPrice[0].slot
        if (profit) {
            let gui = Client.currentGui.get();
            highlightSlot(gui, slot, 0, 1, 0, 1, true);
        };
    };
    if (iscroesus) {
        runcroesus.forEach((run) => {
            let gui = Client.currentGui.get();
            if (run.empty) {
                highlightSlot(gui, run.slot, 0.66, 0, 0, 1, true);
            } else {
                if (run.opened) {
                    if (run.kismet) {
                        highlightSlot(gui, run.slot, 0, 1, 0, 1, true);
                    } else {
                        highlightSlot(gui, run.slot, 0, 0.66, 0.66, 1, true);
                    }
                } else {
                    highlightSlot(gui, run.slot, 1, 0.66, 0, 1, true);
                }
            }
        })
    };
}), () => Settings.dungeonchestprofit);

registerWhen(register("postGuiRender", () => {
    if (ischest) {
        /// Chest Main List 
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let textX = chestX;
        let textY = chestY - 10;
        Renderer.drawStringWithShadow(textprofit, textX, textY);
    };
}), () => Settings.dungeonchestprofit);

registerWhen(register("guiClosed", () => {
    ischest = false
    ischestMain = false
    iscroesus = false
    openedChest = 0
    textitems = "&r"
    textitemsMain = "&r"
    itemsWithPrice = [];
    sortWithPrice = [];
    runcroesus = []
    rnglist = "&r"
    rngItem = null
}), () => Settings.dungeonchestprofit);

registerWhen(register("guiOpened", () => {
    ischest = false
    ischestMain = false
    iscroesus = false
    textitems = "&r"
    textitemsMain = "&r"
    itemsWithPrice = [];
    sortWithPrice = [];
    runcroesus = []
    rnglist = "&r"
    rngItem = null
    openedChest = 0
    if (Settings.dungeonchestprofit) {
        if (debounce) return;
        debounce = true;
        Client.scheduleTask(2, () => {
            try {
                let inv = Player.getContainer();
                if (!inv) return;
                let invName = inv.getName();
                let newName = invName.removeFormatting()
                if (invName == "container") return;
                if (newName.includes("Croesus")) {
                    loadUICroesus(inv);
                };
                if (newName.includes("Catacombs - Floor") || newName.includes("Master Catacombs - Floor")) {
                    loadUIMain(inv);
                };
                if (chestNameOb.includes(invName)) {
                    loadUI(inv);
                };
            } finally {
                debounce = false;
            }
        });
    };
}), () => Settings.dungeonchestprofit);

registerWhen(register("worldUnload", () => {
    textitems = "&r"
    textitemsMain = "&r"
    rnglist = "&r"
    openedChest = 0
    itemsWithPrice = [];
    sortWithPrice = [];
    runcroesus = []
    debounce = false
}), () => Settings.dungeonchestprofit);