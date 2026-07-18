import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

let Entities = []

registerWhen(register("renderWorld", () => {
    if (!Settings.starredmobhighlight) return
    if (!Dungeon.inDungeon) return;
    if (Dungeon.bossEntry) return;
    let starredRegex = "^(?:.* )?§6✯ .+ .*§c❤$"
    Entities = World.getAllEntities().filter(entity => {
        let name = entity.getName();
        return name.includes("§6✯") || name.removeFormatting().includes("Shadow Assassin");
    });
    Entities.forEach(entity => {
        let name = ChatLib.removeFormatting(entity.getName().removeFormatting())
        if (name.includes("Shadow Assassin") && entity.getClassName().includes("Player")) {
            RenderLibV2.drawEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                0.75, 2.1, 0.75,
                Settings.starredmobhighlight_color.getRed() / 255,
                Settings.starredmobhighlight_color.getGreen() / 255,
                Settings.starredmobhighlight_color.getBlue() / 255,
                1,
                true, 2
            );
            RenderLibV2.drawInnerEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                0.75, 2.1, 0.75,
                Settings.starredmobhighlight_color.getRed() / 255,
                Settings.starredmobhighlight_color.getGreen() / 255,
                Settings.starredmobhighlight_color.getBlue() / 255,
                0.25,
                true
            );
        } else {
            if (name.includes("✯")) {
                if (name.includes("Fel") || name.includes("Withermancer")) {
                    RenderLibV2.drawEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                        1, -3.1, 1,
                        Settings.starredmobhighlight_color.getRed() / 255,
                        Settings.starredmobhighlight_color.getGreen() / 255,
                        Settings.starredmobhighlight_color.getBlue() / 255,
                        1,
                        true, 2
                    );
                    RenderLibV2.drawInnerEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                        1, -3.1, 1,
                        Settings.starredmobhighlight_color.getRed() / 255,
                        Settings.starredmobhighlight_color.getGreen() / 255,
                        Settings.starredmobhighlight_color.getBlue() / 255,
                        0.25,
                        true
                    );
                }
                else {
                    RenderLibV2.drawEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                        0.75, -2.1, 0.75,
                        Settings.starredmobhighlight_color.getRed() / 255,
                        Settings.starredmobhighlight_color.getGreen() / 255,
                        Settings.starredmobhighlight_color.getBlue() / 255,
                        1,
                        true, 2
                    );
                    RenderLibV2.drawInnerEspBoxV2(entity.getRenderX(), entity.getRenderY() + 0.1, entity.getRenderZ(),
                        0.75, -2.1, 0.75,
                        Settings.starredmobhighlight_color.getRed() / 255,
                        Settings.starredmobhighlight_color.getGreen() / 255,
                        Settings.starredmobhighlight_color.getBlue() / 255,
                        0.25,
                        true
                    );
                }
            };
        }
    })
}), () => Settings.starredmobhighlight && Dungeon.inDungeon)

registerWhen(register("worldUnload", () => {
    Entities = []
}), () => Settings.starredmobhighlight);