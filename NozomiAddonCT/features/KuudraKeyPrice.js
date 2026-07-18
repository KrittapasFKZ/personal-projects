import Settings from "../config";
import { registerWhen } from "../utils/utils";

let textitems = "&r"
let ischest = false
let debounce = false

function ShortNumber(num) {
    if (num < 1000) return num.toFixed(0);
    const suffixes = ["", "k", "m", "b", "t"];
    let tier = Math.floor(Math.log10(Math.abs(num)) / 3);
    if (tier >= suffixes.length) tier = suffixes.length - 1;
    let scale = Math.pow(10, tier * 3);
    return (num / scale).toFixed(1) + suffixes[tier];
};

const loadUI = () => {
    let bzFile = FileLib.read("NozomiAddon", "data/bz.json");
    let bzData = JSON.parse(bzFile);
    let targetMat = "ENCHANTED_MYCELIUM"
    let price = {
        "1": { Total: 0, Short: "" },
        "2": { Total: 0, Short: "" },
        "3": { Total: 0, Short: "" },
        "4": { Total: 0, Short: "" },
        "5": { Total: 0, Short: "" },
        "6": { Total: 0, Short: "" },
        "7": { Total: 0, Short: "" },
        "8": { Total: 0, Short: "" },
        "9": { Total: 0, Short: "" },
        "10": { Total: 0, Short: "" }
    };
    for (let first = 1; first < 3; first++) {
        if (first == 1) {
            targetMat = "ENCHANTED_MYCELIUM"
        } else {
            if (first == 2) {
                targetMat = "ENCHANTED_RED_SAND"
            };
        };
        let starcost = 0
        let matcost = 0
        if (bzData && bzData[targetMat]) {
            matcost += Number(bzData[targetMat].buy)
        };
        if (bzData && bzData["CORRUPTED_NETHER_STAR"]) {
            starcost = Number(bzData["CORRUPTED_NETHER_STAR"].buy * 2)
        };

        for (i = 1; i < 6; i++) {
            if (i == 5) {
                let basecost = 2400000
                price[i].Total = Number(basecost + Number(matcost * 80) + Number(starcost))
                price[i].Short = ShortNumber(price[i].Total)
            } else {
                if (i == 4) {
                    let basecost = 1200000
                    price[i].Total = Number(basecost + Number(matcost * 40) + Number(starcost))
                    price[i].Short = ShortNumber(price[i].Total)
                } else {
                    if (i == 3) {
                        let basecost = 600000
                        price[i].Total = Number(basecost + Number(matcost * 16) + Number(starcost))
                        price[i].Short = ShortNumber(price[i].Total)
                    } else {
                        if (i == 2) {
                            let basecost = 320000
                            price[i].Total = Number(basecost + Number(matcost * 4) + Number(starcost))
                            price[i].Short = ShortNumber(price[i].Total)
                        } else {
                            if (i == 1) {
                                let basecost = 160000
                                price[i].Total = Number(basecost + Number(matcost * 2) + Number(starcost))
                                price[i].Short = ShortNumber(price[i].Total)
                            };
                        };
                    };
                };
            };
        };
        if (first == 1) {
            textitems += `&5&lMage Shop &a[&6${(matcost).toFixed(2)}&a]\n`
        } else {
            if (first == 2) {
                textitems += `&c&lBarbarian Shop &a[&6${(matcost).toFixed(2)}&a]\n`
            };
        };
        textitems += ` &9Kuudra Key &6${price[1].Short}\n`;
        textitems += ` &5Hot Kuudra Key &6${price[2].Short}\n`;
        textitems += ` &5Burning Kuudra Key &6${price[3].Short}\n`;
        textitems += ` &5Fiery Kuudra Key &6${price[4].Short}\n`;
        textitems += ` &6Infernal Kuudra Key &6${price[5].Short}\n\n`;
    }
    ischest = true;
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
}), () => Settings.kuudrakeyprice);

registerWhen(register("guiClosed", () => {
    ischest = false
    textitems = "&r"
}), () => Settings.kuudrakeyprice);

registerWhen(register("guiOpened", () => {
    ischest = false
    textitems = "&r"
    if (Settings.kuudrakeyprice) {
        if (debounce) return;
        debounce = true
        Client.scheduleTask(2, () => {
            try {
                let inv = Player.getContainer();
                if (!inv) return;
                let invName = inv.getName();
                let newName = invName.removeFormatting()
                if (invName == "container") return;
                if (newName.includes("Mage Shop")) {
                    loadUI();
                };
            } finally {
                debounce = false;
            }
        });
    };
}), () => Settings.kuudrakeyprice);

registerWhen(register("worldUnload", () => {
    textitems = "&r"
    debounce = false
}), () => Settings.kuudrakeyprice);
