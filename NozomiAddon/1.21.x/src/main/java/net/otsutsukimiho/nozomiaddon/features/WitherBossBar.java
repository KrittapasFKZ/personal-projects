package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.mixin.BossBarHudAccessor;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.*;
import java.util.function.Supplier;

public class WitherBossBar extends DraggableHudElement implements FeatureManager.Feature {
    private boolean enabled = false;

    public WitherBossBar() {
        super("WitherBossBar", 10, 10, 100, 10, 10);
    }

    private final Map<String, Map<String, Long>> bossHealth = Map.of(
            "F7", Map.of(
                    "Maxor", 100_000_000L,
                    "Storm", 400_000_000L,
                    "Goldor", 750_000_000L,
                    "Necron", 1_000_000_000L,
                    "None", 0L
            ),
            "M7", Map.of(
                    "Maxor", 800_000_000L,
                    "Storm", 1_000_000_000L,
                    "Goldor", 1_200_000_000L,
                    "Necron", 1_400_000_000L,
                    "None", 0L
            ),
            "None", Map.of(
                    "Maxor", 0L,
                    "Storm", 0L,
                    "Goldor", 0L,
                    "Necron", 0L,
                    "None", 0L
            )
    );

    private String targetFloor = DungeonUtils.getCurrentDungeon();
    private boolean inP5 = false;
    private String currentBoss = "None";
    private long currentHP = 0;
    private static long hideAt = 0;
    private static Text titleText = Text.empty();
    private boolean bossInitialized = false;
    private boolean bossDeadAnnounced = false;

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    private static final Map<String, Supplier<String>> DUMMY_DATA = Map.of(
            "{name}", () -> "Necron",
            "{health}", () -> "850.5M"
    );

    public static BooleanSetting announceDead = new BooleanSetting("AnnounceDead", false);
    public static TextSetting customMessage = new TextSetting("Custom Message", "&c&l{name} &r&a{health} &c❤", DUMMY_DATA);
    @Override
    public List<Settings> getSettings() {
        return List.of(announceDead, customMessage);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WITHER_SKELETON_SKULL);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            inP5 = false;
            currentBoss = "None";
        });
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!enabled || client.world == null) return;
            if (!DUNGEON.inDungeon) return;
            if (inP5) return;
            targetFloor = DungeonUtils.getCurrentDungeon();
            if (targetFloor == null) targetFloor = "None";

            BossBarHud hud = client.inGameHud.getBossBarHud();
            Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) hud).getBossBars();
            Map<String, Long> floorMap = bossHealth.getOrDefault(targetFloor, Collections.emptyMap());
            Long maxHp = floorMap.getOrDefault(currentBoss, 0L);

            for (ClientBossBar bar : bars.values()) {
                String title = removeFormat(bar.getName().getString());
                float percent = bar.getPercent();
                if (title.contains(currentBoss)) {
                    currentHP = (long) (maxHp * percent);
                    if (!bossInitialized && percent >= 0.99f && maxHp > 0) {
                        bossInitialized = true;
                    }
                    if (bossInitialized && !bossDeadAnnounced && percent <= 0.0f) {
                        bossDeadAnnounced = true;

                        titleText = Text.literal("§4§l" + currentBoss + " §cDIED!");
                        hideAt = System.currentTimeMillis() + 2000;

                        if (WitherBossBar.announceDead.isEnabled() && client.player != null) {
                            client.player.sendMessage(Text.literal("§d§lNA §f§l» §4§l" + currentBoss + " §chas died!"), false);
                        }
                    }
                }
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!enabled || client.world == null) return;
            if (!DUNGEON.inDungeon) return;

            String raw = message.getString();

            if (!inP5 && raw.contains("[BOSS] Necron: All this, for nothing...")) {
                inP5 = true;
            }

            if (!inP5 && currentBoss.equals("None") && raw.contains("[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!")) {
                switchBoss("Maxor");
            }

            if (!inP5 && currentBoss.equals("Maxor") && raw.contains("[BOSS] Storm: Pathetic Maxor, just like expected.")) {
                switchBoss("Storm");
            }

            if (!inP5 && currentBoss.equals("Storm") && raw.contains("[BOSS] Goldor: Who dares trespass into my domain?")) {
                switchBoss("Goldor");
            }

            if (!inP5 && currentBoss.equals("Goldor") && raw.contains("[BOSS] Necron: You went further than any human before, congratulations.")) {
                switchBoss("Necron");
            }
        });
    }

    private void switchBoss(String bossName) {
        currentBoss = bossName;
        currentHP = 0;
        bossInitialized = false;
        bossDeadAnnounced = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.of("§d§lNA §f§l» §4§l" + bossName + " §chas spawned!"), false);
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if ((System.currentTimeMillis() < hideAt) && WitherBossBar.announceDead.isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            var matrices = ctx.getMatrices();

            matrices.pushMatrix();
            ctx.getMatrices().translate(width / 2.0f, height / 2.0f - 30);
            ctx.getMatrices().scale(2f, 2f);

            ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, titleText, 0, 0, 0xFFFFFFFF);

            ctx.getMatrices().popMatrix();
        }

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (currentBoss.contains("None")) return;
            if (inP5) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        String rawMessage = WitherBossBar.customMessage.getValue();
        if (rawMessage == null) rawMessage = " ";
        String newMsg = rawMessage.replace("{name}", currentBoss).replace("{health}", shortNumber(currentHP)).trim();
        Text displayText = ColorUtils.parseColor(newMsg);

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 35 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);
        ctx.getMatrices().popMatrix();

        this.width = Math.round(70 * scale);
        this.height = Math.round(10 * scale);
    }

    private String removeFormat(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "").trim();
    }

    private String shortNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        String[] units = {"K", "M", "B", "T"};
        int unitIndex = Math.max(0, (int) ((Math.log10(number) / 3) - 1));
        double shortened = number / Math.pow(1000, unitIndex + 1);
        return String.format("%.1f%s", shortened, units[unitIndex]);
    }
}
