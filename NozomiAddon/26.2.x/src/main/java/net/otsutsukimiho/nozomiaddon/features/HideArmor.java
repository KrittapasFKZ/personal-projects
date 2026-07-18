package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HideArmor implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) { this.enabled = enabled; featureEnabled = enabled; }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static boolean featureEnabled = false;

    public static BooleanSetting onlyOwn = new BooleanSetting("Only Own", false);
    public static CheckMarkSetting helmet = new CheckMarkSetting("Helmet", false);
    public static CheckMarkSetting chestplate = new CheckMarkSetting("Chestplate", false);
    public static CheckMarkSetting leggings = new CheckMarkSetting("Leggings", false);
    public static CheckMarkSetting boots = new CheckMarkSetting("Boots", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(onlyOwn, helmet, chestplate, leggings, boots);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.DIAMOND_CHESTPLATE);
    }

    public void initClient() { }

    public static boolean checkEnabled() {
        return featureEnabled;
    }

}