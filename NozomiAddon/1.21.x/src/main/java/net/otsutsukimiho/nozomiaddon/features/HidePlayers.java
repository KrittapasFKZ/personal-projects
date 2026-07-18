package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.List;

public class HidePlayers implements FeatureManager.Feature {
    public static boolean enabled = false;

    public static ModeSetting hideMode = new ModeSetting("Mode", "Hide In Range", "Hide In Range", "Hide Outside Range");
    public static NumberSetting hideRadius = new NumberSetting("Radius (Blocks)", 10, 1, 128, 1);

    @Override
    public List<Settings> getSettings() {
        return List.of(hideMode, hideRadius);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.PLAYER_HEAD);
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

    public static boolean shouldHide(Entity entity) {
        if (!enabled) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        if (entity instanceof PlayerEntity && entity != client.player) {
            double distance = client.player.distanceTo(entity);
            double radius = hideRadius.getValue();

            if (hideMode.getMode().equals("Hide In Range")) {
                return distance <= radius;
            } else {
                return distance >= radius;
            }
        }

        return false;
    }
}