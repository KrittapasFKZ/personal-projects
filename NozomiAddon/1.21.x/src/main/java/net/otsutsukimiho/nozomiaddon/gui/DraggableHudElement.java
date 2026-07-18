package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.otsutsukimiho.nozomiaddon.config.ConfigManager;
import net.otsutsukimiho.nozomiaddon.features.FeatureManager;

import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.config.*;

public abstract class DraggableHudElement {

    protected String configKey;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int size;

    public DraggableHudElement(String configKey, int defaultX, int defaultY, int defaultWidth, int defaultHeight, int size) {
        this.configKey = configKey;
        UiElementConfig cfg = ConfigManager.getElement(configKey);
        this.x = cfg.x;
        this.y = cfg.y;
        this.width = cfg.width;
        this.height = cfg.height;
        this.size = cfg.size;

        HudManager.register(this);
    }

    public abstract void render(DrawContext context, RenderTickCounter tickCounter);

    public void renderForEditing(DrawContext context) {
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0x80808080);
        render(context, null);
    }

    public boolean isInBounds(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resetSize() {
        this.size = 10;
    }

    public void saveConfig() {
        ConfigManager.setElement(configKey, new UiElementConfig(this.x, this.y, this.width, this.height, this.size));
    }

    public void resize(int delta) {
        int newSize = this.size + delta;

        if (newSize < 5) newSize = 5;
        if (newSize > 200) newSize = 200;

        if (newSize == this.size) return;

        float scale = (float) newSize / this.size;

        this.width = Math.round(this.width * scale);
        this.height = Math.round(this.height * scale);
        this.size = newSize;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getConfigKey() { return configKey; }

    public boolean isEnabled() {
        FeatureManager.Feature feature = FeatureManager.getRegistered().get(configKey);
        return feature != null && feature.isEnabled();
    }
}