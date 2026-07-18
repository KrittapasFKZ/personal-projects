package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.gui.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;

public class MaskTimer extends DraggableHudElement implements FeatureManager.Feature {

    private ItemStack spiritItem = null;
    private ItemStack bonzoItem = null;
    private ItemStack phoenixItem = null;
    private ItemStack cachedIcon = null;

    public MaskTimer() {
        super("MaskTimer", 10, 10, 140, 50, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static float bonzoTime = 0;
    private static float spiritTime = 0;
    private static float phoenixTime = 0;
    private static float bonzoCD = 0;
    private static float spiritCD = 0;
    private static float phoenixCD = 0;
    private boolean spiritUsed = false;
    private boolean bonzoUsed = false;
    private boolean phoenixUsed = false;

    private static final String SPIRIT_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1MDUyMjI5OTg3MzQsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn0sInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiZTcyMWQ3YWQ4YWI5NjVmMDhjYmVjMGI4MzRmNzc5YjUxOTdmNzlkYTRhZWEzZDEzZDI1M2VjZTlkZWMyIn19fQ==";
    private static final String BONZO_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1ODc5MDgzMDU4MjYsInByb2ZpbGVJZCI6IjJkYzc3YWU3OTQ2MzQ4MDI5NDI4MGM4NDIyNzRiNTY3IiwicHJvZmlsZU5hbWUiOiJzYWR5MDYxMCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI3MTZlY2JmNWI4ZGEwMGIwNWYzMTZlYzZhZjYxZThiZDAyODA1YjIxZWI4ZTQ0MDE1MTQ2OGRjNjU2NTQ5YyJ9fX0=";
    private static final String PHOENIX_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY0Mjg2NTc3MTM5MSwKICAicHJvZmlsZUlkIiA6ICJiYjdjY2E3MTA0MzQ0NDEyOGQzMDg5ZTEzYmRmYWI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJsYXVyZW5jaW8zMDMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZiMWI1OWJjODkwYzljOTc1Mjc3ODdkZGUyMDYwMGM4Yjg2ZjZiOTkxMmQ1MWE2YmZjZGIwZTRjMmFhM2M5NyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
    public static BooleanSetting onlyInBoss = new BooleanSetting("OnlyInBoss", false);
    public static ModeSetting displayMode = new ModeSetting("DisplayMode", "Name and Icon", "Only Icon", "Only Name", "Name and Icon");
    public static SoundSetting customSound1 = new SoundSetting("Proc Sound", "minecraft:entity.experience_orb.pickup", 1.0f, 1.25f);
    public static SoundSetting customSound2 = new SoundSetting("Ready Sound", "minecraft:entity.experience_orb.pickup", 1.0f, 1.25f);
    @Override
    public List<Settings> getSettings() {
        return List.of(onlyInBoss, displayMode, customSound1, customSound2);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1MDUyMjI5OTg3MzQsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn0sInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiZTcyMWQ3YWQ4YWI5NjVmMDhjYmVjMGI4MzRmNzc5YjUxOTdmNzlkYTRhZWEzZDEzZDI1M2VjZTlkZWMyIn19fQ==");
    }

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (spiritCD <= 0 && spiritUsed) {
                showMaskTitle("§6Spirit Mask §a§lREADY!");
                spiritUsed = false;
            }
            if (bonzoCD <= 0 && bonzoUsed) {
                showMaskTitle("§9Bonzo's Mask §a§lREADY!");
                bonzoUsed = false;
            }
            if (phoenixCD <= 0 && phoenixUsed) {
                showMaskTitle("§cPhoenix §a§lREADY!");
                phoenixUsed = false;
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            if (!enabled) return;
            String raw = message.getString();
            if (spiritCD <= 0 && raw.contains("Second Wind Activated! Your Spirit Mask saved your life!")) {
                spiritTime = 60;
                spiritCD = 600;
                spiritUsed = true;
                showMaskTitle("§6Spirit Mask §e§lUSED!");
            }
            if (bonzoCD <= 0 && raw.contains("Bonzo's Mask saved your life!")) {
                bonzoTime = 60;
                bonzoCD = 3600;
                bonzoUsed = true;
                showMaskTitle("§9Bonzo's Mask §e§lUSED!");
            }
            if (phoenixCD <= 0 && raw.contains("Your Phoenix Pet saved you from certain death!")) {
                phoenixTime = 60;
                phoenixCD = 1200;
                phoenixUsed = true;
                showMaskTitle("§cPhoenix §e§lUSED!");
            }
        });
    }

