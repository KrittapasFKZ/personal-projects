import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";
import { onChatPacket } from "../../BloomCore/utils/Events";

registerWhen(onChatPacket(() => {
    if (!Settings.dragphaseragtimer) return
    if (!Dungeon.inDungeon) return;
    Client.showTitle("&e&l&kAA&r &6&n&lRAG&r &e&l&kAA&r", "", 0, 50, 5)
    World.playSound("random.levelup", 100, 0.5);
}).setCriteria("[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you."), () => Settings.dragphaseragtimer && Dungeon.inDungeon) 