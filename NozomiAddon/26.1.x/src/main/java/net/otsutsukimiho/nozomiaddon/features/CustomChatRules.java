package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.config.ChatRuleManager;
import net.otsutsukimiho.nozomiaddon.utils.BooleanSetting;
import net.otsutsukimiho.nozomiaddon.utils.Settings;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomChatRules implements FeatureManager.Feature {
    private boolean enabled = false;

    public static BooleanSetting dummy = new BooleanSetting("dummy", false);

    public static boolean isHidingByTime = false;
    public static long hideEndTime = 0;

    @Override
    public List<Settings> getSettings() {
        return List.of(dummy);
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
        ChatRuleManager.load();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            Minecraft client = Minecraft.getInstance();
            if (!enabled || !ChatRuleManager.enabled || client.player == null) return;

            String cleanMessage = message.getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "");

            for (ChatRuleManager.ChatRule rule : ChatRuleManager.rules) {
                if (rule.triggerMessage.isEmpty()) continue;
                if (cleanMessage.contains(rule.triggerMessage)) {
                    executeAction(client, rule, cleanMessage);
                }
            }
        });
    }

    private void executeAction(Minecraft client, ChatRuleManager.ChatRule rule, String originalMessage) {
        if (client.player == null) return;

        String parsedArg = rule.actionArg.replace("&", "§");

        switch (rule.actionType) {
            case "Alert Title":
                client.gui.setTitle(parsedArg.isEmpty() ? Component.empty() : Component.literal(parsedArg));
                client.gui.setSubtitle(Component.empty());
                client.gui.setTimes(0, 30, 10);
                break;
            case "Alert Subtitle":
                client.gui.setTitle(Component.empty());
                client.gui.setSubtitle(Component.literal(parsedArg));
                client.gui.setTimes(0, 30, 10);
                break;
            case "Alert Message":
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» " + parsedArg));
                break;
            case "Run Command":
                if (!parsedArg.isEmpty()) {
                    String cmd = parsedArg;
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    if (client.player.connection != null) {
                        client.player.connection.sendCommand(cmd);
                    }
                }
                break;
            case "Hide Players X seconds":
                try {
                    int seconds = Integer.parseInt(parsedArg);
                    if (seconds > 0) {
                        isHidingByTime = true;
                        hideEndTime = System.currentTimeMillis() + (seconds * 1000L);
                    } else {
                        isHidingByTime = false;
                    }
                } catch (NumberFormatException e) {
                    isHidingByTime = false;
                }
                break;
        }
    }

    public static boolean shouldHide(net.minecraft.world.entity.Entity entity) {
        if (!ChatRuleManager.enabled) return false;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return false;

        if (entity instanceof net.minecraft.world.entity.player.Player && entity != client.player) {
            if (isHidingByTime) {
                if (System.currentTimeMillis() < hideEndTime) {
                    return true;
                } else {
                    isHidingByTime = false;
                }
            }
        }

        return false;
    }
}