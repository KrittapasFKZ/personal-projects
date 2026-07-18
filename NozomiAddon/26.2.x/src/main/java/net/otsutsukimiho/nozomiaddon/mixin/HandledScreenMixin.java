package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.otsutsukimiho.nozomiaddon.features.LeapMenu;
import net.otsutsukimiho.nozomiaddon.features.TerminalsSolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    private void nozomi$cancelVanillaClick(Slot slot, int slotId, int button, ContainerInput actionType, CallbackInfo ci) {
        if (TerminalsSolver.currentOverlay != null) {
            ci.cancel();
        }
        if (LeapMenu.isOpen) {
            ci.cancel();
        }
    }
}