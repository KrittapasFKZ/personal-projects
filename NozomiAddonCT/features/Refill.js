import Settings from "../config";

register("command", () => {
    let Epearl = 0
    let Superboom = 0
    Player.getInventory().getItems().forEach(item => {
        if (item?.getName()?.removeFormatting()?.includes("Ender Pearl")) Epearl += item.getStackSize();
        if (item?.getName()?.removeFormatting()?.includes("Superboom TNT")) Superboom += item.getStackSize();
    })
    let needPearl = 16 - Epearl
    let needTNT = 64 - Superboom
    let text = ""
    if (needPearl >= 1) {
        text += `&5x${needPearl} Ender Pearl`
        setTimeout(() => {
            ChatLib.command(`gfs ender_pearl ${needPearl}`, false);
        }, 2000)
    };
    if (needTNT >= 1) {
        text += ` &9x${needTNT} TNT`
        setTimeout(() => {
            ChatLib.command(`gfs SUPERBOOM_TNT ${needTNT}`, false);
        }, 4000)
    };
    ChatLib.chat(`${Settings.prefix} &aRefilling ${text}&a!`)
}).setName("narf").setAliases(["refill", "na refill"])
