import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { registerWhen } from "../utils/utils"; 

let ismelody = false
let debounce = false
let currentTerminal = "&r"
let mainText = new Text(" ", 5, 5).setScale(1).setShadow(true);

registerWhen(register("guiRender", () => {
    if (ismelody) {
        let screenWidth = Renderer.screen.getWidth();
        let screenHeight = Renderer.screen.getHeight();
        let chestWidth = 6;
        let chestHeight = 192;
        let chestX = (screenWidth - chestWidth) / 2;
        let chestY = (screenHeight - chestHeight) / 2;
        let mainX = chestX + 100;
        let mainY = chestY - 10;
        mainText.setScale(1);
        mainText.setString(`&f&l[&c&lBad Melody&f&l]`);
        mainText.draw(mainX, mainY);
    };
}), () => Settings.dungeonmelody && Dungeon.inDungeon);

registerWhen(register("clicked", (x, y, button, isDown) => {
    if (!Settings.dungeonmelody) return;
    if (!Dungeon.inDungeon) return;
    if (!ismelody) return
    let screenWidth = Renderer.screen.getWidth();
    let screenHeight = Renderer.screen.getHeight();
    let chestWidth = 6;
    let chestHeight = 192;
    let chestX = (screenWidth - chestWidth) / 2;
    let chestY = (screenHeight - chestHeight) / 2;
    let mainX = chestX + 100;
    let mainY = chestY - 10;
    let textWidth = mainText.getWidth()
    let textHeight = 10;

    if (isDown && x >= mainX && x <= mainX + textWidth && y >= mainY && y <= mainY + textHeight) {
        if (debounce) return;
        debounce = true
        ChatLib.chat(`${Settings.prefix} &aAnnounced &c&lBad Melody!`);
        ChatLib.command("pc NA » Bad Melody Pattern! HELP!", false);
        World.playSound("mob.villager.no", 50, 1);
        setTimeout(() => {
            debounce = false
        }, 1000);
    };
}), () => Settings.dungeonmelody && Dungeon.inDungeon);

registerWhen(register("guiClosed", () => {
    currentTerminal = "&r"
    ismelody = false
}), () => Settings.dungeonmelody && Dungeon.inDungeon);

registerWhen(register("guiOpened", () => {
    currentTerminal = "&r"
    ismelody = false
    if (Settings.dungeonmelody) {
        Client.scheduleTask(2, () => {
            let inv = Player.getContainer();
            if (!inv) return;

            let invName = inv.getName();
            let newName = invName.removeFormatting()
            if (invName == "container") return;
            if (newName.includes("Click the button on time!")) {
                ismelody = true
                currentTerminal = "&dMelody"
            };

        });
    };
}), () => Settings.dungeonmelody && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    currentTerminal = "&r"
    ismelody = false
}), () => Settings.dungeonmelody);