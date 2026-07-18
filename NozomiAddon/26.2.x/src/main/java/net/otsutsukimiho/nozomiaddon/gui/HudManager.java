package net.otsutsukimiho.nozomiaddon.gui;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
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
                Identifier.fromNamespaceAndPath(MOD_ID, "draggable_hud"),
                (GuiGraphicsExtractor context, DeltaTracker tickCounter) -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.gui.screen() instanceof EditHudScreen) return;

                    for (DraggableHudElement element : elements) {
                        if (element.isEnabled()) {
                            element.render(context, tickCounter);
                        }
                    }
                }
        );
    }
}