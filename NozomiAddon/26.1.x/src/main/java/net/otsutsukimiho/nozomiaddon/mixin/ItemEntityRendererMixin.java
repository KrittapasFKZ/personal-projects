package net.otsutsukimiho.nozomiaddon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.otsutsukimiho.nozomiaddon.features.EntityScale;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private void onRender(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
        if (EntityScale.enabled) {
            matrices.pushPose();
            float scale = EntityScale.itemScale.getValue();
            matrices.scale(scale, scale, scale);
        }
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void onRenderEnd(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera, CallbackInfo ci) {
        if (EntityScale.enabled) {
            matrices.popPose();
        }
    }
}