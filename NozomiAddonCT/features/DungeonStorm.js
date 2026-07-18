import Settings from "../config";
import { registerWhen } from "../utils/utils";
import Dungeon from "../../BloomCore/dungeons/Dungeon";

let debounce = false 

registerWhen(register("chat", (dmg) => {
    if (Settings.stormfreak) {
        if (debounce) return
        debounce = true
        ChatLib.command(`pc I HATE YOU STORM`, false);
        setTimeout(() => {
            debounce = false
        }, 750);
    };
}).setCriteria("Storm's Lightning Fireball hit you for ${dmg} true damage."), () => Settings.stormfreak && Dungeon.inDungeon);

registerWhen(register("chat", (dmg) => {
    if (Settings.stormfreak) {
        if (debounce) return
        debounce = true
        ChatLib.command(`pc I HATE YOU STORM`, false);
        setTimeout(() => {
            debounce = false
        }, 750);
    };
}).setCriteria("Storm's Static Field hit you for ${dmg} damage."), () => Settings.stormfreak && Dungeon.inDungeon);