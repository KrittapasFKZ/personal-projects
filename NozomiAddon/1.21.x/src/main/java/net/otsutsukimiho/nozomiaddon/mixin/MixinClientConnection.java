package net.otsutsukimiho.nozomiaddon.mixin;

import io.netty.channel.ChannelFutureListener;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;

import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.events.EventBus;
import net.otsutsukimiho.nozomiaddon.utils.events.PacketEvent;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof CommonPingS2CPacket) {
            InvincibleTimer.onPacketReceived();
            DungeonRunOverview.onPacketReceived();
            StormTick.onPacketReceived();
            ShieldCD.onPacketReceived();
            RagTimer.onPacketReceived();
            AutoRefill.onPacketReceived();
            MaskTimer.onPacketReceived();
        }
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        EventBus.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        PacketEvent.Send event = new PacketEvent.Send(packet);
        EventBus.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}