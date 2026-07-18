package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.gui.*;

import net.otsutsukimiho.nozomiaddon.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InvincibleTimer extends DraggableHudElement implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public InvincibleTimer() {
        super("InvincibleTimer", 10, 10, 100, 36, 1);
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

    public static ModeSetting displayMode = new ModeSetting("DisplayMode", "Ticks", "Seconds", "Ticks");
    @Override
    public List<Settings> getSettings() {
        return List.of(displayMode);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1MDUyMjI5OTg3MzQsInByb2ZpbGVJZCI6IjBiZTU2MmUxNzIyODQ3YmQ5MDY3MWYxNzNjNjA5NmNhIiwicHJvZmlsZU5hbWUiOiJ4Y29vbHgzIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn0sInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJiZTcyMWQ3YWQ4YWI5NjVmMDhjYmVjMGI4MzRmNzc5YjUxOTdmNzlkYTRhZWEzZDEzZDI1M2VjZTlkZWMyIn19fQ==");
    }

    public void initClient() {
        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            if (!enabled) return;
            String raw = message.getString();
            if (spiritCD == 0 && raw.contains("Second Wind Activated! Your Spirit Mask saved your life!")) {
                spiritTime = 60;
                spiritCD = 600;
            }
            if (bonzoCD == 0 && raw.contains("Bonzo's Mask saved your life!")) {
                bonzoTime = 60;
                bonzoCD = 3600;
            }
            if (phoenixCD == 0 && raw.contains("Your Phoenix Pet saved you from certain death!")) {
                phoenixTime = 60;
                phoenixCD = 1200;
            }
        });
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        float scale = this.size / 10.0f;
        int y = 0;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        boolean useSeconds = InvincibleTimer.displayMode.is("Seconds");

        if (EditHudScreen.isEditMode()) {
            String timeStr = useSeconds ? "3.00s" : "60t";
            String text = "§6Spirit§f: §a" + timeStr;
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int centerX = 25 - textWidth / 2;
            ctx.drawText(MinecraftClient.getInstance().textRenderer, text, centerX, 0, 0xFFFFFFFF, true);
        }

        if (spiritTime > 0) {
            float timeStr = useSeconds ? (spiritTime / 20f) : spiritTime;
            String text = useSeconds ? String.format("§6Spirit§f: §a%.2fs", timeStr) : String.format("§6Spirit§f: §a%.0ft", timeStr);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int centerX = 25 - textWidth / 2;
            ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), centerX, y, 0xFFFFFFFF, true);
            y += 12;
        }
        if (bonzoTime > 0) {
            float timeStr = useSeconds ? (bonzoTime / 20f) : bonzoTime;
            String text = useSeconds ? String.format("§9Bonzo§f: §a%.2fs", timeStr) : String.format("§9Bonzo§f: §a%.0ft", timeStr);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int centerX = 25 - textWidth / 2;
            ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), centerX, y, 0xFFFFFFFF, true);
            y += 12;
        }
        if (phoenixTime > 0){
            float timeStr = useSeconds ? (phoenixTime / 20f) : phoenixTime;
            String text = useSeconds ? String.format("§cPhoenix§f: §a%.2fs", timeStr) : String.format("§cPhoenix§f: §a%.0ft", timeStr);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(text);
            int centerX = 25 - textWidth / 2;
            ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), centerX, y, 0xFFFFFFFF, true);
        }

        ctx.getMatrices().popMatrix();
        this.width = Math.round(50 * scale);
        this.height = Math.round(10 * scale);
    }

    public static void onPacketReceived() {
        updateCooldowns();
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

}