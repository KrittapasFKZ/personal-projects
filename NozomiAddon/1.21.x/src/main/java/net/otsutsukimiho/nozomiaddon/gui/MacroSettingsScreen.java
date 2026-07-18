package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import net.otsutsukimiho.nozomiaddon.NozomiAddonClient;
import net.otsutsukimiho.nozomiaddon.config.*;

import java.util.ArrayList;
import java.util.List;

public class MacroSettingsScreen extends Screen {

    private final Screen parent;
    private static final int SIDEBAR_WIDTH = 120;
    private static final int ROW_HEIGHT = 55;

    private double scrollOffsetY = 0;
    private double targetScrollOffsetY = 0;
    private int contentHeight = 0;
    private long lastRenderTime = 0;

    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;

    private MacroManager.Macro bindingMacro = null;
    private final List<MacroUI> uiElements = new ArrayList<>();

    private ButtonWidget allowInGuiBtn;
    private ButtonWidget addBtn;

    public MacroSettingsScreen(Screen parent) {
        super(Text.literal("Custom Keybinds"));
        this.parent = parent;
    }

    private static class MacroUI {
        MacroManager.Macro macro;
        TextFieldWidget cmdField;
        ButtonWidget keyBtn;
        ButtonWidget toggleBtn;
        ButtonWidget deleteBtn;
    }

