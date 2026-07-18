package net.otsutsukimiho.nozomiaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.otsutsukimiho.nozomiaddon.features.DisableHotbarScroll;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (DisableHotbarScroll.shouldBlockScroll()) {
            Minecraft client = Minecraft.getInstance();

            if (client.gui.screen() == null) {
                ci.cancel();
            }
        }
    }
}