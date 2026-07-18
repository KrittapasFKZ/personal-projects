package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Clock extends DraggableHudElement implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Clock() {
        super("Clock", 10, 10, 50, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public static ModeSetting timeFormat = new ModeSetting("Format", "HH:mm:ss", "HH:mm:ss", "HH:mm", "hh:mm AM/PM");
    @Override
    public List<Settings> getSettings() {
        return List.of(timeFormat);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.CLOCK);
    }

    public void initClient() {

    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        DateTimeFormatter formatter;
        switch (timeFormat.getMode()) {
            case "HH:mm":
                formatter = DateTimeFormatter.ofPattern("HH:mm");
                break;
            case "hh:mm AM/PM":
                formatter = DateTimeFormatter.ofPattern("hh:mm a");
                break;
            case "HH:mm:ss":
            default:
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                break;
        }

        String timeStr = LocalTime.now().format(formatter);
        String displayText = String.format("§f%s", timeStr);

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 25 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);
        ctx.getMatrices().popMatrix();

        this.width = Math.round(60 * scale);
        this.height = Math.round(10 * scale);
    }

}