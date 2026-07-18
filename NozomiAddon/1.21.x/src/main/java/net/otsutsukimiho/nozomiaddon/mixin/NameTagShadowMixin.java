package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.render.command.LabelCommandRenderer;

import net.otsutsukimiho.nozomiaddon.features.Tweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LabelCommandRenderer.class)
public abstract class NameTagShadowMixin {

    @ModifyArgs(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)V")
    )
    private void modifyNameTagArgs(Args args) {
        args.set(4, Tweaks.nameTagTextShadow.isEnabled());
    }
}