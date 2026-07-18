import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import { registerWhen } from "../utils/utils";

let perServer = true
let inMineshaft = false

function isInMineshaft() {
    try {
        let scoreboard = Scoreboard.getLines()
            .map(line => line.getName().replace(/§./g, '').replace(/[^\x00-\x7F]/g, '').toLowerCase());
        if (scoreboard.some(line => line.includes("glacite mineshafts"))) {
            perServer = false
            return true;
        } else {
            return false;
        }
    } catch (e) {
        return false;
    }
}

registerWhen(register("renderWorld", () => {
    if (!Settings.frozencorpsehelper) return
    if (perServer) {
        inMineshaft = isInMineshaft()
    };
    if (!inMineshaft) return;
    World.getAllEntitiesOfType(Java.type("net.minecraft.entity.item.EntityArmorStand")).forEach((armorStand) => {
        let isInvis = armorStand.isInvisible()
        let x = armorStand.getRenderX();
        let y = armorStand.getRenderY();
        let z = armorStand.getRenderZ();
        if (isInvis) return

        RenderLibV2.drawInnerEspBoxV2(x, y, z,
            1, 1.75, 1,
            0, 0.5, 1, 0.5,
            true, 5);
        Tessellator.drawString(`Frozen Corpse`, x, y, z, 0xFFFFFF, false, 0.05, false);
    });
}), () => Settings.frozencorpsehelper);

registerWhen(register("worldUnload", () => {
    inMineshaft = false
}), () => Settings.frozencorpsehelper);

registerWhen(register("worldLoad", () => {
    perServer = true
}), () => Settings.frozencorpsehelper);