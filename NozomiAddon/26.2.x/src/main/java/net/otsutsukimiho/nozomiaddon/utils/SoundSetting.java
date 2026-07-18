package net.otsutsukimiho.nozomiaddon.utils;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SoundSetting extends Settings {
    private String soundId;
    private float volume;
    private float pitch;

    public static final List<String> ALL_SOUNDS = BuiltInRegistries.SOUND_EVENT.keySet().stream().map(Identifier::toString).sorted().toList();

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
            Identifier id = Identifier.parse(soundId);
            SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(id);
            if (event != null) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(event, pitch, volume)
                );
            }
        } catch (Exception ignored) {}
    }
}