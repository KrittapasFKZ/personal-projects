import Settings from "../config";
import { getSkyblockItemID, getItemIDByName } from "../utils/getItems";
import { checkRng } from "./KuudraRNGJumpscare"
import { registerWhen } from "../utils/utils";

let titletext = new Text("&r").setAlign("CENTER").setScale(1).setShadow(true);

let textitems = "&r"
let textprofit = "&r"
let rnglist = "&r"
let ischest = false
let debounce = false

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

const loadUIKuudra = (inv) => {
    let nugget = inv.getStackInSlot(31)
    let items = inv.getItems().slice(9, 18).filter(a => a && a?.getID() !== 160)
    let lore = nugget.getLore()
    let bzFile = FileLib.read("NozomiAddon", "data/bz.json")
    let bzData = JSON.parse(bzFile)
    let ahFile = FileLib.read("NozomiAddon", "data/auction.json")
    let ahData = JSON.parse(ahFile)
    if (nugget) {
        ischest = true
        let cleanprice = 0
        let priceLine = lore[5]
        let newprice = priceLine.removeFormatting()
        let starcost = 0
        let foundkey = false
        if (bzData && bzData["CORRUPTED_NETHER_STAR"]) {
            starcost = Number(bzData["CORRUPTED_NETHER_STAR"].buy * 2)
        };
        /// Key Price Calculate ///

        if (newprice.includes("Infernal Kuudra Key")) {
            if (!foundkey) {
                foundkey = true
                let basecost = 3000000
                let matcost = 0
                /// Mat ///
                if (bzData && bzData["ENCHANTED_RED_SAND"]) {
                    matcost += Number(bzData["ENCHANTED_RED_SAND"].buy * 120)
                };
                if (bzData && bzData["ENCHANTED_MYCELIUM"]) {
                    matcost += Number(bzData["ENCHANTED_MYCELIUM"].buy * 120)
                };
                matcost = Number(matcost) / 2
                cleanprice = Number(basecost + matcost + Number(starcost))
            };
        } else {
            if (newprice.includes("Fiery Kuudra Key")) {
                if (!foundkey) {
                    foundkey = true
                    let basecost = 1500000
                    let matcost = 0
                    /// Mat ///
                    if (bzData && bzData["ENCHANTED_RED_SAND"]) {
                        matcost += Number(bzData["ENCHANTED_RED_SAND"].buy * 60)
                    };
                    if (bzData && bzData["ENCHANTED_MYCELIUM"]) {
                        matcost += Number(bzData["ENCHANTED_MYCELIUM"].buy * 60)
                    };
                    matcost = Number(matcost) / 2
                    cleanprice = Number(basecost + matcost + Number(starcost))
                };
            } else {
                if (newprice.includes("Burning Kuudra Key")) {
                    if (!foundkey) {
                        foundkey = true
                        let basecost = 750000
                        let matcost = 0
                        /// Mat ///
                        if (bzData && bzData["ENCHANTED_RED_SAND"]) {
                            matcost += Number(bzData["ENCHANTED_RED_SAND"].buy * 20)
                        };
                        if (bzData && bzData["ENCHANTED_MYCELIUM"]) {
                            matcost += Number(bzData["ENCHANTED_MYCELIUM"].buy * 20)
                        };
                        matcost = Number(matcost) / 2
                        cleanprice = Number(basecost + matcost + Number(starcost))
                    };
                } else {
                    if (newprice.includes("Hot Kuudra Key")) {
                        if (!foundkey) {
                            foundkey = true
                            let basecost = 400000
                            let matcost = 0
                            /// Mat ///
                            if (bzData && bzData["ENCHANTED_RED_SAND"]) {
                                matcost += Number(bzData["ENCHANTED_RED_SAND"].buy * 6)
                            };
                            if (bzData && bzData["ENCHANTED_MYCELIUM"]) {
                                matcost += Number(bzData["ENCHANTED_MYCELIUM"].buy * 6)
                            };
                            matcost = Number(matcost) / 2
                            cleanprice = Number(basecost + matcost + Number(starcost))
                        };
                    } else {
                        if (newprice.includes("Kuudra Key")) {
                            if (!foundkey) {
                                foundkey = true
                                let basecost = 200000
                                let matcost = 0
                                /// Mat ///
                                if (bzData && bzData["ENCHANTED_RED_SAND"]) {
                                    matcost += Number(bzData["ENCHANTED_RED_SAND"].buy * 2)
                                };
                                if (bzData && bzData["ENCHANTED_MYCELIUM"]) {
                                    matcost += Number(bzData["ENCHANTED_MYCELIUM"].buy * 2)
                                };
                                matcost = Number(matcost) / 2
                                cleanprice = Number(basecost + matcost + Number(starcost))
                            };
                        } else {
                            cleanprice = 0
                        };
                    };
                };
            };
        };

        ///////////////////////////
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
                if (cleanName.includes("Crimson Essence")) {
                    itemID = "ESSENCE_CRIMSON";
                };
            }
            const match = cleanName.match(/x(\d+)/);
            if (match) {
                multiplier = Number(match[1]);
            }
            let isRNG = checkRng(itemID, itemName, "&cKuudra Chest")
            if (isRNG) {
                rnglist += `&d&k&lA&r ${itemName} &d&k&lA&r `
            };
            // >>>>>>>>>>>>>>>>>>>>>>>>>>>>> ChatLib.chat(`${itemName}`)
            let itemPrice = 0
            if (bzData && bzData[itemID]) {
                itemPrice = Number(bzData[itemID].sell * multiplier).toFixed(2)
            } else {
                if (ahData && ahData[itemID]) {
                    itemPrice = Number(ahData[itemID] * multiplier).toFixed(2)
                } else {
                    itemPrice = 0
                };
            }
            totalProfit += Number(itemPrice)
            textitems += `${itemName} &6${ShortNumber(itemPrice)}\n`
        });
        cleanprice = cleanprice.toFixed(2)
        cleanprice = Number(cleanprice)
        textitems += `\n${priceLine} &6${ShortNumber(cleanprice)}\n`
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
        textitems += `&fProfit: ${profittext}\n`
    };
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
        let textX = chestX;
        let textY = chestY - 10;
        titletext.setString(rnglist)
        titletext.draw((screenWidth / 2), chestY - 30)
        Renderer.drawStringWithShadow(textprofit, textX, textY);
        Renderer.drawStringWithShadow(textitems, mainX, mainY);
    };
}), () => Settings.kuudrachestprofit);

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
        titletext.setString(rnglist)
        titletext.draw((screenWidth / 2), chestY - 30)
        Renderer.drawStringWithShadow(textprofit, textX, textY);
    };
}), () => Settings.kuudrachestprofit);

registerWhen(register("guiClosed", () => {
    ischest = false
    textitems = "&r"
    rnglist = "&r"
}), () => Settings.kuudrachestprofit);

registerWhen(register("guiOpened", () => {
    ischest = false
    textitems = "&r"
    rnglist = "&r"
    if (Settings.kuudrachestprofit) {
        if (debounce) return;
        debounce = true
        Client.scheduleTask(2, () => {
            try {
                let inv = Player.getContainer();
                if (!inv) return;
                let invName = inv.getName();
                let newName = invName.removeFormatting()
                if (invName == "container") return;
                if (newName.includes("Paid Chest")) {
                    loadUIKuudra(inv);
                };
            } finally {
                debounce = false;
            }
        });
    };
}), () => Settings.kuudrachestprofit);

registerWhen(register("worldUnload", () => {
    textitems = "&r"
    rnglist = "&r"
    debounce = false
}), () => Settings.kuudrachestprofit);