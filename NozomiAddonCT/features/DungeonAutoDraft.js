import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils";

const architect = (player) => {
    if (!Settings.autodraft) return;
    if (!Dungeon.inDungeon) return;
    if ((player !== Player.getName() && Settings.autodraft)) return

    Client.scheduleTask(10, () => {
        World.playSound("mob.villager.death", 100, 0.75);
        ChatLib.chat(`${Settings.prefix} &aHere your draft! &c&lSKILL ISSUE!`);
        ChatLib.command("gfs architect's first draft 1")
    })
}

// Normal Puzzles
registerWhen(register("chat", architect).setCriteria(/^PUZZLE FAIL! (\w{1,16}) .+$/), () => Settings.autodraft && Dungeon.inDungeon)

// Quiz
registerWhen(register("chat", architect).setCriteria(/^\[STATUE\] Oruo the Omniscient: (\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\.$/), () => Settings.autodraft && Dungeon.inDungeon) 