import Dungeon from "../../BloomCore/dungeons/Dungeon";
import Settings from "../config";
import { registerWhen } from "../utils/utils";

const Timer = Java.type("java.util.Timer");
const TimerTask = Java.type("java.util.TimerTask");

const timer = new Timer();

function hidePlayer() {
    EventHidePlayer.register()

    timer.schedule(new TimerTask({
        run: function () {
            EventHidePlayer.unregister()
            World.playSound("random.orb", 100, 2);
        }
    }), 2000);
}


registerWhen(register("chat", (name, event) => {
    if (!Settings.leapannounce) return;
    if (!Dungeon.inDungeon) return;
    const playerClass = Dungeon.classes[name];
    ChatLib.command(`pc Leaped to ${name}!`);
    hidePlayer()
    cancel(event);
}).setCriteria("You have teleported to ${name}!"), () => Settings.leapannounce && Dungeon.inDungeon);

let EventHidePlayer = register("renderEntity", (entity, pos, partialTicks, event) => {
    if (!Settings.leaphideplayer) return;
    if (entity.getName() === Player.getName()) return;

    const player = World.getPlayerByName(entity.getName());
    if (!player || player.getPing() !== 1) return;

    const dx = Player.getX() - entity.getX();
    const dy = Player.getY() - entity.getY();
    const dz = Player.getZ() - entity.getZ();
    const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

    if (distance <= 3) {
        cancel(event);
    }
}).setFilteredClass(Java.type("net.minecraft.entity.player.EntityPlayer")).unregister();
