package net.otsutsukimiho.nozomiaddon.gui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final List<DraggableHudElement> elements = new ArrayList<>();

    public static void register(DraggableHudElement element) {
        elements.add(element);
    }

    public static List<DraggableHudElement> getElements() {
        return elements;
    }

    public static void initialize() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "draggable_hud"),
                (DrawContext context, RenderTickCounter tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.currentScreen instanceof EditHudScreen) return;

                    for (DraggableHudElement element : elements) {
                        if (element.isEnabled()) {
                            element.render(context, tickCounter);
                        }
                    }
                }
        );
    }
}