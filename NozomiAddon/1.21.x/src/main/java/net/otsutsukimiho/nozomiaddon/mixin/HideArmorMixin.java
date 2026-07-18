package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.otsutsukimiho.nozomiaddon.features.HideArmor;

@Mixin(BipedEntityRenderer.class)
public abstract class HideArmorMixin {
    @Inject(method = "getEquippedStack", at = @At("HEAD"), cancellable = true)
    private static void modifyArmorRender(LivingEntity entity, EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!(entity instanceof PlayerEntity)) return;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isSelf = (entity == client.getCameraEntity());
        if (HideArmor.onlyOwn.isEnabled() && !isSelf) return;
        if (!HideArmor.checkEnabled()) return;
        if (slot == EquipmentSlot.HEAD && HideArmor.helmet.isEnabled()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else if (slot == EquipmentSlot.CHEST && HideArmor.chestplate.isEnabled()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else if (slot == EquipmentSlot.LEGS && HideArmor.leggings.isEnabled()) {
            cir.setReturnValue(ItemStack.EMPTY);
        } else if (slot == EquipmentSlot.FEET && HideArmor.boots.isEnabled()) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}