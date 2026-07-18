package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import net.otsutsukimiho.nozomiaddon.NozomiAddonClient;
import net.otsutsukimiho.nozomiaddon.config.*;
import net.otsutsukimiho.nozomiaddon.features.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMenuList extends Screen {
    private static final String[] CATEGORIES = {
            "General", "Dungeon", "Highlight", "HUD", "Mining", "Performance", "Other"
    };

    private static final int SIDEBAR_WIDTH = 120;
    private static final int ITEM_HEIGHT = 14;
    private static String selectedCategory = "General";
    private static double scrollOffsetY = 0;
    private static double targetScrollOffsetY = 0;
    private static float sidebarSelectionAnimY = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private final Map<String, FeatureManager.Feature> registered = FeatureManager.getRegistered();
    private final Map<String, List<String>> categorizedFeatures = new HashMap<>();
    private final Map<String, Float> itemHoverAnimations = new HashMap<>();
    private long lastRenderTime = 0;

    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;

    private TextFieldWidget searchBox;
    private String searchText = "";

    public ModMenuList() {
        super(Text.literal("§d§lNozomiAddon §bv" + NozomiAddonClient.MOD_VERSION));
    }

    @Override
    protected void init() {
        sortFeatures();
        lastRenderTime = System.currentTimeMillis();

        int searchWidth = 200;
        searchBox = new TextFieldWidget(
                this.textRenderer,
                SIDEBAR_WIDTH + 10,
                this.height - 25,
                searchWidth,
                18,
                Text.literal(" ")
        );
        searchBox.setChangedListener(text -> {
            this.searchText = text.toLowerCase();
            targetScrollOffsetY = 0;
        });
        this.addDrawableChild(searchBox);

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Open HUD Editor"), btn ->
                                this.client.setScreen(new EditHudScreen(this)))
                        .dimensions(this.width - 110, this.height - 26, 105, 20)
                        .build()
        );

        if (lastWidth != this.width || lastHeight != this.height) {
            scrollOffsetY = 0;
            targetScrollOffsetY = 0;
            lastWidth = this.width;
            lastHeight = this.height;
        } else {
            clampScroll();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double amount = verticalAmount != 0 ? verticalAmount : horizontalAmount;

        if (amount != 0) {
            targetScrollOffsetY -= amount * 35;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void clampScroll() {
        int visibleCount = getDisplayItems().size();
        int contentHeight = visibleCount * ITEM_HEIGHT;
        int mainHeight = this.height - 70;
        int maxScroll = Math.max(0, contentHeight - mainHeight);

        if (targetScrollOffsetY < 0) targetScrollOffsetY = 0;
        if (targetScrollOffsetY > maxScroll) targetScrollOffsetY = maxScroll;
    }

    private void sortFeatures() {
        categorizedFeatures.clear();
        for (String cat : CATEGORIES) {
            categorizedFeatures.put(cat, new ArrayList<>());
        }
        categorizedFeatures.putIfAbsent("Other", new ArrayList<>());

        for (String fullKey : registered.keySet()) {
            String category = "Other";

            if (fullKey.contains(" - ")) {
                String[] parts = fullKey.split(" - ", 2);
                category = parts[0];
            }

            if (categorizedFeatures.containsKey(category)) {
                categorizedFeatures.get(category).add(fullKey);
            } else {
                categorizedFeatures.get("Other").add(fullKey);
            }
        }
    }

    private List<String> getDisplayItems() {
        List<String> items = new ArrayList<>();
        if (!searchText.isEmpty()) {
            for (List<String> catList : categorizedFeatures.values()) {
                for (String fullKey : catList) {
                    String displayKey = fullKey.contains(" - ") ? fullKey.split(" - ", 2)[1] : fullKey;
                    if (displayKey.toLowerCase().contains(searchText)) {
                        items.add(fullKey);
                    }
                }
            }
        } else {
            items.addAll(categorizedFeatures.getOrDefault(selectedCategory, new ArrayList<>()));
        }
        return items;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastRenderTime == 0 ? 0.016f : (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        int mainHeight = this.height - 70;
        int contentHeight = getDisplayItems().size() * ITEM_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - mainHeight);

        if (isDraggingScrollbar) {
            if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
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
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, SIDEBAR_WIDTH / 2, 10, 0xFFFFFFFF);
        context.fill(10, 25, SIDEBAR_WIDTH - 10, 26, 0x55FFFFFF);

        int catY = 35;
        int targetSelectionY = 35;

        for (int i = 0; i < CATEGORIES.length; i++) {
            String category = CATEGORIES[i];
            if (category.equals(selectedCategory)) {
                targetSelectionY = catY;
            }
            catY += 18;
        }

        if (sidebarSelectionAnimY == -1) sidebarSelectionAnimY = targetSelectionY;
        sidebarSelectionAnimY += (targetSelectionY - sidebarSelectionAnimY) * 15f * deltaTime;

        if (searchText.isEmpty()) {
            context.fill(5, (int)sidebarSelectionAnimY, SIDEBAR_WIDTH - 5, (int)sidebarSelectionAnimY + 18, ClickGUI.headerColor.getRGB());
        }
        catY = 35;
        for (String category : CATEGORIES) {
            boolean isHovered = mouseX >= 5 && mouseX <= SIDEBAR_WIDTH - 5 && mouseY >= catY && mouseY < catY + 20;
            boolean isSelected = category.equals(selectedCategory) && searchText.isEmpty();
            int color = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFAAAAAA : 0xFF777777);
            context.drawTextWithShadow(this.textRenderer, category, 15, catY + 5, color);
            catY += 18;
        }

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, 0x60000000);

        List<String> itemsToDisplay = getDisplayItems();

        List<Text> tooltipToRender = null;

        context.enableScissor(mainX, mainY, mainX + mainWidth, mainY + mainHeight);

        int currentY = mainY + 5 - (int)scrollOffsetY;

        for (String fullKey : itemsToDisplay) {
            if (currentY + ITEM_HEIGHT < mainY || currentY > mainY + mainHeight) {
                currentY += ITEM_HEIGHT;
                continue;
            }

            String displayKey = fullKey.contains(" - ") ? fullKey.split(" - ", 2)[1] : fullKey;
            FeatureManager.Feature f = registered.get(fullKey);
            boolean isFeatureEnabled = f.isEnabled();
            boolean hasSettings = !f.getSettings().isEmpty() || displayKey.equals("CommandKeyBind") || fullKey.contains("CommandKeyBind") || displayKey.equals("ChatRules") || fullKey.contains("ChatRules");

            boolean isHovered = mouseX >= mainX && mouseX <= mainX + mainWidth && mouseY >= currentY && mouseY < currentY + ITEM_HEIGHT;

            if (isHovered) {
                tooltipToRender = new ArrayList<>();
                tooltipToRender.add(Text.literal("§b§l" + displayKey));

                String desc = f.getDescription();
                if (desc != null && !desc.isEmpty()) {
                    for (String line : desc.split("\n")) {
                        tooltipToRender.add(Text.literal("§7" + line));
                    }
                }

                tooltipToRender.add(Text.literal("§r"));
                tooltipToRender.add(Text.literal("§e§lLeft Click §ato toggle"));
                tooltipToRender.add(Text.literal("§e§lRIGHT CLICK §ato open settings"));
            }

            float currentItemHover = itemHoverAnimations.getOrDefault(fullKey, 0.0f);
            float targetItemHover = isHovered ? 1.0f : 0.0f;
            if (currentItemHover != targetItemHover) {
                currentItemHover += (targetItemHover - currentItemHover) * 18f * deltaTime;
                if (Math.abs(currentItemHover - targetItemHover) < 0.01f) currentItemHover = targetItemHover;
                itemHoverAnimations.put(fullKey, currentItemHover);
            }

            if (currentItemHover > 0.01f) {
                int hoverAlpha = (int) (currentItemHover * 40);
                int hoverBgColor = (hoverAlpha << 24) | 0xFFFFFF;
                context.fill(mainX, currentY, mainX + mainWidth, currentY + ITEM_HEIGHT, hoverBgColor);
            }

            int textOffsetX = isHovered ? 2 : 0;
            int color = isFeatureEnabled ? 0xFFFFFFFF : 0xFF555555;
            if (isHovered && !isFeatureEnabled) color = 0xFF777777;

            if (isFeatureEnabled) {
                context.fill(mainX + 4, currentY + 2, mainX + 5, currentY + 12, 0xFFFFFFFF);
            }

            String textPrefix = hasSettings ? "☰ " : "";
            context.drawText(this.textRenderer, Text.literal(textPrefix + displayKey), mainX + 12 + textOffsetX, currentY + 3, color, true);

            currentY += ITEM_HEIGHT;
        }
        context.disableScissor();

        if (tooltipToRender != null) {
            context.drawTooltip(this.textRenderer, tooltipToRender, mouseX, mouseY);
        }

        if (contentHeight > mainHeight) {
            int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
            int scrollbarY = mainY + (int) ((scrollOffsetY / maxScroll) * (mainHeight - scrollbarHeight));

            context.fill(mainX + mainWidth - 4, mainY, mainX + mainWidth, mainY + mainHeight, 0x40000000);

            int scrollColor = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.fill(mainX + mainWidth - 4, scrollbarY, mainX + mainWidth, scrollbarY + scrollbarHeight, scrollColor);
        }

        Text rightClickText = Text.literal("§7Right Click to open settings");
        Text leftClickText = Text.literal("§7Left Click to toggle");
        context.drawText(this.textRenderer, leftClickText, this.width - this.textRenderer.getWidth(leftClickText) - 5, 5, 0xFFAAAAAA, true);
        context.drawText(this.textRenderer, rightClickText, this.width - this.textRenderer.getWidth(rightClickText) - 5, 15, 0xFFAAAAAA, true);

        context.drawText(this.textRenderer, Text.literal("§7Search:"), SIDEBAR_WIDTH + 10, this.height - 35, 0xFFAAAAAA, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(Click click) {
        isDraggingScrollbar = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;
        int mainHeight = this.height - 70;

        int contentHeight = getDisplayItems().size() * ITEM_HEIGHT;
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

        if (super.mouseClicked(click, doubled)) return true;

        if (click.x() >= 5 && click.x() <= SIDEBAR_WIDTH - 5) {
            int catY = 35;
            for (String category : CATEGORIES) {
                if (click.y() >= catY && click.y() < catY + 18) {
                    if (!selectedCategory.equals(category)) {
                        selectedCategory = category;
                        targetScrollOffsetY = 0;
                        searchText = "";
                        searchBox.setText("");

                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                    }
                    return true;
                }
                catY += 18;
            }
        }

        if (click.x() >= mainX && click.x() <= mainX + mainWidth && click.y() >= mainY && click.y() <= mainY + mainHeight) {
            List<String> itemsToDisplay = getDisplayItems();
            double relativeY = click.y() - (mainY + 5) + scrollOffsetY;
            int clickedIndex = (int) (relativeY / ITEM_HEIGHT);

            if (relativeY >= 0 && clickedIndex >= 0 && clickedIndex < itemsToDisplay.size()) {
                String fullKey = itemsToDisplay.get(clickedIndex);
                String displayKey = fullKey.contains(" - ") ? fullKey.split(" - ", 2)[1] : fullKey;
                FeatureManager.Feature feature = registered.get(fullKey);
                MinecraftClient client = MinecraftClient.getInstance();

                if (click.button() == 0) {
                    toggleFeature(fullKey);
                    if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                    return true;
                }
                else if (click.button() == 1) {
                    if (displayKey.equals("CommandKeyBind") || fullKey.contains("CommandKeyBind")) {
                        client.setScreen(new MacroSettingsScreen(this));
                        if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                        return true;
                    } else if (displayKey.equals("ChatRules") || fullKey.contains("ChatRules")) {
                        client.setScreen(new ChatRuleSettingsScreen(this));
                        if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                        return true;
                    } else if (!feature.getSettings().isEmpty()) {
                        client.setScreen(new FeatureSettingsScreen(this, displayKey, feature));
                        if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void toggleFeature(String fullKey) {
        boolean newState = !ConfigManager.get(fullKey);
        ConfigManager.set(fullKey, newState);
        FeatureManager.setEnabled(fullKey, newState);
    }
}