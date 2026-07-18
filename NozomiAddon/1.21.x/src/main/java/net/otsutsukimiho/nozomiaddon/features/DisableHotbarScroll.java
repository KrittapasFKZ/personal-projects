package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DisableHotbarScroll implements FeatureManager.Feature {
    private volatile boolean enabled = false;
    private static boolean isEnabled = false;

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        isEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.STRUCTURE_BLOCK);
    }

    public static boolean shouldBlockScroll() {
        return isEnabled;
    }

    @Override
    public void initClient() { }
}