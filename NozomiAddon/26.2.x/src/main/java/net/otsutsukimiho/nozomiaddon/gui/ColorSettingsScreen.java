package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.otsutsukimiho.nozomiaddon.config.*;

import java.awt.Color;

public class ColorSettingsScreen extends Screen {

    private final Screen parent;
    private final String featureName;
    private int currentColor;

    private boolean draggingR, draggingG, draggingB, draggingA;
    private EditBox rField, gField, bField;

    public ColorSettingsScreen(Screen parent, String featureName) {
        super(Component.literal("Color Settings"));
        this.parent = parent;
        this.featureName = featureName;
        this.currentColor = ConfigManager.getColor(featureName);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Reset"), btn -> {
            updateColor(255, 85, 255);
            updateFieldsFromColor();
        }).bounds(centerX - 25, centerY + 50, 50, 20).build());

        rField = createColorField(centerY - 50, "R");
        gField = createColorField(centerY - 25, "G");
        bField = createColorField(centerY, "B");

        updateFieldsFromColor();
    }

    private EditBox createColorField(int y, String type) {
        int centerX = this.width / 2;
        EditBox field = new EditBox(font, centerX - 60, y - 6, 30, 14, Component.literal(""));
        field.setMaxLength(3);

        field.setResponder(val -> {
            if (val.isEmpty()) return;
            try {
                int newValue = Integer.parseInt(val);
                int clamped = Mth.clamp(newValue, 0, 255);
                updateColorComponent(type, clamped);
            } catch (NumberFormatException ignored) {
            }
        });

        this.addRenderableWidget(field);
        return field;
    }

    private void updateFieldsFromColor() {
        Color c = new Color(currentColor, true);
        if (rField != null) rField.setValue(String.valueOf(c.getRed()));
        if (gField != null) gField.setValue(String.valueOf(c.getGreen()));
        if (bField != null) bField.setValue(String.valueOf(c.getBlue()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.fill(centerX - 100, centerY - 60, centerX + 100, centerY + 80, 0x80000000);
        context.fill(centerX - 100, centerY - 80, centerX + 100, centerY - 60, 0xFFFF55FF);
        context.centeredText(font, featureName, centerX, centerY - 75, 0xFFFFFFFF);

        Color c = new Color(currentColor, true);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        context.fill(centerX - 15, centerY + 15, centerX + 15, centerY + 45, currentColor);

        drawSlider(context, "R", r, centerX - 20, centerY - 50, mouseX, mouseY, draggingR, 0xFFFF5555, rField);
        drawSlider(context, "G", g, centerX - 20, centerY - 25, mouseX, mouseY, draggingG, 0xFF55FF55, gField);
        drawSlider(context, "B", b, centerX - 20, centerY, mouseX, mouseY, draggingB, 0xFF5555FF, bField);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void drawSlider(GuiGraphicsExtractor context, String label, int value, int x, int y, int mouseX, int mouseY, boolean isDragging, int color, EditBox linkedField) {
        int width = 100;
        int height = 4;

        context.text(font, label, x - 55, y - 4, 0xFFFFFFFF, true);

        context.fill(x, y, x + width, y + height, 0xFF444444);

        if (isDragging) {
            float percent = (float)(mouseX - x) / (float)width;
            int newValue = (int)(Mth.clamp(percent, 0, 1) * 255);

            if (newValue != value) {
                updateColorComponent(label, newValue);
                if (linkedField != null) {
                    linkedField.setValue(String.valueOf(newValue));
                }
            }
            value = newValue;
        }

        int knockerX = x + (int)((value / 255.0f) * width);
        context.fill(knockerX - 2, y - 4, knockerX + 2, y + 8, color);
    }

    private void updateColorComponent(String type, int val) {
        Color c = new Color(currentColor, true);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        switch (type) {
            case "R" -> r = val;
            case "G" -> g = val;
            case "B" -> b = val;
        }
        updateColor(r, g, b);
    }

    private void updateColor(int r, int g, int b) {
        this.currentColor = new Color(r, g, b, 255).getRGB();
        ConfigManager.setColor(featureName, this.currentColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() != 0) return false;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int sliderX = centerX - 20;
        int width = 100;

        if (checkSliderClick(click.x(), click.y(), sliderX, centerY - 50, width)) draggingR = true;
        else if (checkSliderClick(click.x(), click.y(), sliderX, centerY - 25, width)) draggingG = true;
        else if (checkSliderClick(click.x(), click.y(), sliderX, centerY, width)) draggingB = true;
        else if (checkSliderClick(click.x(), click.y(), sliderX, centerY + 25, width)) draggingA = true;

        return draggingR || draggingG || draggingB || draggingA;
    }

    private boolean checkSliderClick(double mouseX, double mouseY, int x, int y, int width) {
        return mouseX >= x - 5 && mouseX <= x + width + 5 && mouseY >= y - 5 && mouseY <= y + 10;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        draggingR = draggingG = draggingB = draggingA = false;
        return super.mouseReleased(click);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}