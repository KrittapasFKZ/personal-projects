package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import net.otsutsukimiho.nozomiaddon.NozomiAddonClient;
import net.otsutsukimiho.nozomiaddon.config.ChatRuleManager;

import java.util.ArrayList;
import java.util.List;

public class ChatRuleSettingsScreen extends Screen {

    private final Screen parent;
    private static final int SIDEBAR_WIDTH = 120;
    private static final int ROW_HEIGHT = 50;

    public static final String[] ACTION_TYPES = {
            "Hide Players X seconds", "Alert Title", "Alert Subtitle", "Alert Message", "Run Command"
    };

    private double scrollOffsetY = 0;
    private double targetScrollOffsetY = 0;
    private int contentHeight = 0;
    private long lastRenderTime = 0;

    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;

    private ChatRuleManager.ChatRule activeDropdownRule = null;
    private int dropdownX = 0;
    private int dropdownY = 0;
    private int dropdownWidth = 125;

    private final List<RuleUI> uiElements = new ArrayList<>();

    private ButtonWidget addBtn;

    public ChatRuleSettingsScreen(Screen parent) {
        super(Text.literal("Chat Rules Settings"));
        this.parent = parent;
    }

    private static class RuleUI {
        ChatRuleManager.ChatRule rule;
        TextFieldWidget triggerField;
        ButtonWidget actionBtn;
        TextFieldWidget argField;
        ButtonWidget deleteBtn;
    }

