import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerWhen } from "../utils/utils";

registerWhen(register('renderEntity', (entity, pos, ticks, event) => {
    cancel(event)
}).setFilteredClass(Java.type("net.minecraft.entity.item.EntityFallingBlock").class), () => Settings.p5hidefallingBlock)