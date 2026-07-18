package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.otsutsukimiho.nozomiaddon.NozomiAddonClient;
import net.otsutsukimiho.nozomiaddon.config.*;
import net.otsutsukimiho.nozomiaddon.features.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMenuGrid extends Screen {
    private static final String[] CATEGORIES = {
            "General", "Dungeon", "Highlight", "HUD", "Mining", "Performance", "Other"
    };

    private static final int SIDEBAR_WIDTH = 120;

    private static final int CARD_HEIGHT = 100;
    private static final int SPACING = 15;
    private static final int TARGET_CARD_WIDTH = 120;

    private static String selectedCategory = "General";
    private static double scrollOffsetY = 0;
    private static double targetScrollOffsetY = 0;
    private static float sidebarSelectionAnimY = -1;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private final Map<String, FeatureManager.Feature> registered = FeatureManager.getRegistered();
    private final Map<String, List<String>> categorizedFeatures = new HashMap<>();
    private final Map<String, Float> itemHoverAnimations = new HashMap<>();
    private final Map<String, Float> itemToggleAnimations = new HashMap<>();

    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;

    private long lastRenderTime = 0;
    private int contentHeight = 0;

    private int currentColumns = 1;
    private int currentCardWidth = TARGET_CARD_WIDTH;

    private EditBox searchBox;
    private String searchText = "";

    public ModMenuGrid() {
        super(Component.literal("§d§lNozomiAddon §bv" + NozomiAddonClient.MOD_VERSION));
    }

    @Override
    protected void init() {
        sortFeatures();
        lastRenderTime = System.currentTimeMillis();

        int searchWidth = 200;
        searchBox = new EditBox(
                this.font,
                SIDEBAR_WIDTH + 10,
                this.height - 25,
                searchWidth,
                18,
                Component.literal(" ")
        );
        searchBox.setResponder(text -> {
            this.searchText = text.toLowerCase();
            targetScrollOffsetY = 0;
        });
        this.addRenderableWidget(searchBox);

        this.addRenderableWidget(
                Button.builder(Component.literal("Open HUD Editor"), btn ->
                                this.minecraft.gui.setScreen(new EditHudScreen(this)))
                        .bounds(this.width - 110, this.height - 26, 105, 20)
                        .build()
        );

        updateLayout();

        if (lastWidth != this.width || lastHeight != this.height) {
            scrollOffsetY = 0;
            targetScrollOffsetY = 0;
            lastWidth = this.width;
            lastHeight = this.height;
        } else {
            clampScroll();
        }
    }

    private void updateLayout() {
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;

        currentColumns = Math.max(1, (mainWidth - SPACING) / (TARGET_CARD_WIDTH + SPACING));
        currentColumns = Math.min(currentColumns, 5);
        currentCardWidth = (mainWidth - (SPACING * (currentColumns + 1))) / currentColumns;

        int rows = (int) Math.ceil((double) getDisplayItems().size() / currentColumns);
        contentHeight = rows * (CARD_HEIGHT + SPACING) + SPACING;
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
        updateLayout();
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

    private int interpolateColorARGB(int color1, int color2, float fraction) {
        int a = (int) (((color1 >> 24) & 0xff) + (((color2 >> 24) & 0xff) - ((color1 >> 24) & 0xff)) * fraction);
        int r = (int) (((color1 >> 16) & 0xff) + (((color2 >> 16) & 0xff) - ((color1 >> 16) & 0xff)) * fraction);
        int g = (int) (((color1 >> 8) & 0xff) + (((color2 >> 8) & 0xff) - ((color1 >> 8) & 0xff)) * fraction);
        int b = (int) ((color1 & 0xff) + ((color2 & 0xff) - (color1 & 0xff)) * fraction);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int interpolateColorRGB(int color1, int color2, float fraction) {
        int r = (int) (((color1 >> 16) & 0xff) + (((color2 >> 16) & 0xff) - ((color1 >> 16) & 0xff)) * fraction);
        int g = (int) (((color1 >> 8) & 0xff) + (((color2 >> 8) & 0xff) - ((color1 >> 8) & 0xff)) * fraction);
        int b = (int) ((color1 & 0xff) + ((color2 & 0xff) - (color1 & 0xff)) * fraction);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastRenderTime == 0 ? 0.016f : (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        updateLayout();

        int mainHeight = this.height - 70;
        int maxScroll = Math.max(0, contentHeight - mainHeight);

        if (isDraggingScrollbar) {
            if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                isDraggingScrollbar = false;
            } else {
                int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
                float scrollFraction = (float) (mouseY - scrollbarDragOffsetY - 30) / (mainHeight - scrollbarHeight);
                scrollFraction = Mth.clamp(scrollFraction, 0.0f, 1.0f);
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
        context.centeredText(this.font, this.title, SIDEBAR_WIDTH / 2, 10, 0xFFFFFFFF);
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
            context.text(this.font, category, 15, catY + 5, color);
            catY += 18;
        }

        int mainX = SIDEBAR_WIDTH + 10;
        int mainY = 30;
        int mainWidth = this.width - SIDEBAR_WIDTH - 20;

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, 0x60000000);

        List<String> itemsToDisplay = getDisplayItems();

        List<Component> tooltipToRender = null;

        context.enableScissor(mainX, mainY, mainX + mainWidth, mainY + mainHeight);

        for (int i = 0; i < itemsToDisplay.size(); i++) {
            String fullKey = itemsToDisplay.get(i);

            int col = i % currentColumns;
            int row = i / currentColumns;

            int cardX = mainX + SPACING + col * (currentCardWidth + SPACING);
            int cardY = mainY + SPACING + row * (CARD_HEIGHT + SPACING) - (int)scrollOffsetY;

            if (cardY + CARD_HEIGHT < mainY || cardY > mainY + mainHeight) continue;

            String displayKey = fullKey.contains(" - ") ? fullKey.split(" - ", 2)[1] : fullKey;
            FeatureManager.Feature f = registered.get(fullKey);
            boolean isFeatureEnabled = f.isEnabled();

            boolean hasSettings = !f.getSettings().isEmpty() || displayKey.equals("CommandKeyBind") || fullKey.contains("CommandKeyBind") || displayKey.equals("ChatRules") || fullKey.contains("ChatRules");

            boolean isHovered = mouseX >= cardX && mouseX <= cardX + currentCardWidth && mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT;

            if (isHovered) {
                tooltipToRender = new ArrayList<>();
                tooltipToRender.add(Component.literal("§b§l" + displayKey));

                String desc = f.getDescription();
                if (desc != null && !desc.isEmpty()) {
                    for (String line : desc.split("\n")) {
                        tooltipToRender.add(Component.literal("§7" + line));
                    }
                }

                tooltipToRender.add(Component.literal("§r"));
                tooltipToRender.add(Component.literal("§e§lLeft Click §ato toggle"));
                tooltipToRender.add(Component.literal("§e§lRIGHT CLICK §ato open settings"));
            }

            float currentItemHover = itemHoverAnimations.getOrDefault(fullKey, 0.0f);
            float targetItemHover = isHovered ? 1.0f : 0.0f;
            if (currentItemHover != targetItemHover) {
                currentItemHover += (targetItemHover - currentItemHover) * 15f * deltaTime;
                if (Math.abs(currentItemHover - targetItemHover) < 0.01f) currentItemHover = targetItemHover;
                itemHoverAnimations.put(fullKey, currentItemHover);
            }

            float currentToggle = itemToggleAnimations.getOrDefault(fullKey, isFeatureEnabled ? 1.0f : 0.0f);
            float targetToggle = isFeatureEnabled ? 1.0f : 0.0f;
            if (currentToggle != targetToggle) {
                currentToggle += (targetToggle - currentToggle) * 15f * deltaTime;
                if (Math.abs(currentToggle - targetToggle) < 0.01f) currentToggle = targetToggle;
                itemToggleAnimations.put(fullKey, currentToggle);
            }

            int baseColor = interpolateColorRGB(0x0A0A0A, 0x222222, currentToggle);
            int bgAlpha = 180 + (int)(currentItemHover * 50);
            int bgColor = (bgAlpha << 24) | baseColor;
            context.fill(cardX, cardY, cardX + currentCardWidth, cardY + CARD_HEIGHT, bgColor);

            ItemStack iconStack = f.getIcon();

            context.pose().pushMatrix();
            float scale = 1.5f;
            context.pose().translate(cardX + currentCardWidth / 2.0f, cardY + 20.0f);
            context.pose().scale(scale, scale);
            context.item(iconStack, -8, -8);
            context.pose().popMatrix();

            String shortName = displayKey;
            if (font.width(shortName) > currentCardWidth - 10) {
                shortName = shortName.substring(0, Math.min(shortName.length(), 14)) + "...";
            }
            int titleColor = interpolateColorARGB(0xFFAAAAAA, 0xFFFFFFFF, currentToggle);
            context.centeredText(font, shortName, cardX + currentCardWidth / 2, cardY + 38, titleColor);

            if (hasSettings) {
                int optionsY = cardY + CARD_HEIGHT - 35;
                boolean isOptionsHovered = mouseX >= cardX && mouseX <= cardX + currentCardWidth && mouseY >= optionsY && mouseY <= optionsY + 15;

                int optBgColor = interpolateColorARGB(0xFF151515, 0xFF333333, currentToggle);
                if (isOptionsHovered) {
                    optBgColor = interpolateColorARGB(0xFF2A2A2A, 0xFF4A4A4A, currentToggle);
                }

                int optTextColor = interpolateColorARGB(0xFF666666, 0xFFDDDDDD, currentToggle);
                if (isOptionsHovered) optTextColor = 0xFFFFFFFF;

                context.fill(cardX, optionsY, cardX + currentCardWidth, optionsY + 15, optBgColor);
                context.centeredText(font, "Settings", cardX + currentCardWidth / 2, optionsY + 4, optTextColor);
            }

            int toggleY = cardY + CARD_HEIGHT - 20;
            int toggleBgColor = interpolateColorARGB(0xFF2A2A2A, 0xFFE0E0E0, currentToggle);
            context.fill(cardX, toggleY, cardX + currentCardWidth, toggleY + 20, toggleBgColor);

            if (currentToggle > 0.01f) {
                int enabledAlpha = (int)(currentToggle * 255);
                int enabledColor = (enabledAlpha << 24) | 0x404040;
                int tw = font.width("§lENABLED");
                context.text(font, "§lENABLED", cardX + currentCardWidth / 2 - tw / 2, toggleY + 6, enabledColor, false);
            }
            if (currentToggle < 0.99f) {
                int disabledAlpha = (int)((1.0f - currentToggle) * 255);
                int disabledColor = (disabledAlpha << 24) | 0x777777;
                int tw = font.width("§lDISABLED");
                context.text(font, "§lDISABLED", cardX + currentCardWidth / 2 - tw / 2, toggleY + 6, disabledColor, true);
            }

            if (currentItemHover > 0.05f) {
                context.fill(cardX, cardY, cardX + currentCardWidth, cardY + 1, 0xFF555555);
                context.fill(cardX, cardY + CARD_HEIGHT - 1, cardX + currentCardWidth, cardY + CARD_HEIGHT, 0xFF555555);
                context.fill(cardX, cardY, cardX + 1, cardY + CARD_HEIGHT, 0xFF555555);
                context.fill(cardX + currentCardWidth - 1, cardY, cardX + currentCardWidth, cardY + CARD_HEIGHT, 0xFF555555);
            }
        }
        context.disableScissor();

        if (tooltipToRender != null) {
            context.setComponentTooltipForNextFrame(this.font, tooltipToRender, mouseX, mouseY);
        }

        if (contentHeight > mainHeight) {
            int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
            int scrollbarY = mainY + (int) ((scrollOffsetY / maxScroll) * (mainHeight - scrollbarHeight));

            context.fill(mainX + mainWidth - 4, mainY, mainX + mainWidth, mainY + mainHeight, 0x40000000);

            int scrollColor = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.fill(mainX + mainWidth - 4, scrollbarY, mainX + mainWidth, scrollbarY + scrollbarHeight, scrollColor);
        }

        Component rightClickText = Component.literal("§7Right Click to open settings");
        Component leftClickText = Component.literal("§7Left Click to toggle");
        context.text(this.font, leftClickText, this.width - this.font.width(leftClickText) - 5, 5, 0xFFAAAAAA, true);
        context.text(this.font, rightClickText, this.width - this.font.width(rightClickText) - 5, 15, 0xFFAAAAAA, true);

        context.text(this.font, Component.literal("§7Search:"), SIDEBAR_WIDTH + 10, this.height - 35, 0xFFAAAAAA, true);

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        isDraggingScrollbar = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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

        if (super.mouseClicked(click, doubled)) return true;

        if (click.x() >= 5 && click.x() <= SIDEBAR_WIDTH - 5) {
            int catY = 35;
            for (String category : CATEGORIES) {
                if (click.y() >= catY && click.y() < catY + 18) {
                    if (!selectedCategory.equals(category)) {
                        selectedCategory = category;
                        targetScrollOffsetY = 0;
                        searchText = "";
                        searchBox.setValue("");

                        Minecraft client = Minecraft.getInstance();
                        if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                    }
                    return true;
                }
                catY += 18;
            }
        }

        if (click.x() >= mainX && click.x() <= mainX + mainWidth && click.y() >= mainY && click.y() <= mainY + mainHeight) {
            List<String> itemsToDisplay = getDisplayItems();

            int relX = (int)click.x() - mainX - SPACING;
            int relY = (int)click.y() - mainY - SPACING + (int)scrollOffsetY;

            if (relX < 0 || relY < 0) return false;

            int col = relX / (currentCardWidth + SPACING);
            int row = relY / (CARD_HEIGHT + SPACING);

            int inCardX = relX % (currentCardWidth + SPACING);
            int inCardY = relY % (CARD_HEIGHT + SPACING);

            if (inCardX <= currentCardWidth && inCardY <= CARD_HEIGHT && col < currentColumns) {
                int clickedIndex = row * currentColumns + col;

                if (clickedIndex >= 0 && clickedIndex < itemsToDisplay.size()) {
                    String fullKey = itemsToDisplay.get(clickedIndex);
                    String displayKey = fullKey.contains(" - ") ? fullKey.split(" - ", 2)[1] : fullKey;
                    FeatureManager.Feature feature = registered.get(fullKey);
                    Minecraft client = Minecraft.getInstance();

                    boolean hasSettings = !feature.getSettings().isEmpty() || displayKey.equals("CommandKeyBind") || fullKey.contains("CommandKeyBind")
                            || displayKey.equals("ChatRules") || fullKey.contains("ChatRules");

                    boolean clickedOptions = inCardY >= CARD_HEIGHT - 35 && inCardY <= CARD_HEIGHT - 20;

                    if (click.button() == 1 || (click.button() == 0 && clickedOptions && hasSettings)) {

                        if (displayKey.equals("CommandKeyBind") || fullKey.contains("CommandKeyBind")) {
                            client.gui.setScreen(new MacroSettingsScreen(this));
                            if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                            return true;
                        }
                        else if (displayKey.equals("ChatRules") || fullKey.contains("ChatRules")) {
                            client.gui.setScreen(new ChatRuleSettingsScreen(this));
                            if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                            return true;
                        }
                        else if (!feature.getSettings().isEmpty()) {
                            client.gui.setScreen(new FeatureSettingsScreen(this, displayKey, feature));
                            if (client.player != null) client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
                            return true;
                        }
                    }

                    else if (click.button() == 0) {
                        toggleFeature(fullKey);
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