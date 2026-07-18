package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.otsutsukimiho.nozomiaddon.utils.HeadUtils;
import net.otsutsukimiho.nozomiaddon.utils.NumberSetting;
import net.otsutsukimiho.nozomiaddon.utils.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class HidePlayerLeap implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static long hideUntil = 0;

    public static NumberSetting hideDuration = new NumberSetting("HideDuration", 2000, 500, 5000, 100);
    @Override
    public List<Settings> getSettings() {
        return List.of(hideDuration);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTY1MjE0NjYxMjc0MiwKICAicHJvZmlsZUlkIiA6ICI5ZWU3NTUxOGQyZWE0Y2Q4OGJiNGI1YTZkNmVhNTFjYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaWNyb3MxMTgyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM3N2Q0YTIwNmQ3NzU3ZjQ3OWYzMzJlYzFhMmJiYmVlNTdjZWY5NzU2OGRkODhkZjgxZjQ4NjRhZWU3ZDNkOTgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    }

    public void initClient() {
        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            String raw = message.getString();
            if (raw.contains("You have teleported to")) {
                hideUntil = System.currentTimeMillis() + HidePlayerLeap.hideDuration.getValue();
            }
        });
    }

    public static boolean shouldHide(Entity entity) {
        if (System.currentTimeMillis() > hideUntil) return false;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;
        if (entity instanceof AbstractClientPlayer && entity != client.player) {
            return client.player.distanceToSqr(entity) <= 9.0;
        }
        return false;
    }

}