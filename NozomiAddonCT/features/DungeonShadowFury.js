import Settings from "../config";
import RenderLibV2 from "../../RenderLibV2";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

let Ignore = [
    "EntityItem", "EntityArrow", "EntityFireBall", "EntityPlayer"
    , "EntityArmorStand", "EntityXPOrb", "EntityPlayerSP", "EntityOtherPlayerMP"
]

registerWhen(register("renderWorld", () => {
    let item = Player.getHeldItem();
    if (!item) return;

    let name = item.getName().removeFormatting();
    if (!name.includes("Shadow Fury")) return;

    World.getAllEntities().forEach(entity => {
        let Class = entity.getClassName()
        if (Ignore.includes(Class)) return;
        const player = World.getPlayerByName(entity.getName());
        if (player && player.getPing() !== 1) return;

        const dx = Player.getRenderX() - entity.getRenderX();
        const dy = Player.getRenderY() - entity.getRenderY();
        const dz = Player.getRenderZ() - entity.getRenderZ();
        const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance <= 12.7) {
            RenderLibV2.drawInnerEspBoxV2(
                entity.getRenderX(), entity.getRenderY() + 1, entity.getRenderZ(),
                0.5, 0.5, 0.5,
                0, 0.5, 1, 0.75,
                true, 0
            );
            RenderLibV2.drawLine(
                Player.getRenderX(), Player.getRenderY() + 1.5, Player.getRenderZ(),
                entity.getRenderX(), entity.getRenderY() + 1, entity.getRenderZ(),
                0, 0.5, 1, 1,
                true, 10
            );
        }
    });
}), () => Settings.shadowfuryhighlight && Dungeon.inDungeon)