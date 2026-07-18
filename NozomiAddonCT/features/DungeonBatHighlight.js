import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

let EntityArray = new Set();

registerWhen(register("renderWorld", () => {
    if (!Settings.bathighlight) return;
    if (!Dungeon.inDungeon) return;
    if (Dungeon.bossEntry) return;

    const entities = World.getAllEntities();

    entities.forEach(ent => {
        const entity = ent.getEntity();
        const classent = ent.getClassName()
        const entName = ent.getName().removeFormatting()
        const isInvisible = ent.isInvisible()
        const uuid = ent.getUUID()

        if (!classent.includes("EntityBat")) return;
        if (isInvisible) return
        let currentHP = entity.func_110143_aJ()

        let identifier = `${entName}-${uuid}`;

        if (!EntityArray.has(identifier) && currentHP <= 0) {
            Client.showTitle(" ", `&9&lBat &c&lDied!`, 0, 30, 0)
            ChatLib.chat(`${Settings.prefix} &9Bat &chas been killed!`)
            World.playSound("random.orb", 100, 1.25);
            EntityArray.add(identifier)
        };

        const x = ent.getRenderX();
        const y = ent.getRenderY();
        const z = ent.getRenderZ();
        const dx = Player.getRenderX() - x;
        const dy = Player.getRenderY() - y;
        const dz = Player.getRenderZ() - z;
        const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance > 24) return;

        RenderLibV2.drawInnerEspBoxV2(x, y, z,
            1, 1, 1,
            Settings.bathighlight_color.getRed() / 255,
            Settings.bathighlight_color.getGreen() / 255,
            Settings.bathighlight_color.getBlue() / 255,
            0.25,
            true
        );
        RenderLibV2.drawEspBoxV2(x, y, z,
            1, 1, 1,
            Settings.bathighlight_color.getRed() / 255,
            Settings.bathighlight_color.getGreen() / 255,
            Settings.bathighlight_color.getBlue() / 255,
            1,
            true, 2
        );
        if (Settings.bathighlight_line) {
            RenderLibV2.drawLine(
                x, y + 0.5, z,
                Player.getRenderX(), Player.getRenderY() + 1.5, Player.getRenderZ(),
                Settings.bathighlight_color.getRed() / 255,
                Settings.bathighlight_color.getGreen() / 255,
                Settings.bathighlight_color.getBlue() / 255,
                Settings.bathighlight_color.getAlpha() / 255,
                true, 5
            );
        };
    });
}), () => Settings.bathighlight && Dungeon.inDungeon)

registerWhen(register("worldUnload", () => {
    EntityArray.clear();
}), () => Settings.bathighlight)