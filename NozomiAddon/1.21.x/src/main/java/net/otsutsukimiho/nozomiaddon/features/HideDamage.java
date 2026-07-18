package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.config.*;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class HideDamage implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static final Pattern DMG_PATTERN = Pattern.compile("^.?\\d[\\d,.]+.*?$");
    private static boolean isEnabled = false;

    public static BooleanSetting onlyInDungeon = new BooleanSetting("OnlyInDungeon", false);
    public static FloatSetting bobberDistance = new FloatSetting("Bobber Distance", 0.5f, 0.1f, 3f, 0.1f);
    @Override
    public List<Settings> getSettings() {
        return List.of(onlyInDungeon, bobberDistance);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    public void initClient() { }

    public static boolean shouldCheck() {
        return isEnabled;
    }

    public static void checkAndRemove(ArmorStandEntity stand) {
        if (stand == null) return;

        Text customName = stand.getCustomName();
        if (customName == null) return;

        String plain = customName.getString();
        if (plain.isEmpty()) return;

        if (HideDamage.onlyInDungeon.isEnabled() && !DUNGEON.inDungeon) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        if (DMG_PATTERN.matcher(plain.trim()).matches()) {
            Box searchBox = stand.getBoundingBox().expand(HideDamage.bobberDistance.getValue());
            boolean isNearBobber = !client.world.getEntitiesByClass(FishingBobberEntity.class, searchBox, b -> true).isEmpty();
            stand.setCustomNameVisible(isNearBobber);
        }
    }

}