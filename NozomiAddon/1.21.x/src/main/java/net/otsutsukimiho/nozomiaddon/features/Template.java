package net.otsutsukimiho.nozomiaddon.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.config.*;

public class Template extends DraggableHudElement implements FeatureManager.Feature {
    public Template() {
        super("Template", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;

    public void initClient() {

    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter tickCounter) {

    }
}