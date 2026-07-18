import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

registerWhen(register("renderWorld", () => {
    if (!Settings.felhighlight) return;
    if (!Dungeon.inDungeon) return;

    const entities = World.getAllEntities();

    entities.forEach(ent => {
        const entity = ent.getEntity();
        const classent = ent.getClassName()

        if (!classent.includes("EntityEnderman")) return;

        const x = ent.getRenderX();
        const y = ent.getRenderY();
        const z = ent.getRenderZ();
        const dx = Player.getRenderX() - x;
        const dy = Player.getRenderY() - y;
        const dz = Player.getRenderZ() - z;
        const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance > 24) return;

        RenderLibV2.drawInnerEspBoxV2(x, y, z,
            0.75, 3, 0.75,
            0.65, 0.13, 0.83, 0.25,
            true
        );
        RenderLibV2.drawEspBoxV2(x, y, z,
            0.75, 3, 0.75,
            0.65, 0.13, 0.83, 1,
            true, 2
        );
    });
}), () => Settings.felhighlight && Dungeon.inDungeon);