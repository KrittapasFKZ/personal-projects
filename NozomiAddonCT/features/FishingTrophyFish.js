import Settings from "../config";
import { registerWhen } from "../utils/utils";

let rarityLists = {
    "BRONZE": "&8&lBRONZE",
    "SILVER": "&7&lSILVER",
    "GOLD": "&6&lGOLD",
    "DIAMOND": "&b&lDIAMOND"
}

let fishLists = {
    "Sulphur": "&fSulphur Skitter",
    "Blobfish": "&fBlobfish",
    "Steaming-Hot Flounder": "&fSteaming-Hot Flounder",
    "Gusher": "&fGusher",
    "Obfuscated 2": "&aObfuscated 2",
    "Slugfish": "&aSlugfish",
    "Flyfish": "&aFlyfish",
    "Obfuscated 3": "&9Obfuscated 3",
    "Vanille": "&9Vanille",
    "Lavahorse": "&9Lavahorse",
    "Mana Ray": "&9Mana Ray",
    "Volcanic Stonefish": "&9Volcanic Stonefish",
    "Skeleton Fish": "&5Skeleton Fish",
    "Moldfin": "&5Moldfin",
    "Soul Fish": "&5Soul Fish",
    "Karate Fish": "&5Karate Fish",
    "Golden Fish": "&6Golden Fish",
}

registerWhen(register("chat", (fish, rarity) => {
    if(rarity !== "DIAMOND") return;
    World.playSound("random.levelup", 100, 1);
    Client.showTitle(`${rarityLists[rarity]}`, `${fishLists[fish]}`, 0, 40, 5)
}).setCriteria("♔ TROPHY FISH! You caught a ${fish} ${rarity}!"), () => Settings.fishingtrophyfish);