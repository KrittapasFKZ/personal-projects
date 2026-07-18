package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.List;
import java.util.Map;

public class TimeChanger implements FeatureManager.Feature {
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    public static boolean forceNight = false;
    public static long timeTicks = 0;

    public static ModeSetting mode = new ModeSetting("Time", "Sunrise", "Sunrise", "Noon", "Sunset", "Midnight");
    @Override
    public List<Settings> getSettings() {
        return List.of(mode);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.CLOCK);
    }

    private static final Map<String, Long> TICKS_TABLE = Map.ofEntries(
            Map.entry("Sunrise", 0L),
            Map.entry("Noon", 6000L),
            Map.entry("Sunset", 12000L),
            Map.entry("Midnight", 18000L)
    );

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            forceNight = enabled;
            timeTicks = TICKS_TABLE.getOrDefault(mode.getMode(), 0L);
        });
    }
}