import Settings from "../config";

let procSound = false
let targetSound = ""
let targetPitch = 1
let dropNormal = [
    "BURNING_KUUDRA_CORE",
    "WHEEL_OF_FATE"
];
let dropRNG = [
    "ENCHANTMENT_ULTIMATE_INFERNO_1",
    "ENCHANTMENT_ULTIMATE_FATAL_TEMPO_1",
    "ANANKE_FEATHER",
    "HELLSTORM_STAFF",
    "TORMENTOR",
    "TENTACLE_DYE",
];

const procStuff = (type, chat) => {
    ChatLib.chat(`${chat}`);
    if (type == "NORMAL") {
        targetSound = "note.pling"
        targetPitch = 1
        procSound = true
    } else {
        if (type == "RNG") {
            targetSound = "mob.enderdragon.growl"
            targetPitch = 2
            procSound = true
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
        procStuff("NORMAL", `${Settings.prefix} ${chestName}&f: ${itemName}`);
        return true
    };
    if (dropRNG.includes(itemID)) {
        procStuff("RNG", `${Settings.prefix} ${chestName}&f: ${itemName}`);
        return true
    };
    return false
};