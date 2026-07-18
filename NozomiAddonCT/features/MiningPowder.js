import Settings from "../config";
import { registerWhen } from "../utils/utils";

registerWhen(register("chat", (amount) => {
    if (!Settings.miningpowder) return
    Client.showTitle("", `&a+${amount} &d᠅`, 0, 30, 0)
}).setCriteria("&r    &r&dGemstone Powder &r&8x${amount}&r"), () => Settings.miningpowder);