    public static void onPacketReceived() {
        updateCooldowns();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if (spiritItem == null) spiritItem = HeadUtils.getSkull(SPIRIT_TEXTURE);
        if (bonzoItem == null) bonzoItem = HeadUtils.getSkull(BONZO_TEXTURE);
        if (phoenixItem == null) phoenixItem = HeadUtils.getSkull(PHOENIX_TEXTURE);

        float scale = this.size / 10.0f;
        int y = 0;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) {
                if (spiritCD > 0) {
                    drawMaskWithItem(ctx, spiritItem, "§6Spirit Mask", spiritCD, y);
                    y += 15;
                }
                if (bonzoCD > 0) {
                    drawMaskWithItem(ctx, bonzoItem, "§9Bonzo's Mask", bonzoCD, y);
                    y += 15;
                }
                if (phoenixCD > 0) {
                    drawMaskWithItem(ctx, phoenixItem, "§cPhoenix", phoenixCD, y);
                }
            } else {
                if (MaskTimer.onlyInBoss.isEnabled() && DUNGEON.bossEntry == -1) { /* (=3=) */ } else {
                    drawMaskWithItem(ctx, spiritItem, "§6Spirit Mask", spiritCD, y);
                    y += 15;
                    drawMaskWithItem(ctx, bonzoItem, "§9Bonzo's Mask", bonzoCD, y);
                    y += 15;
                    drawMaskWithItem(ctx, phoenixItem, "§cPhoenix", phoenixCD, y);
                }
            }
        } else {
            drawMaskWithItem(ctx, spiritItem, "§6Spirit Mask", spiritCD, y);
            y += 15;
            drawMaskWithItem(ctx, bonzoItem, "§9Bonzo's Mask", bonzoCD, y);
            y += 15;
            drawMaskWithItem(ctx, phoenixItem, "§cPhoenix", phoenixCD, y);
        }

        ctx.pose().popMatrix();
        this.width = Math.round(140 * scale);
        this.height = Math.round(50 * scale);
    }

    private static void updateCooldowns() {
        float tps = 1f;

        spiritTime = Math.max(0f, spiritTime - tps);
        bonzoTime = Math.max(0f, bonzoTime - tps);
        phoenixTime = Math.max(0f, phoenixTime - tps);

        spiritCD = Math.max(0f, spiritCD - tps);
        bonzoCD = Math.max(0f, bonzoCD - tps);
        phoenixCD = Math.max(0f, phoenixCD - tps);
    }

    private void showMaskTitle(String text) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.nullToEmpty("§d§lNA §f§l» " + text));
                if (text.contains("READY")) { customSound2.playTestSound(); } else { customSound1.playTestSound(); }
                Minecraft.getInstance().gui.hud.setTitle(Component.literal(" "));
                Minecraft.getInstance().gui.hud.setSubtitle(Component.literal(text));
                Minecraft.getInstance().gui.hud.setTimes(0,40,0);
            }
        });
    }

    private void drawMaskWithItem(GuiGraphicsExtractor ctx, ItemStack item, String name, float cooldown, int y) {
        boolean showIcon = MaskTimer.displayMode.is("Name and Icon") || MaskTimer.displayMode.is("Only Icon");
        boolean showName = MaskTimer.displayMode.is("Name and Icon") || MaskTimer.displayMode.is("Only Name");

        if (showIcon) {
            ctx.item(item, 0, y);
        }

        String status;

        if (showName) {
            status = cooldown > 0
                    ? String.format("%s§f: §7%.2f", name, cooldown / 20.0)
                    : String.format("%s§f: §a§lREADY", name);
        } else {
            status = cooldown > 0
                    ? String.format("§7%.1f", cooldown / 20.0)
                    : "§a§lREADY";
        }

        int textX = showIcon ? 16 : 0;
        int textY = y + 4;

        ctx.text(Minecraft.getInstance().font, Component.literal(status), textX, textY, 0xFFFFFFFF, true);
    }
}