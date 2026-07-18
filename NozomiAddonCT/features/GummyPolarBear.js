import Settings from "../config";
import { registerWhen } from "../utils/utils";

let durataiontime = 0
let procSound = false

const showTitle = (title, chat) => {
    if (Settings.gummy_bear) {
        Client.showTitle("", `${title}`, 0, 70, 0)
        ChatLib.chat(`${chat}`)
        World.playSound("mob.villager.no", 100, 1);
    };
};

registerWhen(register("step", () => {
    durataiontime--
    if (durataiontime <= 0 && procSound) {
        showTitle(`&9&lRe-heated Gummy &chas ran out!`, `${Settings.prefix} &9&lRe-heated Gummy &chas ran out!`)
        procSound = false
    }
}).setFps(10), () => Settings.gummy_bear);

registerWhen(register("chat", () => {
    durataiontime = parseInt(36000)
    ChatLib.chat(`${Settings.prefix} &9&lRe-heated Gummy &ahas been used!`);
    procSound = true
}).setCriteria("&r&aYou ate a &r&aRe-heated Gummy Polar Bear&r&a!&r"), () => Settings.gummy_bear);