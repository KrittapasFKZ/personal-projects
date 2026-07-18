import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

const classColor = {
    "mage": {
        "name": "&bMage",
        "r": 85 / 255,
        "g": 255 / 255,
        "b": 255 / 255
    },
    "berserk": {
        "name": "&4Berserk",
        "r": 170 / 255,
        "g": 0 / 255,
        "b": 0 / 255
    },
    "tank": {
        "name": "&2Tank",
        "r": 0 / 255,
        "g": 170 / 255,
        "b": 0 / 255
    },
    "healer": {
        "name": "&dHealer",
        "r": 255 / 255,
        "g": 85 / 255,
        "b": 255 / 255
    },
    "archer": {
        "name": "&6Archer",
        "r": 255 / 255,
        "g": 170 / 255,
        "b": 0 / 255
    },
};

let markedEntity = null;

registerWhen(register("command", (...args) => {
    let cleanName = args[0]?.toLowerCase();
    if (!classColor[cleanName]) {
        ChatLib.chat(`${Settings.prefix} &cInvalid class name.`);
        return;
    }
    Settings.classhighlight_class = cleanName;
    ChatLib.chat(`${Settings.prefix} &aClass Highlight changed to ${classColor[cleanName].name}`);
}).setName("naclass"), () => Settings.classhighlight && Dungeon.inDungeon);

registerWhen(register("tick", () => {
    if (!Settings.classhighlight || !Dungeon.inDungeon) return;
    if (!markedEntity == null) return;

    let targetClass = Settings.classhighlight_class?.toLowerCase() || "mage";

    World.getAllEntities().forEach(entity => {
        if (!entity.getClassName().includes("Player")) return;

        let name = entity.getName().removeFormatting();
        if (name === Player.getName()) return;
        if (!Dungeon.classes.hasOwnProperty(name)) return;

        let playerClass = Dungeon.classes[name]?.toLowerCase();
        if (playerClass && playerClass.includes(targetClass)) {
            markedEntity = entity;
        }
    });
}), () => Settings.classhighlight && Dungeon.inDungeon);

registerWhen(register("renderWorld", () => {
    if (!markedEntity) return;

    let targetClass = Settings.classhighlight_class?.toLowerCase() || "mage";
    let color = classColor[targetClass];

    let x = markedEntity.getRenderX();
    let y = markedEntity.getRenderY();
    let z = markedEntity.getRenderZ();
    let px = Player.getRenderX();
    let py = Player.getRenderY();
    let pz = Player.getRenderZ();

    RenderLibV2.drawEspBoxV2(x, y, z,
        0.75, 2, 0.75,
        color.r, color.g, color.b, 1,
        true, 2
    );
    RenderLibV2.drawInnerEspBoxV2(x, y, z,
        0.75, 2, 0.75,
        color.r, color.g, color.b, 0.25,
        true
    );

    if (Settings.classhighlight_line) {
        RenderLibV2.drawLine(x, y, z, px, py, pz, color.r, color.g, color.b, 1, true, 10);
    };
}), () => Settings.classhighlight && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    markedEntity = null;
}), () => Settings.classhighlight)