    @Override
    protected void init() {
        uiElements.clear();
        lastRenderTime = System.currentTimeMillis();

        int mainWidth = this.width - SIDEBAR_WIDTH - 20;
        int usableWidth = mainWidth - 30;

        addBtn = ButtonWidget.builder(Text.literal("Add New Rule"), btn -> {
            ChatRuleManager.rules.add(new ChatRuleManager.ChatRule());
            ChatRuleManager.save();
            this.init(this.width, this.height);
        }).dimensions(-1000, 0, usableWidth, 20).build();
        this.addDrawableChild(addBtn);

        for (ChatRuleManager.ChatRule r : ChatRuleManager.rules) {
            RuleUI ui = new RuleUI();
            ui.rule = r;

            ui.triggerField = new TextFieldWidget(textRenderer, -1000, 0, 200, 16, Text.literal(""));
            ui.triggerField.setMaxLength(256);
            ui.triggerField.setText(r.triggerMessage);
            ui.triggerField.setChangedListener(val -> {
                r.triggerMessage = val;
                ChatRuleManager.save();
            });
            this.addDrawableChild(ui.triggerField);

            ui.actionBtn = ButtonWidget.builder(Text.literal(r.actionType), btn -> {
                activeDropdownRule = r;
            }).dimensions(-1000, 0, 125, 20).build();
            this.addDrawableChild(ui.actionBtn);

            ui.argField = new TextFieldWidget(textRenderer, -1000, 0, 150, 16, Text.literal(""));
            ui.argField.setMaxLength(256);
            ui.argField.setText(r.actionArg);
            ui.argField.setChangedListener(val -> {
                if (r.actionType.equals("Hide Players X seconds") && !val.matches("\\d*")) {
                    String numbersOnly = val.replaceAll("[^\\d]", "");
                    ui.argField.setText(numbersOnly);
                    r.actionArg = numbersOnly;
                } else {
                    r.actionArg = val;
                }
                ChatRuleManager.save();
            });
            this.addDrawableChild(ui.argField);

            ui.deleteBtn = ButtonWidget.builder(Text.literal("§cDelete"), btn -> {
                ChatRuleManager.rules.remove(r);
                ChatRuleManager.save();
                this.init(this.width, this.height);
            }).dimensions(-1000, 0, 60, 42).build();
            this.addDrawableChild(ui.deleteBtn);

            uiElements.add(ui);
        }

        contentHeight = 55 + (ChatRuleManager.rules.size() * ROW_HEIGHT);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (activeDropdownRule != null) return true;
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
    public boolean mouseClicked(Click click, boolean doubled) {
        MinecraftClient client = MinecraftClient.getInstance();

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;
        int mainHeight = this.height - 70;

        if (activeDropdownRule != null) {
            int listHeight = ACTION_TYPES.length * 14;
            if (click.x() >= dropdownX && click.x() <= dropdownX + dropdownWidth && click.y() >= dropdownY && click.y() <= dropdownY + listHeight) {
                int clickedIndex = (int) ((click.y() - dropdownY) / 14);
                if (clickedIndex >= 0 && clickedIndex < ACTION_TYPES.length) {
                    activeDropdownRule.actionType = ACTION_TYPES[clickedIndex];

                    if (activeDropdownRule.actionType.equals("Hide Players X seconds") && !activeDropdownRule.actionArg.matches("\\d*")) {
                        activeDropdownRule.actionArg = "";
                    }

                    client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    ChatRuleManager.save();
                    this.init(this.width, this.height);
                }
                activeDropdownRule = null;
                return true;
            }
            activeDropdownRule = null;
            return true;
        }

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

        if (isBackHovered) context.fill(5, backBtnY, SIDEBAR_WIDTH - 5, backBtnY + 18, 0x40FFFFFF);
        int backTextColor = isBackHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
        context.drawTextWithShadow(this.textRenderer, "◄ Back to Menu", 15, backBtnY + 5, backTextColor);

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;

        context.drawCenteredTextWithShadow(this.textRenderer, "§nChat Rules", mainX + (mainWidth / 2), 15, 0xFFFFFFFF);

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, 0x60000000);
        context.enableScissor(mainX, mainY, mainX + mainWidth, mainY + mainHeight);

        int currentY = mainY + 10 - (int)scrollOffsetY;
        int elementX = mainX + 15;

        if (currentY + 20 > mainY && currentY < mainY + mainHeight) {
            addBtn.setX(elementX);
            addBtn.setY(currentY);
            addBtn.visible = true;
        } else {
            addBtn.visible = false;
        }
        currentY += 35;

        for (RuleUI ui : uiElements) {
            if (currentY + 50 > mainY && currentY < mainY + mainHeight) {
                int fieldWidth = mainWidth - 95;

                ui.triggerField.setX(elementX);
                ui.triggerField.setY(currentY);
                ui.triggerField.setWidth(fieldWidth);
                ui.triggerField.visible = true;

                ui.deleteBtn.setX(elementX + fieldWidth + 5);
                ui.deleteBtn.setY(currentY);
                ui.deleteBtn.visible = true;

                ui.actionBtn.setX(elementX);
                ui.actionBtn.setY(currentY + 22);
                ui.actionBtn.visible = true;

                if (activeDropdownRule == ui.rule) {
                    dropdownX = elementX;
                    dropdownY = currentY + 42;
                }

                ui.argField.setX(elementX + 130);
                ui.argField.setY(currentY + 24);
                ui.argField.setWidth(fieldWidth - 130);
                ui.argField.visible = true;

                if (ui.triggerField.getText().isEmpty() && !ui.triggerField.isFocused()) {
                    context.drawText(this.textRenderer, "Trigger Message / Regex", elementX + 4, currentY + 4, 0x777777, false);
                }
                if (ui.argField.getText().isEmpty() && !ui.argField.isFocused()) {
                    String argHint = "Args / Command (e.g. &cAlert)";
                    if (ui.rule.actionType.equals("Hide Players X seconds")) argHint = "Seconds (Int)";

                    context.drawText(this.textRenderer, argHint, elementX + 134, currentY + 28, 0x777777, false);
                }

            } else {
                ui.triggerField.visible = false; ui.deleteBtn.visible = false;
                ui.actionBtn.visible = false; ui.argField.visible = false;
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

        if (activeDropdownRule != null) {
            int listHeight = ACTION_TYPES.length * 14;
            context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + listHeight, 0xFF111111);

            int drawY = dropdownY;
            for (String act : ACTION_TYPES) {
                boolean isHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth && mouseY >= drawY && mouseY < drawY + 14;
                if (isHovered) context.fill(dropdownX + 1, drawY + 1, dropdownX + dropdownWidth - 1, drawY + 13, 0xFF44AAFF);
                context.drawCenteredTextWithShadow(this.textRenderer, act, dropdownX + (dropdownWidth / 2), drawY + 3, isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                drawY += 14;
            }
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}