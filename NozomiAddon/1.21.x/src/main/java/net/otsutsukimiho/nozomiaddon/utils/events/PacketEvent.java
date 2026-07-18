package net.otsutsukimiho.nozomiaddon.utils.events;

import net.minecraft.network.packet.Packet;

public abstract class PacketEvent extends CancellableEvent {
    public final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }
}