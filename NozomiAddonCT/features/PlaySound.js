import Settings from "../config";
import { getWorld } from "../utils/worlds";

register("command", (...args) => {
    World.playSound(args[0], 100, Number(args[1]));
    ChatLib.chat(`${Settings.prefix} &aPlayed sound!`)
}).setName("nasound")

register("command", (...args) => {
    World.playSound(args[0], 100, Number(args[1]));
    Client.showTitle(`${args}`, "", 0, 50, 5)
}).setName("natitle")

register("command", () => {
    ChatLib.chat(`${Settings.prefix} &aCurrent World is &b${getWorld()}`)
}).setName("nagetworld")