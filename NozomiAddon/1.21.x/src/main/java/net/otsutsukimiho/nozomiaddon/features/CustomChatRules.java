package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

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
            MinecraftClient client = MinecraftClient.getInstance();
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

    private void executeAction(MinecraftClient client, ChatRuleManager.ChatRule rule, String originalMessage) {
        if (client.player == null) return;

        String parsedArg = rule.actionArg.replace("&", "§");

        switch (rule.actionType) {
            case "Alert Title":
                client.inGameHud.setTitle(parsedArg.isEmpty() ? Text.empty() : Text.literal(parsedArg));
                client.inGameHud.setSubtitle(Text.empty());
                client.inGameHud.setTitleTicks(0, 30, 10);
                break;
            case "Alert Subtitle":
                client.inGameHud.setTitle(Text.empty());
                client.inGameHud.setSubtitle(Text.literal(parsedArg));
                client.inGameHud.setTitleTicks(0, 30, 10);
                break;
            case "Alert Message":
                client.player.sendMessage(Text.literal("§d§lNA §f§l» " + parsedArg), false);
                break;
            case "Run Command":
                if (!parsedArg.isEmpty()) {
                    String cmd = parsedArg;
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    if (client.player.networkHandler != null) {
                        client.player.networkHandler.sendChatCommand(cmd);
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

    public static boolean shouldHide(net.minecraft.entity.Entity entity) {
        if (!ChatRuleManager.enabled) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        if (entity instanceof net.minecraft.entity.player.PlayerEntity && entity != client.player) {
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