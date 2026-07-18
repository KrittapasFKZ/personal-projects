package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.otsutsukimiho.nozomiaddon.features.HitBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandEntityMixin {
    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true)
    private void revertArmorStandHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        try {
            if (!HitBox.hitboxArmorStand.isEnabled()) return;
            ArmorStand self = (ArmorStand) (Object) this;
            if (self.isMarker()) return;
            if (self.isBaby()) {
                cir.setReturnValue(EntityDimensions.fixed(0.7F, 1.1875F));
            } else {
                cir.setReturnValue(EntityDimensions.fixed(0.7F, 2.175F));
            }
        } catch (Throwable t) {
            //
        }
    }
}