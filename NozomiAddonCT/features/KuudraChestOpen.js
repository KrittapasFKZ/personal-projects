import Settings from "../config";
import { registerWhen } from "../utils/utils";
import { getWorld } from "../utils/worlds";

let ChestOpened = false

registerWhen(register("packetSent", (packet) => {
    if (!Settings.kuudrachestopen) return
    if (!packet) return
    let ClickedType = packet.func_149542_h()

    if (ClickedType == 0 || ClickedType == 1 || ClickedType == 2) {
        let ItemName = packet.func_149546_g()?.func_82833_r()?.removeFormatting()
        let Slot = packet.func_149544_d()
        let ContainerName = Player.getContainer().getName()
        if (ItemName == "Open Reward Chest" && Slot == 31) {
            ChestOpened = ContainerName
        }
    } else if (ClickedType == 3) {
        let Slot = packet.func_149544_d()
        let Container = Player?.getContainer()
        let ContainerName = Container?.getName()
        let ItemName = Container.getStackInSlot(Slot).getName().removeFormatting()

        if (ItemName == "Open Reward Chest" && Slot == 31) {
            ChestOpened = ContainerName
        }
    } else { return }
}).setFilteredClass(net.minecraft.network.play.client.C0EPacketClickWindow), () => Settings.kuudrachestopen && getWorld() == "Kuudra")

registerWhen(register("soundPlay", (pos, name, vol, pitch, category, event) => {
    if (name == "fireworks.blast") {
        if (vol == "20") {
            ChestOpened = false
            ChatLib.chat(`${Settings.prefix} &aChest Looted!`);
            ChatLib.command(`pc NA » Chest Looted!`, false);
            World.playSound("random.orb", 100, 1.25);
        };
    }
}), () => Settings.kuudrachestopen && getWorld() == "Kuudra" && ChestOpened)

registerWhen(register("worldLoad", () => {
    ChestOpened = false
}), () => Settings.kuudrachestopen);