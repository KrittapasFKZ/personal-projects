package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.otsutsukimiho.nozomiaddon.features.TimeChanger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.ClientLevelData.class)
public class ClientWorldMixin {
    @Inject(method = "getGameTime", at = @At("HEAD"), cancellable = true)
    private void onGetTime(CallbackInfoReturnable<Long> cir) {
        if (TimeChanger.forceNight) {
            cir.setReturnValue(TimeChanger.timeTicks);
        }
    }
}