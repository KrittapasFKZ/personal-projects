package net.otsutsukimiho.nozomiaddon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.List;

public class SoundSetting extends Settings {
    private String soundId;
    private float volume;
    private float pitch;

    public static final List<String> ALL_SOUNDS = Registries.SOUND_EVENT.getIds().stream().map(Identifier::toString).sorted().toList();

    public SoundSetting(String name, String defaultSound, float defaultVolume, float defaultPitch) {
        super(name);
        this.soundId = defaultSound;
        this.volume = defaultVolume;
        this.pitch = defaultPitch;
    }

    public String getSoundId() { return soundId; }
    public void setSoundId(String soundId) { this.soundId = soundId; }

    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = volume; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public void playTestSound() {
        try {
            Identifier id = Identifier.of(soundId);
            SoundEvent event = Registries.SOUND_EVENT.get(id);
            if (event != null) {
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.ui(event, pitch, volume)
                );
            }
        } catch (Exception ignored) {}
    }
}