    @Override
    protected void init() {
        uiElements.clear();
        lastRenderTime = System.currentTimeMillis();

        int mainWidth = this.width - SIDEBAR_WIDTH - 20;
        int usableWidth = mainWidth - 30;
        int topBtnWidth = (usableWidth - 5) / 2;

        allowInGuiBtn = ButtonWidget.builder(Text.literal("Allow in GUI: " + (MacroManager.allowInGui ? "§aEnabled" : "§cDisabled")), btn -> {
            MacroManager.allowInGui = !MacroManager.allowInGui;
            btn.setMessage(Text.literal("Allow in GUI: " + (MacroManager.allowInGui ? "§aEnabled" : "§cDisabled")));
            MacroManager.save();
        }).dimensions(-1000, 0, topBtnWidth, 20).build();
        this.addDrawableChild(allowInGuiBtn);

        addBtn = ButtonWidget.builder(Text.literal("Add New Macro"), btn -> {
            MacroManager.macros.add(new MacroManager.Macro());
            MacroManager.save();
            this.init(this.width, this.height);
        }).dimensions(-1000, 0, topBtnWidth, 20).build();
        this.addDrawableChild(addBtn);

        for (MacroManager.Macro m : MacroManager.macros) {
            MacroUI ui = new MacroUI();
            ui.macro = m;

            ui.cmdField = new TextFieldWidget(textRenderer, -1000, 0, 200, 16, Text.literal(""));
            ui.cmdField.setMaxLength(256);
            ui.cmdField.setText(m.command);
            ui.cmdField.setChangedListener(val -> {
                m.command = val;
                MacroManager.save();
            });
            this.addDrawableChild(ui.cmdField);

            ui.keyBtn = ButtonWidget.builder(Text.literal(m.getKeyName()), btn -> {
                bindingMacro = m;
                btn.setMessage(Text.literal("..."));
            }).dimensions(-1000, 0, 100, 20).build();
            this.addDrawableChild(ui.keyBtn);

            ui.toggleBtn = ButtonWidget.builder(Text.literal(m.enabled ? "§aEnabled" : "§cDisabled"), btn -> {
                m.enabled = !m.enabled;
                btn.setMessage(Text.literal(m.enabled ? "§aEnabled" : "§cDisabled"));
                MacroManager.save();
            }).dimensions(-1000, 0, 100, 20).build();
            this.addDrawableChild(ui.toggleBtn);

            ui.deleteBtn = ButtonWidget.builder(Text.literal("§cDelete"), btn -> {
                MacroManager.macros.remove(m);
                MacroManager.save();
                this.init(this.width, this.height);
            }).dimensions(-1000, 0, 60, 42).build();
            this.addDrawableChild(ui.deleteBtn);

            uiElements.add(ui);
        }

        contentHeight = 55 + (MacroManager.macros.size() * ROW_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount != 0) {
            targetScrollOffsetY -= verticalAmount * 35;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void clampScroll() {
        int mainHeight = this.height - 70;
        int maxScroll = Math.max(0, contentHeight - mainHeight);
        if (targetScrollOffsetY < 0) targetScrollOffsetY = 0;
        if (targetScrollOffsetY > maxScroll) targetScrollOffsetY = maxScroll;
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        int code = key.getKeycode();

        if (bindingMacro != null) {
            if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                bindingMacro.keycode = -1;
            } else {
                bindingMacro.keycode = code;
            }
            MacroManager.save();
            bindingMacro = null;
            this.init(this.width, this.height);
            return true;
        }

        return super.keyPressed(key);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        MinecraftClient client = MinecraftClient.getInstance();

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;
        int mainHeight = this.height - 70;

        if (contentHeight > mainHeight) {
            int maxScroll = contentHeight - mainHeight;
            int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
            int scrollbarY = mainY + (int) ((scrollOffsetY / maxScroll) * (mainHeight - scrollbarHeight));

            if (click.x() >= mainX + mainWidth - 6 && click.x() <= mainX + mainWidth && click.y() >= scrollbarY && click.y() <= scrollbarY + scrollbarHeight) {
                if (click.button() == 0) {
                    isDraggingScrollbar = true;
                    scrollbarDragOffsetY = click.y() - scrollbarY;
                    return true;
                }
            }
        }

        if (click.x() >= 5 && click.x() <= SIDEBAR_WIDTH - 5 && click.y() >= 35 && click.y() < 35 + 18) {
            if (click.button() == 0) {
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.close();
                return true;
            }
        }

        if (bindingMacro != null) {
            bindingMacro.keycode = -100 - click.button();
            MacroManager.save();
            bindingMacro = null;
            this.init(this.width, this.height);
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        isDraggingScrollbar = false;
        return super.mouseReleased(click);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastRenderTime == 0 ? 0.016f : (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        int mainHeight = this.height - 70;
        int maxScroll = Math.max(0, contentHeight - mainHeight);

        if (isDraggingScrollbar) {
            if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                isDraggingScrollbar = false;
            } else {
                int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
                float scrollFraction = (float) (mouseY - scrollbarDragOffsetY - 30) / (mainHeight - scrollbarHeight);
                scrollFraction = MathHelper.clamp(scrollFraction, 0.0f, 1.0f);
                targetScrollOffsetY = scrollFraction * maxScroll;
                scrollOffsetY = targetScrollOffsetY;
            }
        }

        if (Math.abs(targetScrollOffsetY - scrollOffsetY) > 0.5f) {
            scrollOffsetY += (targetScrollOffsetY - scrollOffsetY) * 15f * deltaTime;
        } else {
            scrollOffsetY = targetScrollOffsetY;
        }

        context.fill(0, 0, SIDEBAR_WIDTH, this.height, 0x90000000);
        context.drawCenteredTextWithShadow(this.textRenderer, "§d§lNozomiAddon §bv" + NozomiAddonClient.MOD_VERSION, SIDEBAR_WIDTH / 2, 10, 0xFFFFFFFF);
        context.fill(10, 25, SIDEBAR_WIDTH - 10, 26, 0x55FFFFFF);

        int backBtnY = 35;
        boolean isBackHovered = mouseX >= 5 && mouseX <= SIDEBAR_WIDTH - 5 && mouseY >= backBtnY && mouseY < backBtnY + 18;

        if (isBackHovered) {
            context.fill(5, backBtnY, SIDEBAR_WIDTH - 5, backBtnY + 18, 0x40FFFFFF);
        }
        int backTextColor = isBackHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
        context.drawTextWithShadow(this.textRenderer, "◄ Back to Menu", 15, backBtnY + 5, backTextColor);

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;

        context.drawCenteredTextWithShadow(this.textRenderer, "§nCustom KeyBinds", mainX + (mainWidth / 2), 15, 0xFFFFFFFF);

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, 0x60000000);
        context.enableScissor(mainX, mainY, mainX + mainWidth, mainY + mainHeight);

        int currentY = mainY + 10 - (int)scrollOffsetY;
        int elementX = mainX + 15;
        int topBtnWidth = (mainWidth - 35) / 2;

        if (currentY + 20 > mainY && currentY < mainY + mainHeight) {
            allowInGuiBtn.setX(elementX); allowInGuiBtn.setY(currentY); allowInGuiBtn.visible = true;
            addBtn.setX(elementX + topBtnWidth + 5); addBtn.setY(currentY); addBtn.visible = true;
        } else {
            allowInGuiBtn.visible = false; addBtn.visible = false;
        }
        currentY += 35;

        for (MacroUI ui : uiElements) {
            if (currentY + 50 > mainY && currentY < mainY + mainHeight) {
                int fieldWidth = mainWidth - 100;

                ui.cmdField.setX(elementX);
                ui.cmdField.setY(currentY);
                ui.cmdField.setWidth(fieldWidth);
                ui.cmdField.visible = true;

                ui.deleteBtn.setX(elementX + fieldWidth + 10);
                ui.deleteBtn.setY(currentY);
                ui.deleteBtn.visible = true;

                ui.keyBtn.setX(elementX);
                ui.keyBtn.setY(currentY + 22);
                ui.keyBtn.visible = true;

                ui.toggleBtn.setX(elementX + 105);
                ui.toggleBtn.setY(currentY + 22);
                ui.toggleBtn.visible = true;

            } else {
                ui.cmdField.visible = false; ui.deleteBtn.visible = false;
                ui.keyBtn.visible = false; ui.toggleBtn.visible = false;
            }
            currentY += ROW_HEIGHT;
        }

        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        if (contentHeight > mainHeight) {
            int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
            int scrollbarY = mainY + (int) ((scrollOffsetY / maxScroll) * (mainHeight - scrollbarHeight));

            context.fill(mainX + mainWidth - 4, mainY, mainX + mainWidth, mainY + mainHeight, 0x40000000);

            int scrollColor = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.fill(mainX + mainWidth - 4, scrollbarY, mainX + mainWidth, scrollbarY + scrollbarHeight, scrollColor);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}