package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.otsutsukimiho.nozomiaddon.features.Tweaks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NameTagFeatureRenderer.class)
public abstract class NameTagShadowMixin {

    @ModifyConstant(
            method = "renderTranslucent",
            constant = @Constant(intValue = 0)
    )
    private int modifyNameTagShadow(int originalDropShadow) {
        return Tweaks.nameTagTextShadow.isEnabled() ? 1 : 0;
    }
}