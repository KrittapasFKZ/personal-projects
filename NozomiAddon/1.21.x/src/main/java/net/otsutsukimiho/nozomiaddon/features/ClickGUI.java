package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class ClickGUI implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = true;

    public static ColorSetting headerColor = new ColorSetting("HeaderColor", new Color(255, 85, 255, 255), false);
    public static ModeSetting displayMode = new ModeSetting("ModMenu Style", "V2", "V1", "V2");
    @Override
    public java.util.List<Settings> getSettings() {
        return List.of(headerColor, displayMode);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.STRUCTURE_BLOCK);
    }

    public void initClient() { }
}