package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

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
        super(Text.literal("§b§k§lA§r §d§lGUI Editing Mode §b§k§lA§r"));
        this.parent = parent;
        this.elements = HudManager.getElements();
    }

    @Override
    protected void init() {
        editMode = true;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), (button) -> {
            for (DraggableHudElement element : elements) {
                editMode = false;
                element.saveConfig();
            }
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xFFFFAA00);
        for (DraggableHudElement element : elements) {
            if (element.isEnabled()) {
                element.renderForEditing(context);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
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
                    if (client.player != null) {
                        client.player.playSound(SoundEvents.BLOCK_PISTON_CONTRACT, 1, 1.0f);
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }


    @Override
    public boolean mouseDragged(Click click, double mouseX, double mouseY) {
        if (click.button() == 0 && this.draggingElement != null) {
            int newX = (int) (click.x() - this.dragOffsetX);
            int newY = (int) (click.y() - this.dragOffsetY);
            this.draggingElement.setPosition(newX, newY);
            return true;
        }
        return super.mouseDragged(click, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(Click click) {
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
    public void close() {
        editMode = false;
        for (DraggableHudElement element : elements) {
            element.saveConfig();
        }
        this.client.setScreen(this.parent);
    }
}