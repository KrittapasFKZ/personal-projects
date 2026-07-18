package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HitBox implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) { this.enabled = true; }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public static BooleanSetting hitboxLever = new BooleanSetting("1.8 Lever", false);
    public static BooleanSetting hitboxArmorStand = new BooleanSetting("1.8 ArmorStand", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(hitboxLever);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.STRUCTURE_VOID);
    }

    public void initClient() { }
}