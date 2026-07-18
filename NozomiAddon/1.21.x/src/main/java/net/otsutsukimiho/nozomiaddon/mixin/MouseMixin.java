package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.otsutsukimiho.nozomiaddon.features.DisableHotbarScroll;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (DisableHotbarScroll.shouldBlockScroll()) {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.currentScreen == null) {
                ci.cancel();
            }
        }
    }
}