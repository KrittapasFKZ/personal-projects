package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.otsutsukimiho.nozomiaddon.features.LeapMenu;
import net.otsutsukimiho.nozomiaddon.features.TerminalsSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            TerminalsSolver.currentOverlay.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    public void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            TerminalsSolver.currentOverlay.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
    @Inject(method = "renderMain", at = @At("HEAD"), cancellable = true)
    public void onRenderMain(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            TerminalsSolver.currentOverlay.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
}