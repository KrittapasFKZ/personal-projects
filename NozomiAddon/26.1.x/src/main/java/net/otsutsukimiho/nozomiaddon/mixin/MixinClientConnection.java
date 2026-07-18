package net.otsutsukimiho.nozomiaddon.mixin;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.events.EventBus;
import net.otsutsukimiho.nozomiaddon.utils.events.PacketEvent;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinClientConnection {

    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket) {
            InvincibleTimer.onPacketReceived();
            DungeonRunOverview.onPacketReceived();
            StormTick.onPacketReceived();
            LagDetector.onPacketReceived();
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

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        PacketEvent.Send event = new PacketEvent.Send(packet);
        EventBus.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}