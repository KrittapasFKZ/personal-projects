import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { getSkyblockItemID, getItemIDByName } from "../utils/getItems";

let textitems = "&r"
let ischest = false
let ingredients = {};
let targetItem = "&r"
let debounce = false

let slots = [
    10, 11, 12,
    19, 20, 21,
    28, 29, 30
];

function ShortNumber(num) {
    if (num < 1000) return num.toFixed(0);
    const suffixes = ["", "k", "m", "b", "t"];
    let tier = Math.floor(Math.log10(Math.abs(num)) / 3);
    if (tier >= suffixes.length) tier = suffixes.length - 1;
    let scale = Math.pow(10, tier * 3);
    return (num / scale).toFixed(1) + suffixes[tier];
};

const loadUI = (inv) => {
    let bzFile = FileLib.read("NozomiAddon", "data/bz.json");
    let bzData = JSON.parse(bzFile);
    let ahFile = FileLib.read("NozomiAddon", "data/auction.json")
    let ahData = JSON.parse(ahFile)
    let resultitem = inv.getStackInSlot(25);
    let resultitemID = getSkyblockItemID(resultitem);

    let totalCost = 0
    ischest = true
    textitems += `&e&lRecipe: ${targetItem}\n`

    let Targetprice = 0;
    if (bzData[resultitemID]) Targetprice = bzData[resultitemID].buy;
    else if (ahData[resultitemID]) Targetprice = ahData[resultitemID];
    textitems += `&e&lLowest BIN: &6${ShortNumber(Targetprice)}\n`

    textitems += `\n&e&lIngredients:\n`
    slots.forEach(s => {
        let item = inv.getStackInSlot(s);
        if (!item) return;

        let name = item.getName().removeFormatting();
        let itemID = getSkyblockItemID(item);
        let qty = item.getStackSize();

        if (!itemID) return;

        if (!ingredients[itemID]) {
            ingredients[itemID] = {
                name: item.getName(),
                cleanname: name,
                qty: 0
            };
        }
        ingredients[itemID].qty += qty;
    });

    for (let id in ingredients) {
        let ing = ingredients[id];
        let price = 0;

        if (bzData[id]) price = bzData[id].buy;
        else if (ahData[id]) price = ahData[id];

        let cost = price * ing.qty;
        totalCost += cost;

        textitems += ` &f⤷ ${ing.name} &fx${ing.qty} &6${ShortNumber(cost)}\n`;
    };

    textitems += `\n&e&lTotal Cost: &6${ShortNumber(totalCost)} Coins\n`;
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
        Renderer.drawStringWithShadow(textitems, mainX, mainY);
    };
}), () => Settings.recipetotalcost);

registerWhen(register("guiClosed", () => {
    ischest = false
    textitems = "&r"
    ingredients = {};
}), () => Settings.recipetotalcost);

registerWhen(register("guiOpened", () => {
    ischest = false
    ingredients = {};
    textitems = "&r"
    targetItem = "&r"
    if (Settings.recipetotalcost) {
        if (debounce) return;
        debounce = true
        Client.scheduleTask(2, () => {
            try {
                let inv = Player.getContainer();
                if (!inv) return;
                let invName = inv.getName();
                let newName = invName.removeFormatting()
                if (invName == "container") return;

                let craft_nugget = inv.getStackInSlot(23)
                let craft_lore = craft_nugget.getLore()
                let craft_classline = craft_lore[0].removeFormatting()

                let close_nugget = inv.getStackInSlot(49)
                let close_lore = close_nugget.getLore()
                let close_classline = close_lore[0].removeFormatting()

                if (craft_classline == "Crafting Table" && close_classline == "Close") {
                    targetItem = inv.getStackInSlot(25).getLore()[0]
                    loadUI(inv);
                };
            } finally {
                debounce = false;
            }
        });
    };
}), () => Settings.recipetotalcost);

registerWhen(register("worldUnload", () => {
    textitems = "&r"
    debounce = false
    ingredients = {};
}), () => Settings.recipetotalcost);
