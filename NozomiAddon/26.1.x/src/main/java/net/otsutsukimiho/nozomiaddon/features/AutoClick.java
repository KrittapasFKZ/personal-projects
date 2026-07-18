package net.otsutsukimiho.nozomiaddon.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.mixin.MinecraftClientAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AutoClick implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean enabled = false;

    private static List<String> getAllLocalItems() {
        List<String> items = new ArrayList<>();
        try {
            File itemsFile = new File("config/nozomiaddon/items.json");
            if (itemsFile.exists()) {
                JsonObject itemsObj = JsonParser.parseReader(new FileReader(itemsFile)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : itemsObj.entrySet()) {
                    String itemName = entry.getKey();
                    if (!items.contains(itemName)) {
                        items.add(itemName);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load items.json for ignore list", e);
        }
        items.sort(String::compareToIgnoreCase);
        return items;
    }

    public static MultiSelectSetting allowlist = new MultiSelectSetting(
            "Sword List",
            List.of("Dark Claymore", "Giant's Sword", "Hyperion", "Valkyrie", "Scylla", "Astraea",
                    "Midas' Sword", "Atomsplit Katana", "Pyrochaos Dagger", "Deathripper Dagger"),
            "Dark Claymore"
    );
    public static BooleanSetting listOnly = new BooleanSetting("Sword Only", false);
    public static RangeSetting cps = new RangeSetting("Sword CPS", 5, 10, 1, 20, 1);
    public static MultiSelectSetting bow_allowlist = new MultiSelectSetting(
            "Bow List",
            List.of("Terminator", "Mosquito Shortbow"),
            "Terminator"
    );
    public static BooleanSetting bow_listOnly = new BooleanSetting("Bow Only", false);
    public static RangeSetting bow_cps = new RangeSetting("Bow CPS", 5, 10, 1, 20, 1);
    public static BooleanSetting enableLeftClick = new BooleanSetting("Enable Left Click", false);
    public static BooleanSetting enableRightClick = new BooleanSetting("Enable Right Click", false);
    public static RangeSetting leftCps = new RangeSetting("Left CPS", 5, 10, 1, 20, 1);
    public static RangeSetting rightCps = new RangeSetting("Right CPS", 5, 10, 1, 20, 1);
    public static MultiSelectSetting ignoreList = new MultiSelectSetting(
            "Ignore List",
            getAllLocalItems(),
            ""
    );

    private long nextLeftClick = 0;
    private long nextRightClick = 0;

    @Override
    public List<Settings> getSettings() {
        return List.of(listOnly, allowlist, cps, bow_listOnly, bow_allowlist, bow_cps, ignoreList, enableLeftClick, leftCps, enableRightClick, rightCps);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (client.screen != null) return;
            if (client.player == null) return;
            if (isHoldingIgnoredItem(client)) return;

            long nowMillis = System.currentTimeMillis();

            if (AutoClick.listOnly.isEnabled()) {
                if (isHoldingAllowedItem(client, AutoClick.allowlist) && client.options.keyAttack.isDown() && nowMillis >= nextLeftClick) {
                    double randomCps = AutoClick.cps.getRandomValue();
                    if (randomCps <= 0) randomCps = 1;
                    nextLeftClick = nowMillis + (long) ((1000.0 / randomCps) + ((Math.random() - 0.5) * 60.0));
                    simulateLeftClick(client);
                }
            } else if (AutoClick.enableLeftClick.isEnabled() && client.options.keyAttack.isDown() && nowMillis >= nextLeftClick) {
                double randomCps = AutoClick.leftCps.getRandomValue();
                if (randomCps <= 0) randomCps = 1;
                nextLeftClick = nowMillis + (long) ((1000.0 / randomCps) + ((Math.random() - 0.5) * 60.0));
                simulateLeftClick(client);
            }

            if (AutoClick.bow_listOnly.isEnabled()) {
                if (isHoldingAllowedItem(client, AutoClick.bow_allowlist) && client.options.keyUse.isDown() && nowMillis >= nextLeftClick) {
                    double randomCps = AutoClick.bow_cps.getRandomValue();
                    if (randomCps <= 0) randomCps = 1;
                    nextLeftClick = nowMillis + (long) ((1000.0 / randomCps) + ((Math.random() - 0.5) * 60.0));
                    simulateLeftClick(client);
                }
            } else if (AutoClick.enableRightClick.isEnabled() && client.options.keyUse.isDown() && nowMillis >= nextRightClick) {
                double randomCps = AutoClick.rightCps.getRandomValue();
                if (randomCps <= 0) randomCps = 1;
                nextRightClick = nowMillis + (long) ((1000.0 / randomCps) + ((Math.random() - 0.5) * 60.0));
                simulateRightClick(client);
            }

        });
    }

    private boolean isHoldingAllowedItem(Minecraft client, MultiSelectSetting targetList) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        if (mainHand == null || mainHand.isEmpty()) return false;
        Component name = mainHand.getHoverName();
        if (name == null) return false;

        String itemName = name.getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "");

        for (String allowedItem : targetList.getSelectedOptions()) {
            if (itemName.contains(allowedItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isHoldingIgnoredItem(Minecraft client) {
        if (AutoClick.ignoreList.getSelectedOptions().isEmpty()) return false;
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        if (mainHand == null || mainHand.isEmpty()) return false;
        Component name = mainHand.getHoverName();
        if (name == null) return false;

        String itemName = name.getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "");

        for (String ignoredItem : AutoClick.ignoreList.getSelectedOptions()) {
            if (itemName.contains(ignoredItem)) {
                return true;
            }
        }
        return false;
    }

    private void simulateLeftClick(Minecraft client) {
        ((MinecraftClientAccessor) client).setAttackCooldown(0);
        KeyMapping.click(client.options.keyAttack.getDefaultKey());
    }

    private void simulateRightClick(Minecraft client) {
        KeyMapping.click(client.options.keyUse.getDefaultKey());
    }
}