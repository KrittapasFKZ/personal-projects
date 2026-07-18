package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Tweaks implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) { this.enabled = true; }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public static BooleanSetting nameTagTextShadow = new BooleanSetting("Render NameTag TextShadow", false);
    public static BooleanSetting ownNameTag = new BooleanSetting("Render Own NameTag", false);
    public static BooleanSetting customBowSound = new BooleanSetting("CustomBowSound", false);
    public static SoundSetting bowShootSound = new SoundSetting("BowShootSound", "minecraft:entity.arrow.shoot", 1.0f, 1.0f);
    public static SoundSetting arrowHitSound = new SoundSetting("ArrowHitSound", "minecraft:entity.arrow.hit", 1.0f, 1.0f);
    public static SoundSetting arrowHitPlayerSound = new SoundSetting("ArrowHitPlayerSound", "minecraft:entity.arrow.hit_player", 1.0f, 1.0f);
    @Override
    public List<Settings> getSettings() {
        return List.of(nameTagTextShadow, ownNameTag, customBowSound, bowShootSound, arrowHitSound, arrowHitPlayerSound);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.STRUCTURE_BLOCK);
    }

    public void initClient() { }
}