import Settings from "../config";
import { registerWhen } from "../utils/utils";

let lastState = false;

registerWhen(register("step", () => { 
    if (Settings.slayer_boss_spawn_alert) {
        let scoreboard = Scoreboard.getLines()
            .map(line => line.getName().replace(/§./g, '').toLowerCase());
        let isBossSpawned = scoreboard.some(line => line.includes("slay the boss!"));

        if (isBossSpawned && !lastState) {
            Client.showTitle("&c&lBoss Spawned!", "", 0, 25, 3);
            ChatLib.chat(`${Settings.prefix} &c&lBoss Spawned!`)
            World.playSound("mob.enderdragon.growl", 100, 1);
            lastState = true;
        }

        if (!isBossSpawned) {
            lastState = false;
        }
    };
}).setFps(1), () => Settings.slayer_boss_spawn_alert);