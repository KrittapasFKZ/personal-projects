package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RagAlert implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public static TextSetting alertMessage = new TextSetting("Alert Message", "&e&l&kAA&r &6&n&lRAG&r &e&l&kAA&r");
    public static SoundSetting customSound = new SoundSetting("Alert Sound", "minecraft:entity.player.levelup", 1.0f, 0.5f);
    @Override
    public List<Settings> getSettings() {
        return List.of(alertMessage, customSound);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.GOLDEN_AXE);
    }

    public void initClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) return;
            if (client.player == null) return;
            String msg = message.getString();
            if (msg.contains("[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you.")) {
                customSound.playTestSound();

                String rawMessage = RagAlert.alertMessage.getValue();
                if (rawMessage == null) rawMessage = " ";
                Text formattedText = ColorUtils.parseColor(rawMessage.trim());

                client.inGameHud.setTitle(formattedText);
                client.inGameHud.setSubtitle(Text.literal(" "));
                client.inGameHud.setTitleTicks(0, 50, 5);
            }
        });
    }

}