import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";
import { getWorld } from "../utils/worlds";
import { rename } from "../features/DamageSplash";

const dmgRegex = /^.?\d[\d,.]+.*?$/

registerWhen(register("renderEntity", (entity, pos, tick, event) => {
    if (entity.getEntity() instanceof net.minecraft.entity.item.EntityArmorStand) {
        let name = entity.getName()?.removeFormatting();
        if (name && dmgRegex.test(name)) {
            cancel(event);
            return;
        }
    };
}), () => Settings.dungeonhidedamage && Dungeon.inDungeon && getWorld() !== "Kuudra");

registerWhen(register("renderEntity", (entity, pos, tick, event) => {
    if (entity.getEntity() instanceof net.minecraft.entity.item.EntityArmorStand) {
        let name = entity.getName()?.removeFormatting();
        if (name && dmgRegex.test(name)) {
            cancel(event);
            return;
        }
    };
}), () => Settings.kuudrahidedamage && !Dungeon.inDungeon && getWorld() == "Kuudra");

registerWhen(register("renderEntity", (entity, pos, tick, event) => {
    if (entity.getEntity() instanceof net.minecraft.entity.item.EntityArmorStand) {
        let name = entity.getName()?.removeFormatting();
        if (name && dmgRegex.test(name)) {
            cancel(event);
            return;
        }
    };
}), () => Settings.generalhidedamage && !Dungeon.inDungeon && getWorld() !== "Kuudra");

registerWhen(register("renderEntity", (entity, pos, tick, event) => {
    if (entity.getEntity() instanceof net.minecraft.entity.item.EntityArmorStand) {
        rename(entity)
    };
}), () => !(Settings.dungeonhidedamage && Dungeon.inDungeon && getWorld() !== "Kuudra") || !(Settings.kuudrahidedamage && !Dungeon.inDungeon && getWorld() == "Kuudra") || 1(Settings.generalhidedamage && !Dungeon.inDungeon && getWorld() !== "Kuudra"));