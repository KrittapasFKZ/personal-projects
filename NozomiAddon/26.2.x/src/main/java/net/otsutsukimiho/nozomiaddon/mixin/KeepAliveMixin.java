package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class KeepAliveMixin {
    @Inject(method = "handleKeepAlive", at = @At("HEAD"))
    private void onKeepAlive(ClientboundKeepAlivePacket packet, CallbackInfo ci) {

    }
    @Inject(method = "handlePing", at = @At("HEAD"))
    private void onPing(ClientboundPingPacket packet, CallbackInfo ci) {

    }
}