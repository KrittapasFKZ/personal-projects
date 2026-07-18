import Settings from "../config";
import Dungeon from "../../BloomCore/dungeons/Dungeon";
import { onChatPacket } from "../../BloomCore/utils/Events";
import { registerWhen } from "../utils/utils";

const S32PacketConfirmTransaction = Java.type("net.minecraft.network.play.server.S32PacketConfirmTransaction")

let BloodArray = [
    "Bonzo", "Scarf", "Spirit Bear", "Livid", "Giant", "Putrid", "Reaper", "Vader",
    "Frost", "Cannibal", "Revoker", "Tear", "Mr. Dead", "Skull", "Walker", "Psycho",
    "Ooze", "Freak", "Flamer", "Mute", "Leech", "Parasite"
];

let BloodMod = {
    "Health": 0,
    "Stormy": 0,
    "Golden": 0,
    "Speedy": 0,
    "Boomer": 0,
    "Stealthy": 0
}

let gianttype = null
let bloodOpen = false
let checkblood = false
let sent = 0
let ticksElapsed = 0;

const getBloodTicks = () => { return ticksElapsed; }

const EntityID = Java.type("net.minecraft.entity.item.EntityArmorStand");

registerWhen(register("renderWorld", () => {
    if (!Settings.bloodroom) return;
    if (!Dungeon.inDungeon) return;
    if (!bloodOpen) return

    const entities = World.getAllEntities();

    entities.forEach(ent => {
        const entity = ent.getEntity();
        if (!(entity instanceof EntityID)) return;

        let name = ent.getName()
        let rawName = ent.getName().removeFormatting()
        let split = rawName.split(" ");
        let modifier = split[0];
        let possibleModifier = name.split(" ")[0];
        let possibleName = split[1];

        if (BloodArray.includes(possibleName) && !checkblood) {
            if (!BloodMod.hasOwnProperty(possibleModifier)) return;
            if (sent > 1) return
            sent += 1
            checkblood = true
            ChatLib.chat(`${Settings.prefix} &cBlood Modifier: ${possibleModifier}`);
            ChatLib.command(`pc NA » Blood Modifier: ${modifier}`, false);
            World.playSound("mob.skeleton.say", 100, 1);
        };

    });
}), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(register("chat", (sender, modifier) => {
    if (!Settings.bloodroom) return
    if (checkblood) return
    checkblood = true
    ChatLib.command(`pc NA » Blood Modifier: ${modifier}`, false);
    World.playSound("mob.skeleton.say", 100, 1);
}).setCriteria("&r&9Party &8> ${sender}: &rNA » Blood Modifier: ${modifier}&r"), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(register("packetReceived", () => {
    if (!Settings.bloodroom) return
    if (!bloodOpen) return;

    ticksElapsed++;

}).setFilteredClass(S32PacketConfirmTransaction), () => Settings.bloodroom && Dungeon.inDungeon);

function BloodStart() {
    if (!Settings.bloodroom) return
    bloodOpen = true
};

onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: Things feel a little more roomy now, eh?");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: Oh.. hello?");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: I'm starting to get tired of seeing you around here...");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: You've managed to scratch and claw your way here, eh?");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: So you made it this far... interesting.");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: Ah, we meet again...");
onChatPacket(() => { BloodStart() }).setCriteria("[BOSS] The Watcher: Ah, you've finally arrived.");

registerWhen(register("renderWorld", () => {
    if (!Settings.bloodroom) return
    if (!Dungeon.inDungeon) return;
    if (!bloodOpen) return;
    if (gianttype) return
    const entities = World.getAllEntities();
    entities.forEach(ent => {
        const entity = ent.getEntity();
        const classent = ent.getClassName()
        if (ent.isInvisible()) return;
        if (!classent.includes("EntityGiantZombie")) return;
        let getArmor = entity.func_71124_b(1);
        let getArmorName = getArmor.func_82833_r()
        if (getArmorName == "Diamond Boots") {
            gianttype = "DIAMOND_GIANT"
            Client.showTitle(`&3&lDIAMOND GIANT`, `&c&lWARNING!`, 0, 40, 3)
            ChatLib.chat(`${Settings.prefix} &cBlood Giant is &3&lDiamond Giant`);
            World.playSound("mob.endermen.scream", 100, 0.2);
        } else {
            gianttype = "NOT_DIAMOND_GIANT"
            Client.showTitle(`&r`, `&2Shit Giant..`, 0, 40, 3)
            ChatLib.chat(`${Settings.prefix} &cBlood Giant is &2Shit`);
            World.playSound("mob.villager.idle", 100, 2);
        };
    });
}), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(onChatPacket(() => { 
    if (!Settings.bloodroom) return
    let elapsedMs = Date.now() - Dungeon.runStarted;
    let seconds = Math.floor(elapsedMs / 1000) % 60;
    let minutes = Math.floor(elapsedMs / (1000 * 60)) % 60;
    let timeTaken = `${minutes}m ${seconds}s`;
    ChatLib.chat(`${Settings.prefix} &c&lBLOOD DOOR &chas been opened! &f(&a${timeTaken}&f)`);
    ChatLib.command(`pc NA » Blood Opened! (${timeTaken})`, false);
}).setCriteria("The BLOOD DOOR has been opened!"), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(onChatPacket(() => {
    if (!Settings.bloodroom) return
    bloodOpen = false
    let timeTaken = (ticksElapsed / 20).toFixed(2);
    ChatLib.chat(`${Settings.prefix} &cBlood Done! &f(took &a${timeTaken}s&f)`);
    ChatLib.command(`pc NA » Blood Done! (took ${timeTaken}s)`, false);
    World.playSound("mob.skeleton.death", 100, 1);
}).setCriteria("[BOSS] The Watcher: You have proven yourself. You may pass."), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(onChatPacket(() => {
    if (!Settings.bloodroom) return
    ChatLib.chat(`${Settings.prefix} &cBlood Ready!`);
    ChatLib.command(`pc NA » Blood Ready!`, false);
}).setCriteria("[BOSS] The Watcher: That will be enough for now."), () => Settings.bloodroom && Dungeon.inDungeon);

registerWhen(register("worldUnload", () => {
    bloodOpen = false
    gianttype = null
    ticksElapsed = 0;
    checkblood = false
    sent = 0
}), () => Settings.bloodroom);

export { getBloodTicks };