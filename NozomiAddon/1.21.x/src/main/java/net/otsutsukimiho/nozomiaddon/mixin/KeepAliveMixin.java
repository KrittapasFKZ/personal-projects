package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class KeepAliveMixin {
    @Inject(method = "onKeepAlive", at = @At("HEAD"))
    private void onKeepAlive(KeepAliveS2CPacket packet, CallbackInfo ci) {

    }
    @Inject(method = "onPing", at = @At("HEAD"))
    private void onPing(CommonPingS2CPacket packet, CallbackInfo ci) {

    }
}