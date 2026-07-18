package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.List;

public class EntityScale implements FeatureManager.Feature {
    public static boolean enabled = false;

    public static FloatSetting itemScale = new FloatSetting("Item Entity Scale", 1.0f, 0.1f, 15.0f, 0.05f);
    @Override
    public List<Settings> getSettings() {
        return List.of(itemScale);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.DIAMOND);
    }

    @Override
    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void initClient() {
    }
}