package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.MinecraftClient;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathMessage implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean enabled = false;
    private static final Pattern DEATH_PATTERN = Pattern.compile("^ ☠ (\\S+) .+");

    public static StringSetting deathMessageText = new StringSetting("Message", "L {name}!");
    @Override
    public List<Settings> getSettings() {
        return List.of(deathMessageText);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.SKELETON_SKULL);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void initClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            String unformattedText = message.getString().replaceAll("§.", "");

            if (unformattedText.contains("reconnected") || unformattedText.contains("Cata Level")) return;

            Matcher matcher = DEATH_PATTERN.matcher(unformattedText);
            if (matcher.find()) {
                String playerName = matcher.group(1);
                sendPartyMessage(playerName);
            }
        });
    }

    private void sendPartyMessage(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.player.networkHandler == null) return;

        String text = DeathMessage.deathMessageText.getValue();

        if (text.contains("{name}")) {
            text = text.replace("{name}", playerName);
        }

        client.player.networkHandler.sendChatCommand("pc " + text);
    }
}