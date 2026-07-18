import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

let trueLividColor = null;
let trueLividName = null;
let trueLividEntity = null;

const numToLivid = {
    0: {
        "color": "§f",
        "name": "Vendetta Livid",
        "r": 255 / 255,
        "g": 255 / 255,
        "b": 255 / 255
    },
    5: {
        "color": "§a",
        "name": "Smile Livid",
        "r": 85 / 255,
        "g": 255 / 255,
        "b": 85 / 255
    },
    13: {
        "color": "§2",
        "name": "Frog Livid",
        "r": 0 / 255,
        "g": 170 / 255,
        "b": 0 / 255
    },
    14: {
        "color": "§c",
        "name": "Hockey Livid",
        "r": 255 / 255,
        "g": 85 / 255,
        "b": 85 / 255
    },
    2: {
        "color": "§d",
        "name": "Crossed Livid",
        "r": 255 / 255,
        "g": 85 / 255,
        "b": 255 / 255
    },
    10: {
        "color": "§5",
        "name": "Purple Livid",
        "r": 170 / 255,
        "g": 0 / 255,
        "b": 170 / 255
    },
    7: {
        "color": "§7",
        "name": "Doctor Livid",
        "r": 170 / 255,
        "g": 170 / 255,
        "b": 170 / 255
    },
    11: {
        "color": "§9",
        "name": "Scream Livid",
        "r": 85 / 255,
        "g": 85 / 255,
        "b": 255 / 255
    },
    4: {
        "color": "§e",
        "name": "Arcade Livid",
        "r": 255 / 255,
        "g": 255 / 255,
        "b": 85 / 255
    }
};

registerWhen(register("renderWorld", () => {
    if (!Settings.lividhighlight) return
    if (!Dungeon.inDungeon) return;
    if (!Dungeon.bossEntry && !Dungeon.runStarted) return

    let block = World.getBlockAt(5, 108, 40);
    if (block.type.getRegistryName() == "minecraft:wool") {
        trueLividColor = numToLivid[block.getMetadata()].color;
        trueLividName = numToLivid[block.getMetadata()].name;
    };

    World.getAllEntities().forEach(entity => {
        let entityName = entity.getName();
        if (entityName.includes("Livid") && entityName.removeFormatting().includes(trueLividName) && entity.getClassName() === "EntityOtherPlayerMP") {
            trueLividEntity = entity;
            let x = entity.getRenderX();
            let y = entity.getRenderY();
            let z = entity.getRenderZ();
            let px = Player.getRenderX();
            let py = Player.getRenderY();
            let pz = Player.getRenderZ();

            RenderLibV2.drawInnerEspBoxV2(x, y, z,
                0.5, 2, 0.5,
                numToLivid[block.getMetadata()].r, numToLivid[block.getMetadata()].g, numToLivid[block.getMetadata()].b, 0.25,
                true
            );
            RenderLibV2.drawEspBoxV2(x, y, z,
                0.5, 2, 0.5,
                numToLivid[block.getMetadata()].r, numToLivid[block.getMetadata()].g, numToLivid[block.getMetadata()].b, 1,
                true, 2
            );
            RenderLibV2.drawLine(
                x, y, z,
                px, py, pz,
                numToLivid[block.getMetadata()].r, numToLivid[block.getMetadata()].g, numToLivid[block.getMetadata()].b, 1,
                true, 10
            );
        };
    });

}), () => Settings.lividhighlight && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    trueLividColor = null;
    trueLividName = null;
    trueLividEntity = null;
}), () => Settings.lividhighlight)