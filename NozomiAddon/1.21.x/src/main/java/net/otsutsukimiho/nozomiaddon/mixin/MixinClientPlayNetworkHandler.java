package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.SoundSetting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow private ClientWorld world;

    @Inject(method = "onEntityTrackerUpdate", at = @At("TAIL"))
    private void onTrackerUpdate(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci) {
        if (!HideDamage.shouldCheck()) return;

        if (this.world == null) return;
        Entity entity = this.world.getEntityById(packet.id());

        if (entity instanceof ArmorStandEntity) {
            MinecraftClient.getInstance().execute(() -> {
                HideDamage.checkAndRemove((ArmorStandEntity) entity);
            });
        }
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void replaceBowSound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        if (!Tweaks.customBowSound.isEnabled()) return;

        SoundEvent original = packet.getSound().value();
        SoundSetting matchedSetting = null;

        if (original == SoundEvents.ENTITY_ARROW_SHOOT) {
            matchedSetting = Tweaks.bowShootSound;
        } else if (original == SoundEvents.ENTITY_ARROW_HIT) {
            matchedSetting = Tweaks.arrowHitSound;
        } else if (original == SoundEvents.ENTITY_ARROW_HIT_PLAYER) {
            matchedSetting = Tweaks.arrowHitPlayerSound;
        }

        if (matchedSetting == null) return;
        Identifier id = Identifier.of(matchedSetting.getSoundId());
        ci.cancel();

        final SoundSetting finalSetting = matchedSetting;
        MinecraftClient.getInstance().execute(() -> {
            PositionedSoundInstance sound = new PositionedSoundInstance(
                    id,
                    packet.getCategory(),
                    finalSetting.getVolume(),
                    finalSetting.getPitch(),
                    Random.create(),
                    false,
                    0,
                    SoundInstance.AttenuationType.LINEAR,
                    packet.getX(),
                    packet.getY(),
                    packet.getZ(),
                    false
            );

            client.getSoundManager().play(sound);
        });
    }
}