package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.otsutsukimiho.nozomiaddon.features.HitBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {
    @Inject(method = "getBaseDimensions", at = @At("HEAD"), cancellable = true)
    private void revertArmorStandHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        try {
            if (!HitBox.hitboxArmorStand.isEnabled()) return;
            ArmorStandEntity self = (ArmorStandEntity) (Object) this;
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