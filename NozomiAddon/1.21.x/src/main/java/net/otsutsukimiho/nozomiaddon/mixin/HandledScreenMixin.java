package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.otsutsukimiho.nozomiaddon.features.LeapMenu;
import net.otsutsukimiho.nozomiaddon.features.TerminalsSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    private void nozomi$cancelVanillaClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
}