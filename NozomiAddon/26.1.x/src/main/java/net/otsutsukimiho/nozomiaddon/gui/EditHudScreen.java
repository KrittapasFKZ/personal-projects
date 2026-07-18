package net.otsutsukimiho.nozomiaddon.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class EditHudScreen extends Screen {

    private final Screen parent;
    private final List<DraggableHudElement> elements;

    private DraggableHudElement draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    private static boolean editMode = false;

    public static boolean isEditMode() {
        return editMode;
    }

    public EditHudScreen(Screen parent) {
        super(Component.literal("§b§k§lA§r §d§lGUI Editing Mode §b§k§lA§r"));
        this.parent = parent;
        this.elements = HudManager.getElements();
    }

    @Override
    protected void init() {
        editMode = true;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (button) -> {
            for (DraggableHudElement element : elements) {
                editMode = false;
                element.saveConfig();
            }
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.centeredText(this.font, this.title, this.width / 2, 5, 0xFFFFAA00);
        for (DraggableHudElement element : elements) {
            if (element.isEnabled()) {
                element.renderForEditing(context);
            }
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();

        if (click.button() == 0) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                DraggableHudElement element = elements.get(i);
                if (element.isEnabled() && element.isInBounds(mouseX, mouseY)) {
                    draggingElement = element;
                    dragOffsetX = (int) (mouseX - element.getX());
                    dragOffsetY = (int) (mouseY - element.getY());
                    return true;
                }
            }
        } else if (click.button() == 1) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                DraggableHudElement element = elements.get(i);
                if (element.isEnabled() && element.isInBounds(mouseX, mouseY)) {
                    element.setPosition(0, 0);
                    element.resetSize();
                    if (minecraft.player != null) {
                        minecraft.player.playSound(SoundEvents.PISTON_CONTRACT, 1, 1.0f);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }


    @Override
    public boolean mouseDragged(MouseButtonEvent click, double mouseX, double mouseY) {
        if (click.button() == 0 && this.draggingElement != null) {
            int newX = (int) (click.x() - this.dragOffsetX);
            int newY = (int) (click.y() - this.dragOffsetY);
            this.draggingElement.setPosition(newX, newY);
            return true;
        }
        return super.mouseDragged(click, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0 && this.draggingElement != null) {
            this.draggingElement = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;

        for (int i = elements.size() - 1; i >= 0; i--) {
            DraggableHudElement element = elements.get(i);

            if (element.isEnabled() && element.isInBounds(mouseX, mouseY)) {
                int delta = (int) Math.signum(verticalAmount) * 2;
                element.resize(delta);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        editMode = false;
        for (DraggableHudElement element : elements) {
            element.saveConfig();
        }
        this.minecraft.setScreen(this.parent);
    }
}