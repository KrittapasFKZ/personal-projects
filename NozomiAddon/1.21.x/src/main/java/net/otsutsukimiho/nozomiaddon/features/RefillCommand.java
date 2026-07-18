package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.item.ItemStack;

import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RefillCommand implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = true;

    public static CheckMarkSetting triggerEPearl = new CheckMarkSetting("Ender Pearl", false);
    public static CheckMarkSetting triggerSuperB = new CheckMarkSetting("Superboom", false);
    public static CheckMarkSetting triggerJerry = new CheckMarkSetting("Inflatable Jerry", false);
    public static CheckMarkSetting triggerDecoy = new CheckMarkSetting("Decoy", false);
    public static CheckMarkSetting triggerToxic = new CheckMarkSetting("Toxic Arrow Poison", false);
    public static CheckMarkSetting triggerTwilight = new CheckMarkSetting("Twilight Arrow Poison", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(triggerEPearl, triggerSuperB, triggerJerry, triggerDecoy, triggerToxic, triggerTwilight);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTU5Nzk4NDQ2MTMxOCwKICAicHJvZmlsZUlkIiA6ICJiNzQ3OWJhZTI5YzQ0YjIzYmE1NjI4MzM3OGYwZTNjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTeWxlZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=");
    }

    public void initClient() { }
}