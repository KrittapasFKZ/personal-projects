package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.otsutsukimiho.nozomiaddon.features.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (HidePlayerLeap.shouldHide(entity)) {
            cir.cancel();
        }
        if (HidePlayers.shouldHide(entity)) {
            cir.setReturnValue(false);
        }
        if (CustomChatRules.shouldHide(entity)) {
            cir.setReturnValue(false);
        }
    }
}