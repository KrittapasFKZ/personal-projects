export const MCItemStack = Java.type("net.minecraft.item.ItemStack")

/**
* @param {Item | MCItemStack} item  
*/
export const getSkyblockItemID = (item) => {
    if (item instanceof MCItemStack) item = new Item(item)
    if (!(item instanceof Item)) return null

    const extraAttributes = item.getNBT()?.getCompoundTag("tag")?.getCompoundTag("ExtraAttributes")
    const itemID = extraAttributes?.getString("id") ?? null
    const itemName = item.getName()
    const itemRawName = itemName.removeFormatting()

    if (itemID == "ENCHANTED_BOOK") {
        const enchantments = extraAttributes.getCompoundTag("enchantments")
        const enchants = [...enchantments.getKeySet()]
        if (!enchants.length) return null

        const enchantment = enchants[0]
        const level = enchantments.getInteger(enchants[0])

        return `ENCHANTMENT_${enchantment.toUpperCase()}_${level}`
    } else {
        if (itemRawName.includes("Shard")) {
            const upperCase = itemRawName.toUpperCase();
            const cleanName = upperCase.replace("SHARD", "").trim();
            const shardName = cleanName.replace(/X\d+$/, "").trim();
            const idName = shardName.replace(/\s+/g, "_").trim();
            return `SHARD_${idName}`;
        } else { 
            return itemID
        }
    }

};

export const getItemIDByName = (name) => {
    const mappingFile = FileLib.read("NozomiAddon", "data/items.json");
    const nameToIdMap = JSON.parse(mappingFile);
    if (name in nameToIdMap) return nameToIdMap[name];

    if (name.startsWith("Enchanted Book (")) {
        let match = name.match(/^Enchanted Book \((.+?) (\d+|I{1,5}|X)\)$/);
        if (!match) return null;
        let enchantName = match[1].trim();
        let roman = match[2];
        const romanToNumber = {
            I: 1,
            II: 2,
            III: 3,
            IV: 4,
            V: 5,
            VI: 6,
            VII: 7,
            VIII: 8,
            IX: 9,
            X: 10
        };
        let level = romanToNumber[roman] || parseInt(roman);
        if (!level) return null;
        const ultimateEnchants = [
            "Chimera",
            "Combo",
            "Fatal Tempo",
            "Inferno",
            "Soul Eater",
            "Swarm",
            "One For All",
            "Reiterate",
            "Rend",
            "Bank",
            "Habanero Tactics",
            "Bobbin Time",
            "Last Stand",
            "Legion",
            "No Pain No Gain",
            "Refrigerate",
            "Wisdom",
            "Flowstate",
            "Flash",
            "The One"
        ];

        let isUltimate = ultimateEnchants.some(e => e.toLowerCase() === enchantName.toLowerCase());

        let baseName = enchantName.toUpperCase().replace(/ /g, "_");
        return `ENCHANTMENT_${isUltimate ? "ULTIMATE_" : ""}${baseName}_${level}`;
    }

    return null;
};
