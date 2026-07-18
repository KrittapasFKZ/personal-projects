import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import { registerWhen } from "../utils/utils";

let questactive = false;
let questcd = false 
let questtype = "???";
let questzone = "???";
let lastquestat = 0

registerWhen(register("renderWorld", () => {
    if (!Settings.trapperanimalhelper) return;

    let validEntities = ["EntityCow", "EntityPig", "EntitySheep", "EntityRabbit", "EntityChicken", "EntityHorse"]

    World.getAllEntities().forEach(ent => {
        let entity = ent.getEntity();
        let classent = ent.getClassName()
        let className = ent.getName().removeFormatting()

        if (!validEntities.some(type => classent.includes(type))) return;
        let currentHP = entity.func_110143_aJ()
        let maxHP = entity.func_110138_aP()
        let x = ent.getRenderX();
        let y = ent.getRenderY();
        let z = ent.getRenderZ();

        if (className == "Dinnerbone") return;
        if (currentHP == 20 || maxHP == 20) return;
        if (currentHP == 50 || maxHP == 50) return;
        if (currentHP == 130 || maxHP == 130) return;

        RenderLibV2.drawInnerEspBoxV2(x, y, z,
            1.5, 1.5, 1.5,
            0.33, 0.83, 0.83, 0.5,
            true, 0
        );
        RenderLibV2.drawLine(
            x, y, z,
            Player.getRenderX(), Player.getRenderY() + 1, Player.getRenderZ(),
            0.33, 0.83, 0.83, 1,
            true, 10
        );
        Tessellator.drawString(`${className}`, x, y, z, 0xFFFFFF, false, 0.2, false)
    });
}), () => Settings.trapperanimalhelper)

registerWhen(register("tick", (type, zone, event) => {
    if (questcd) {
        Client.showTitle("", `&a${((lastquestat - Date.now()) / 1000).toFixed(0)}s`, 0, 5, 0)
        if (Date.now() > lastquestat) {
            questcd = false;
            Client.showTitle("", `&aCooldown Ended!`, 0, 30, 0)
            ChatLib.chat(`${Settings.prefix} &e&lTrapper Animals &aCooldown Ended!`);
            World.playSound("random.orb", 100, 1.25);
        };
    };
}), () => Settings.trapperanimalhelper);

registerWhen(register("chat", (type, zone, event) => {
    lastquestat = Date.now() + 15000;
    questactive = true;
    questcd = true;
    questtype = type;
    questzone = zone;
    ChatLib.chat(`${Settings.prefix} &e&lTrapper Animals &ais &f&l${questtype} &aat &f&l${zone}`);
}).setCriteria("[NPC] Trevor: You can find your ${type} animal near the ${zone}."), () => Settings.trapperanimalhelper);

registerWhen(register("chat", (event) => {
    questactive = false;
    questtype = "???";
    questzone = "???";
    ChatLib.chat(`${Settings.prefix} &e&lTrapper Animals &acompleted!`);
}).setCriteria("Return to the Trapper soon to get a new animal to hunt!"), () => Settings.trapperanimalhelper);

registerWhen(register("worldUnload", () => {
    questactive = false;
    questtype = "???";
    questzone = "???";
    lastquestat = 0
}), () => Settings.trapperanimalhelper);