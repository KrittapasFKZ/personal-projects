package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class CryptNotify extends DraggableHudElement implements FeatureManager.Feature {
    public CryptNotify() {
        super("CryptNotify", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    private static boolean complete = false;
    private static int cryptFound = 0;
    private static int reminderSent = 0;
    private static long hideAt = 0;
    private static Component titleText = Component.empty();

    private static final Map<String, Supplier<String>> DUMMY_DATA = Map.of(
            "{amount}", () -> "4",
            "{found}", () -> "2",
            "{total}", () -> "7"
    );

    public static BooleanSetting sendPartyChat = new BooleanSetting("SendPartyChat", false);
    public static BooleanSetting showTitle = new BooleanSetting("ShowTitle", false);
    public static BooleanSetting announcement = new BooleanSetting("Announcement", false);
    public static TextSetting needMessage = new TextSetting("Need Message", "&cNeed &e{amount} &cmore crypts!", DUMMY_DATA);
    public static TextSetting doneMessage = new TextSetting("Done Message", "&aCrypts Done!");
    public static TextSetting defaultHud = new TextSetting("Default HUD", "&7Crypt&f: &c{found}&f/&a5", DUMMY_DATA);
    public static TextSetting doneHud = new TextSetting("Done HUD", "&7Crypt&f: &aDONE &7(&b{total}&7)", DUMMY_DATA);
    public static BooleanSetting hideWhenDone = new BooleanSetting("HideWhenDone", false);
    public static NumberSetting firstAnnounce = new NumberSetting("#1 Announce(s)", 60,10,240,1);
    public static NumberSetting secondAnnounce = new NumberSetting("#2 Announce(s)", 90,30,240,1);
    public static SoundSetting customSound1 = new SoundSetting("Done Sound", "minecraft:entity.zombie.death", 1.0f, 0.75f);
    public static SoundSetting customSound2 = new SoundSetting("Alert Sound", "minecraft:entity.zombie.hurt", 1.0f, 0.75f);
    @Override
    public List<Settings> getSettings() {
        return List.of(sendPartyChat, showTitle, announcement, needMessage, doneMessage, defaultHud, doneHud, hideWhenDone, firstAnnounce, secondAnnounce, customSound1, customSound2);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1ODcwMTg2Nzk1NzYsInByb2ZpbGVJZCI6IjJkYzc3YWU3OTQ2MzQ4MDI5NDI4MGM4NDIyNzRiNTY3IiwicHJvZmlsZU5hbWUiOiJzYWR5MDYxMCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI1Njg5NWI5NjU5ODk2YWQ2NDdmNTg1OTkyMzhhZjUzMmQ0NmRiOWMxYjAzODliOGJiZWI3MDk5OWRhYjMzZCJ9fX0=");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            complete = false;
            cryptFound = 0;
            reminderSent = 0;
            hideAt = 0;
        });
        ClientTickEvents.END_LEVEL_TICK.register(c -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            if (DUNGEON.bossEntry != -1) return;

            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            long elapsedMs = System.currentTimeMillis() - DUNGEON.runStarted;
            long seconds = elapsedMs / 1000;
            cryptFound = DUNGEON.crypts;

            if (!complete) {
                if (cryptFound >= 5) {
                    complete = true;
                    customSound1.playTestSound();
                    client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §cCrypts Done! §f(§a" + getTime(elapsedMs) + "§f)"));

                    String rawMessage = CryptNotify.doneMessage.getValue();
                    if (rawMessage == null) rawMessage = " ";
                    titleText = ColorUtils.parseColor(rawMessage.trim());

                    hideAt = System.currentTimeMillis() + 3000;
                    if (CryptNotify.sendPartyChat.isEnabled()) {
                        client.player.connection.sendCommand("pc NA » Crypts Done! (" + cryptFound + "/5) (" + getTime(elapsedMs) + ")");
                    }
                }
            }

            if (reminderSent <= 1) {
                if (!CryptNotify.announcement.isEnabled()) return;
                if (cryptFound >= 5) return;
                if ((reminderSent == 0 && seconds >= CryptNotify.firstAnnounce.getValue() && seconds < (CryptNotify.firstAnnounce.getValue() + 10)) || (reminderSent == 1 && seconds >= CryptNotify.secondAnnounce.getValue() && seconds < (CryptNotify.secondAnnounce.getValue() + 10))) {
                    reminderSent++;
                    customSound2.playTestSound();
                    client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §cNeed §e" + (5 - cryptFound) + " §cmore Crypts! §e#" + reminderSent));

                    String rawMessage = CryptNotify.needMessage.getValue();
                    if (rawMessage == null) rawMessage = " ";
                    String newMsg = rawMessage.replace("{amount}", String.valueOf((5 - cryptFound))).trim();
                    titleText = ColorUtils.parseColor(newMsg);

                    hideAt = System.currentTimeMillis() + 3000;
                    if (CryptNotify.sendPartyChat.isEnabled()) {
                        client.player.connection.sendCommand("pc NA » We need " + (5 - cryptFound) + " more Crypts! #" + reminderSent);
                    }
                }
            }

        });
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if ((System.currentTimeMillis() < hideAt) && CryptNotify.showTitle.isEnabled()) {
            Minecraft client = Minecraft.getInstance();
            int width = client.getWindow().getGuiScaledWidth();
            int height = client.getWindow().getGuiScaledHeight();
            var matrices = ctx.pose();

            matrices.pushMatrix();
            ctx.pose().translate(width / 2.0f, height / 2.0f - 30);
            ctx.pose().scale(3f, 3f);

            ctx.centeredText(Minecraft.getInstance().font, titleText, 0, 0, 0xFFFFFFFF);

            ctx.pose().popMatrix();
        }

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (DUNGEON.bossEntry != -1) return;
            if (CryptNotify.hideWhenDone.isEnabled() && cryptFound >= 5) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        String default_rawMessage = CryptNotify.defaultHud.getValue();
        if (default_rawMessage == null) default_rawMessage = " ";
        String default_newMsg = default_rawMessage.replace("{found}", String.valueOf(cryptFound)).trim();

        String done_rawMessage = CryptNotify.doneHud.getValue();
        if (done_rawMessage == null) done_rawMessage = " ";
        String done_newMsg = done_rawMessage.replace("{total}", String.valueOf(cryptFound)).trim();

        String textStr = cryptFound >= 5 ? done_newMsg : default_newMsg;
        Component textToDraw = ColorUtils.parseColor(textStr);

        ctx.text(Minecraft.getInstance().font, textToDraw, 0, 0, 0xFFFFFFFF, true);
        ctx.pose().popMatrix();

        this.width = Math.round(50 * scale);
        this.height = Math.round(10 * scale);
    }

    public static String getTime(Long ms) {
        if (ms == null || ms == 0) return "?";
        long minutes = ms / 60000;
        long seconds = (ms / 1000) % 60;
        if (minutes != 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

}