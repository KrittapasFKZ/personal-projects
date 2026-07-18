import Settings from "../config";
import { registerWhen } from "../utils/utils";

registerWhen(register("chat", (rarity, items, magic_find) => {
    if (Settings.raredropalert) {
        if (Settings.raredropalert_onlyrng) {
            if (rarity.includes("CRAZY RARE DROP") || rarity.includes("INSANE DROP")) {
                Client.showTitle(`${rarity}`, `${items}`, 0, 70, 0);
                World.playSound("random.orb", 100, 1.25);
            };
        } else {
            Client.showTitle(`${rarity}`, `${items}`, 0, 70, 0);
            World.playSound("random.orb", 100, 1.25);
        };
    };
}).setCriteria("&r${rarity}! &r${items} &r&b(${magic_find} &r&b✯ Magic Find&r&b)&r"), () => Settings.raredropalert);