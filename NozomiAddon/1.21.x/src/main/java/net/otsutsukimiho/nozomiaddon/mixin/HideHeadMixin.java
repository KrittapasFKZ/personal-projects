package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.otsutsukimiho.nozomiaddon.features.HideArmor;

@Mixin(LivingEntity.class)
public abstract class HideHeadMixin {

    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private void nozomi$hideHead(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.HEAD) return;
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof PlayerEntity)) return;
        if (!HideArmor.helmet.isEnabled()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getCameraEntity() == null) return;
        boolean isSelf = entity.getId() == client.getCameraEntity().getId();
        if (!HideArmor.checkEnabled()) return;
        if (HideArmor.onlyOwn.isEnabled() && !isSelf) return;
        cir.setReturnValue(ItemStack.EMPTY);
    }
}