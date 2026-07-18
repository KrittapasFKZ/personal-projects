package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;

public class KeyNotify implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public static TextSetting witherKey = new TextSetting("Wither Key Title", "&8&lWither Key Picked!");
    public static TextSetting bloodKey = new TextSetting("Blood Key Title", "&c&lBlood Key Picked!");
    public static SoundSetting customSound = new SoundSetting("PickUpSound", "minecraft:block.note_block.bit", 1.0f, 0.5f);
    @Override
    public java.util.List<Settings> getSettings() {
        return List.of(witherKey, bloodKey, customSound);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTYwMzYxMDQ0MzU4MywKICAicHJvZmlsZUlkIiA6ICIzM2ViZDMyYmIzMzk0YWQ5YWM2NzBjOTZjNTQ5YmE3ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYW5ub0JhbmFubm9YRCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNDllYzdkODJiMTQxNWFjYWUyMDU5Zjc4Y2QxZDE3NTRiOWRlOWIxOGNhNTlmNjA5MDI0YzRhZjg0M2Q0ZDI0IgogICAgfQogIH0KfQ==");
    }

    public void initClient() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            String msg = message.getString();
            if (msg.contains("has obtained Blood Key!")) {
                client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §c§lBlood Key Picked!"));
                customSound.playTestSound();

                String rawMessage = KeyNotify.bloodKey.getValue();
                if (rawMessage == null) rawMessage = " ";
                Component formattedText = ColorUtils.parseColor(rawMessage.trim());

                client.gui.hud.setTitle(Component.literal(" "));
                client.gui.hud.setSubtitle(formattedText);
                client.gui.hud.setTimes(0, 30, 10);
            }
            if (msg.contains("has obtained Wither Key!")) {
                client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §8§lWither Key Picked!"));
                customSound.playTestSound();

                String rawMessage = KeyNotify.witherKey.getValue();
                if (rawMessage == null) rawMessage = " ";
                Component formattedText = ColorUtils.parseColor(rawMessage.trim());

                client.gui.hud.setTitle(Component.literal(" "));
                client.gui.hud.setSubtitle(formattedText);
                client.gui.hud.setTimes(0, 30, 10);
            }
        });
    }

}