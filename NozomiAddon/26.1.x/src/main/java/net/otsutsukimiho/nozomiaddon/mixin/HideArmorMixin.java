package net.otsutsukimiho.nozomiaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.otsutsukimiho.nozomiaddon.features.HideArmor;

@Mixin(HumanoidMobRenderer.class)
public abstract class HideArmorMixin {
    @Inject(method = "getEquipmentIfRenderable", at = @At("HEAD"), cancellable = true)
    private static void modifyArmorRender(LivingEntity entity, EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!(entity instanceof Player)) return;
        Minecraft client = Minecraft.getInstance();
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