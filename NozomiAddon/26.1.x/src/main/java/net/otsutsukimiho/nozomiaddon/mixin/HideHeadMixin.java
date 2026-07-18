package net.otsutsukimiho.nozomiaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.otsutsukimiho.nozomiaddon.features.HideArmor;

@Mixin(LivingEntity.class)
public abstract class HideHeadMixin {

    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void nozomi$hideHead(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.HEAD) return;
        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof Player)) return;
        if (!HideArmor.helmet.isEnabled()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.getCameraEntity() == null) return;
        boolean isSelf = entity.getId() == client.getCameraEntity().getId();
        if (!HideArmor.checkEnabled()) return;
        if (HideArmor.onlyOwn.isEnabled() && !isSelf) return;
        cir.setReturnValue(ItemStack.EMPTY);
    }
}