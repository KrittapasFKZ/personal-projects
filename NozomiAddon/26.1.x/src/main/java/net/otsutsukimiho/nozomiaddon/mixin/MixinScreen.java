package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.otsutsukimiho.nozomiaddon.features.LeapMenu;
import net.otsutsukimiho.nozomiaddon.features.TerminalsSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"), cancellable = true)
    public void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            TerminalsSolver.currentOverlay.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
}
