package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RenderMode implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = true;

    public static ModeSetting renderType = new ModeSetting("RenderType", "CoolBox", "Outline", "Filled", "CoolBox");
    public static BooleanSetting depthTest = new BooleanSetting("DepthTest", false);
    public static FloatSetting outlineThickness = new FloatSetting("Line Thickness", 0.5f, 0.05f, 3.0f, 0.05f);
    @Override
    public List<Settings> getSettings() {
        return List.of(depthTest, outlineThickness, renderType);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.SPYGLASS);
    }

    public void initClient() { }
}