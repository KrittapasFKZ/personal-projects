import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

let armorStandPositions = [];
let seenUUIDs = new Set();

registerWhen(register("renderWorld", () => {
    if (!Settings.detectarmorstanddrop) return
    if (!Dungeon.inDungeon) return;

    armorStandPositions = armorStandPositions.filter(pos => {
        let isStillHere = World.getAllEntities().some(ent =>
            ent.getUUID() === pos.uuid
        );

        if (!isStillHere) {
            seenUUIDs.delete(`${pos.name}-${Math.round(pos.x * 10)}-${Math.round(pos.y * 10)}-${Math.round(pos.z * 10)}`);
        }
        Client.showTitle("&e&l&kA&r &d&lRARE DROP &e&l&kA&r", `${pos.name}`, 0, 5, 5);
        return isStillHere;
    });

    World.getAllEntitiesOfType(Java.type("net.minecraft.entity.item.EntityArmorStand")).forEach((armorStand) => {
        let name = armorStand.getName()
        let cleanname = name.removeFormatting()
        let uuid = armorStand.getUUID();
        if (cleanname.includes("Ice Spray Wand") || cleanname.includes("Skeleton Master Chestplate")) {
            let x = armorStand.getRenderX();
            let y = armorStand.getRenderY();
            let z = armorStand.getRenderZ();

            let identifier = `${name}-${Math.round(x * 10)}-${Math.round(y * 10)}-${Math.round(z * 10)}`;

            if (!seenUUIDs.has(identifier)) {
                armorStandPositions.push({ name, cleanname, uuid, x, y, z });
                seenUUIDs.add(identifier);
                World.playSound("random.orb", 0.1, 1.25);

                ChatLib.chat(`&d&m-----------------------------------------------------&r`);
                ChatLib.chat(`${Settings.prefix} &d&lRARE DROP! ${name}`);
                ChatLib.chat(`&d&m-----------------------------------------------------&r`);
            };
        }
    });
    let px = Player.getRenderX();
    let py = Player.getRenderY();
    let pz = Player.getRenderZ();
    armorStandPositions.forEach(({ name, cleanname, uuid, x, y, z }) => {
        if (cleanname.includes("Ice Spray Wand")) {
            RenderLibV2.drawInnerEspBoxV2(x, y + 0.5, z,
                1, 1.75, 1,
                0, 0.5, 1, 0.25,
                true
            );
            RenderLibV2.drawEspBoxV2(x, y, z,
                1, 1.75, 1,
                0, 0.5, 1, 1,
                true, 2
            );
            RenderLibV2.drawLine(
                x, y + 1.5, z,
                px, py + 1, pz,
                0, 0.5, 1, 1,
                true, 10
            );
        } else {
            if (cleanname.includes("Skeleton Master Chestplate")) {
                RenderLibV2.drawInnerEspBoxV2(x, y + 0.5, z,
                    1, 1.75, 1,
                    1, 0.5, 0, 0.25,
                    true
                );
                RenderLibV2.drawEspBoxV2(x, y, z,
                    1, 1.75, 1,
                    1, 0.5, 0, 1,
                    true, 2
                );
                RenderLibV2.drawLine(
                    x, y + 1.5, z,
                    px, py + 1, pz,
                    1, 0.5, 0, 1,
                    true, 10
                );
            } else {

            }
        }
    });
}), () => Settings.detectarmorstanddrop && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    seenUUIDs.clear();
    armorStandPositions = [];
}), () => Settings.detectarmorstanddrop);