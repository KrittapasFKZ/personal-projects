import Settings from "../config";

let procSound = false
let targetSound = ""
let targetPitch = 1
let dropNormal = [
    "RECOMBOBULATOR_3000",
    "BONZO_STAFF",
    "SPIRIT_WING",
    "SPIRIT_BONE",
    "LIVID_DAGGER",
    "SHADOW_ASSASSIN_CHESTPLATE",
    "STARRED_SHADOW_ASSASSIN_CHESTPLATE",
    "NECROMANCER_LORD_CHESTPLATE",
    "SUMMONING_RING",
    "WITHER_CHESTPLATE",
    "AUTO_RECOMBOBULATOR",
    "ITEM_SPIRIT_BOW",
    "LAST_BREATH",
];
let dropRNG = [
    "SHADOW_FURY",
    "GIANTS_SWORD",
    "PRECURSOR_EYE",
    "SHADOW_WARP_SCROLL",
    "IMPLOSION_SCROLL",
    "WITHER_SHIELD_SCROLL",
    "NECRON_HANDLE",
    "DARK_CLAYMORE",
    "DYE_LIVID",
    "DYE_NECRON",
    "ENCHANTMENT_THUNDERLORD_7",
    "MASTER_SKULL_TIER_5"
];
let dropStar = [
    "FIRST_MASTER_STAR",
    "SECOND_MASTER_STAR",
    "THIRD_MASTER_STAR",
    "FOURTH_MASTER_STAR",
    "FIFTH_MASTER_STAR"
];

const procStuff = (type, chat, item) => {
    ChatLib.chat(`${chat}`);
    if (type == "NORMAL") {
        targetSound = "note.pling"
        targetPitch = 1
        procSound = true
        Client.showTitle(`${item}`, `&6&lRARE DROP`, 0, 50, 10);
    } else {
        if (type == "STAR") {
            targetSound = "mob.wither.death"
            targetPitch = 1.5
            procSound = true
            Client.showTitle(`${item}`, `&d&lRNG DROP`, 0, 50, 10);
        } else {
            if (type == "RNG") {
                targetSound = "mob.enderdragon.growl"
                targetPitch = 2
                procSound = true
                Client.showTitle(`${item}`, `&d&lRNG DROP`, 0, 50, 10);
            };
        };
    };
};

register("guiRender", () => {
    if (procSound) {
        procSound = false
        World.playSound(targetSound, 100, targetPitch);
        targetSound = ""
        targetPitch = 1
    };
});

export const checkRng = (itemID, itemName, chestName) => {
    if (dropNormal.includes(itemID)) {
        procStuff("NORMAL", `${Settings.prefix} ${chestName}&f: ${itemName}`, itemName);
        return true
    };
    if (dropStar.includes(itemID)) {
        procStuff("STAR", `${Settings.prefix} ${chestName}&f: ${itemName}`, itemName);
        return true
    };
    if (dropRNG.includes(itemID)) {
        procStuff("RNG", `${Settings.prefix} ${chestName}&f: ${itemName}`, itemName);
        return true
    };
    return false
};