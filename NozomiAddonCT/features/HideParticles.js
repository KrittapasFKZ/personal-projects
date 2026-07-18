import Settings from "../config";
import { registerWhen } from "../utils/utils";

const S2APacketParticles = Java.type('net.minecraft.network.play.server.S2APacketParticles').class

registerWhen(register("packetReceived", (packet, event) => {
    let type = packet.func_179749_a().toString()
    if (type.includes("HEART") && Settings.hideheartparticles) {
        cancel(event)
    };
    if (type.includes("VILLAGER_ANGRY") && Settings.hideangryvillagerparticles) {
        cancel(event)
    };
    if (type.includes("ENCHANTMENT_TABLE") && Settings.hideenchantmenttableparticles) {
        cancel(event)
    };
    if (type.includes("FIREWORKS_SPARK") && Settings.hidefireworksparkparticles) {
        cancel(event)
    };
}).setFilteredClass(S2APacketParticles), () => Settings.hideheartparticles || Settings.hideangryvillagerparticles || Settings.hidefireworksparkparticles|| Settings.hideenchantmenttableparticles);