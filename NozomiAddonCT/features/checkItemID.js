import Settings from "../config";
import { getSkyblockItemID, getItemIDByName } from "../utils/getItems";

register("command", () => {
    let item = Player.getHeldItem();
    let itemName = item.getName();
    let cleanName = itemName.removeFormatting();
    let itemLore = item.getLore();
    let itemID = getSkyblockItemID(item);

    ChatLib.chat(`${Settings.prefix} &aID: &7${itemID}`)

}).setName("checkitemid");