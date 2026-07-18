package net.otsutsukimiho.nozomiaddon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.SoundSetting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityData", at = @At("TAIL"))
    private void onTrackerUpdate(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        if (!HideDamage.shouldCheck()) return;

        if (this.level == null) return;
        Entity entity = this.level.getEntity(packet.id());

        if (entity instanceof ArmorStand) {
            Minecraft.getInstance().execute(() -> {
                HideDamage.checkAndRemove((ArmorStand) entity);
            });
        }
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"), cancellable = true)
    private void replaceBowSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        if (!Tweaks.customBowSound.isEnabled()) return;

        SoundEvent original = packet.getSound().value();
        SoundSetting matchedSetting = null;

        if (original == SoundEvents.ARROW_SHOOT) {
            matchedSetting = Tweaks.bowShootSound;
        } else if (original == SoundEvents.ARROW_HIT) {
            matchedSetting = Tweaks.arrowHitSound;
        } else if (original == SoundEvents.ARROW_HIT_PLAYER) {
            matchedSetting = Tweaks.arrowHitPlayerSound;
        }

        if (matchedSetting == null) return;
        Identifier id = Identifier.parse(matchedSetting.getSoundId());
        ci.cancel();

        final SoundSetting finalSetting = matchedSetting;
        Minecraft.getInstance().execute(() -> {
            SimpleSoundInstance sound = new SimpleSoundInstance(
                    id,
                    packet.getSource(),
                    finalSetting.getVolume(),
                    finalSetting.getPitch(),
                    RandomSource.create(),
                    false,
                    0,
                    SoundInstance.Attenuation.LINEAR,
                    packet.getX(),
                    packet.getY(),
                    packet.getZ(),
                    false
            );

            client.getSoundManager().play(sound);
        });
    }
}