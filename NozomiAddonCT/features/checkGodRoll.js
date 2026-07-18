import Settings from "../config";
import { checkRng } from "./KuudraRNGJumpscare"
import { getSkyblockItemID, getItemIDByName } from "../utils/getItems";
import { checkGodRolls } from "../utils/checkAttributes";

let ahFile = FileLib.read("NozomiAddon", "data/auction.json")
let ahData = JSON.parse(ahFile)

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

register("command", () => {
    let item = Player.getHeldItem();
    let itemName = item.getName();
    let cleanName = itemName.removeFormatting();
    let itemLore = item.getLore();
    let itemID = getSkyblockItemID(item);
    /// GodRolls Checker ///
    let godrolls = false
    let godrollName = "";
    let grItemID = `${itemID}`

    //////////////////////// Aurora ////////////////////////
    if (cleanName.includes("Aurora")) {
        if (checkGodRolls.mage1(itemLore)) {
            godrolls = true;
            godrollName = "MP & MR";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MANA_REGENERATION`
        } else if (checkGodRolls.mage2(itemLore)) {
            godrolls = true;
            godrollName = "MP & VIT";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.mage3(itemLore)) {
            godrolls = true;
            godrollName = "MP & VET";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.mage4(itemLore)) {
            godrolls = true;
            godrollName = "DOM & MP";
            grItemID += `+ATTRIBUTE_DOMINANCE+ATTRIBUTE_MANA_POOL`
        };
    };
    //////////////////////// Aurora ////////////////////////

    //////////////////////// Terror ////////////////////////
    if (cleanName.includes("Terror")) {
        if (checkGodRolls.arch1(itemLore)) {
            godrolls = true;
            godrollName = "LL & MP";
            grItemID += `+ATTRIBUTE_LIFELINE+ATTRIBUTE_MANA_POOL`
        } else if (checkGodRolls.arch2(itemLore)) {
            godrolls = true;
            godrollName = "DOM & VIT";
            grItemID += `+ATTRIBUTE_DOMINANCE+ATTRIBUTE_MENDING`
        };
    };
    //////////////////////// Terror ////////////////////////

    //////////////////////// Crimson ////////////////////////
    if (cleanName.includes("Crimson")) {
        if (checkGodRolls.ber1(itemLore)) {
            godrolls = true;
            godrollName = "MF & VET";
            grItemID += `+ATTRIBUTE_MAGIC_FIND+ATTRIBUTE_VETERAN`
        } else if (checkGodRolls.ber2(itemLore)) {
            godrolls = true;
            godrollName = "MF & VIT";
            grItemID += `+ATTRIBUTE_MAGIC_FIND+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.ber3(itemLore)) {
            godrolls = true;
            godrollName = "VIT & VET";
            grItemID += `+ATTRIBUTE_MENDING+ATTRIBUTE_VETERAN`
        };
    };
    //////////////////////// Crimson ////////////////////////

    //////////////////////// Molten ////////////////////////
    if (cleanName.includes("Molten")) {
        if (checkGodRolls.mage1(itemLore)) {
            godrolls = true;
            godrollName = "MP & MR";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MANA_REGENERATION`
        } else if (checkGodRolls.mage2(itemLore)) {
            godrolls = true;
            godrollName = "MP & VIT";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.mage3(itemLore)) {
            godrolls = true;
            godrollName = "MP & VET";
            grItemID += `+ATTRIBUTE_MANA_POOL+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.mage4(itemLore)) {
            godrolls = true;
            godrollName = "DOM & MP";
            grItemID += `+ATTRIBUTE_DOMINANCE+ATTRIBUTE_MANA_POOL`
        } else if (checkGodRolls.arch1(itemLore)) {
            godrolls = true;
            godrollName = "LL & MP";
            grItemID += `+ATTRIBUTE_LIFELINE+ATTRIBUTE_MANA_POOL`
        } else if (checkGodRolls.arch2(itemLore)) {
            godrolls = true;
            godrollName = "DOM & VIT";
            grItemID += `+ATTRIBUTE_DOMINANCE+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.ber1(itemLore)) {
            godrolls = true;
            godrollName = "MF & VET";
            grItemID += `+ATTRIBUTE_MAGIC_FIND+ATTRIBUTE_VETERAN`
        } else if (checkGodRolls.ber2(itemLore)) {
            godrolls = true;
            godrollName = "MF & VIT";
            grItemID += `+ATTRIBUTE_MAGIC_FIND+ATTRIBUTE_MENDING`
        } else if (checkGodRolls.ber3(itemLore)) {
            godrolls = true;
            godrollName = "VIT & VET";
            grItemID += `+ATTRIBUTE_MENDING+ATTRIBUTE_VETERAN`
        };
    };
    //////////////////////// Molten ////////////////////////

    if (godrolls) {
        checkRng("GOD_ROLL", `${itemName} &b(${godrollName})`, "&cKuudra Chest");
    };
    ////////////////////////


}).setName("checkgodroll");