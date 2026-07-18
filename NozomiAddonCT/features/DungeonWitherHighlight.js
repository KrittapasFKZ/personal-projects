import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerWhen } from "../utils/utils";

let inP5 = false

registerWhen(register("renderWorld", () => {
    if (!Settings.witherlordhighlight) return

    if (!Dungeon.inDungeon) return;
    if (inP5) return;

    const ent = World.getAllEntitiesOfType(Java.type("net.minecraft.entity.boss.EntityWither")).find(entity => !entity.isInvisible() && entity.entity.func_82212_n() !== 800)
    if (!ent) return

    let x = ent.getRenderX();
    let y = ent.getRenderY();
    let z = ent.getRenderZ();

    RenderLibV2.drawEspBoxV2(x, y, z,
        1, 2.5, 1,
        Settings.witherlordhighlight_color.getRed() / 255,
        Settings.witherlordhighlight_color.getGreen() / 255,
        Settings.witherlordhighlight_color.getBlue() / 255,
        1,
        true, 2
    );
    RenderLibV2.drawInnerEspBoxV2(x, y, z,
        1, 2.5, 1,
        Settings.witherlordhighlight_color.getRed() / 255,
        Settings.witherlordhighlight_color.getGreen() / 255,
        Settings.witherlordhighlight_color.getBlue() / 255,
        0.25,
        true
    );
}), () => Settings.witherlordhighlight && Dungeon.inDungeon)

registerWhen(onChatPacket((message) => {
    if (!Settings.witherlordhighlight) return
    if (message != "All this, for nothing...") return
    inP5 = true
}).setCriteria(/^\[BOSS\] Necron: (.+)$/), () => Settings.witherlordhighlight && Dungeon.inDungeon)

registerWhen(register("worldUnload", () => {
    inP5 = false
}), () => Settings.witherlordhighlight)