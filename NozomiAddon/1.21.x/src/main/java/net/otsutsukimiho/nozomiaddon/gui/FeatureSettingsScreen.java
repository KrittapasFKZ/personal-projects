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
import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureSettingsScreen extends Screen {

    private final Screen parent;
    private final String featureName;
    private final FeatureManager.Feature feature;

    private static final int SIDEBAR_WIDTH = 120;
    private static final int WIDGET_WIDTH = 100;
    private static final int FIELD_WIDTH = 35;

    private double scrollOffsetY = 0;
    private double targetScrollOffsetY = 0;
    private int contentHeight = 0;
    private long lastRenderTime = 0;

    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;

    private boolean isDraggingDropdownScrollbar = false;
    private double dropdownScrollbarDragOffsetY = 0;

    private boolean isDraggingMultiScrollbar = false;
    private double multiScrollbarDragOffsetY = 0;
    private int multiScrollIndex = 0;

    private NumberSetting draggingSetting = null;
    private ColorSetting draggingColor = null;
    private String draggingColorComponent = "";
    private FloatSetting draggingFloat = null;
    private KeySetting bindingKey = null;
    private RangeSetting draggingRangeSetting = null;
    private boolean draggingRangeIsMax = false;

    private SoundSetting activeDropdown = null;
    private MultiSelectSetting activeMultiDropdown = null;
    private ModeSetting activeModeDropdown = null;
    private SoundSetting draggingSoundVol = null;
    private SoundSetting draggingSoundPitch = null;
    private String soundSearchQuery = "";
    private String multiSearchQuery = "";

    private int dropdownScrollIndex = 0;
    private int dropdownX = 0, dropdownY = 0, dropdownWidth = 200;

    private final Map<NumberSetting, TextFieldWidget> numberFields = new HashMap<>();
    private final Map<ColorSetting, TextFieldWidget[]> colorFields = new HashMap<>();
    private final Map<StringSetting, TextFieldWidget> stringFields = new HashMap<>();
    private final Map<FloatSetting, TextFieldWidget> floatFields = new HashMap<>();
    private final Map<BooleanSetting, ButtonWidget> booleanButtons = new HashMap<>();
    private final Map<ModeSetting, ButtonWidget> modeButtons = new HashMap<>();
    private final Map<KeySetting, ButtonWidget> keyButtons = new HashMap<>();
    private final Map<RangeSetting, TextFieldWidget[]> rangeFields = new HashMap<>();
    private final Map<TextSetting, TextFieldWidget> textFields = new HashMap<>();
    private final Map<TagSetting, TextFieldWidget> tagFields = new HashMap<>();

    public FeatureSettingsScreen(Screen parent, String featureName, FeatureManager.Feature feature) {
        super(Text.literal("Settings: " + featureName));
        this.parent = parent;
        this.featureName = featureName;
        this.feature = feature;
    }

    @Override
    protected void init() {
        numberFields.clear();
        colorFields.clear();
        stringFields.clear();
        floatFields.clear();
        booleanButtons.clear();
        modeButtons.clear();
        rangeFields.clear();
        textFields.clear();
        tagFields.clear();
        activeMultiDropdown = null;

        lastRenderTime = System.currentTimeMillis();
        scrollOffsetY = 0;
        targetScrollOffsetY = 0;

        int dummyX = -1000;

        for (Settings setting : feature.getSettings()) {
            if (!setting.isVisible()) continue;

            if (setting instanceof BooleanSetting bool) {
                ButtonWidget btn = ButtonWidget.builder(
                        Text.literal(bool.isEnabled() ? "§aEnabled" : "§cDisabled"),
                        b -> {
                            bool.toggle();
                            b.setMessage(Text.literal(bool.isEnabled() ? "§aEnabled" : "§cDisabled"));
                            ConfigManager.save();
                        }
                ).dimensions(dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 20).build();
                this.addDrawableChild(btn);
                booleanButtons.put(bool, btn);
            }
            else if (setting instanceof StringSetting str) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 16, Text.literal(""));
                field.setMaxLength(256);
                field.setText(str.getValue());
                field.setChangedListener(val -> {
                    str.setValue(val);
                    ConfigManager.save();
                });
                this.addDrawableChild(field);
                stringFields.put(str, field);
            }
            else if (setting instanceof TextSetting textSetting) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 16, Text.literal(""));
                field.setMaxLength(256);
                field.setText(textSetting.getValue());
                field.setChangedListener(val -> {
                    textSetting.setValue(val);
                    ConfigManager.save();
                });
                this.addDrawableChild(field);
                textFields.put(textSetting, field);
            }
            else if (setting instanceof ModeSetting mode) {
                ButtonWidget btn = ButtonWidget.builder(
                        Text.literal(mode.getMode()),
                        b -> {
                            mode.cycle();
                            b.setMessage(Text.literal(mode.getMode()));
                            ConfigManager.save();
                        }
                ).dimensions(dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 20).build();
                this.addDrawableChild(btn);
                modeButtons.put(mode, btn);
            }
            else if (setting instanceof TagSetting tagSetting) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 16, Text.literal(""));
                field.setMaxLength(512);
                field.setText(tagSetting.getTagsAsString());
                field.setChangedListener(val -> {
                    tagSetting.setTagsFromString(val);
                    ConfigManager.save();
                });
                this.addDrawableChild(field);
                tagFields.put(tagSetting, field);
            }
            else if (setting instanceof ColorSetting color) {
                Color c = new Color(color.getRGB(), true);
                TextFieldWidget r = createColorField(dummyX, 0, color, "R");
                TextFieldWidget g = createColorField(dummyX, 0, color, "G");
                TextFieldWidget b = createColorField(dummyX, 0, color, "B");

                r.setText(String.valueOf(c.getRed()));
                g.setText(String.valueOf(c.getGreen()));
                b.setText(String.valueOf(c.getBlue()));

                colorFields.put(color, new TextFieldWidget[]{r, g, b});
            }
            else if (setting instanceof NumberSetting num) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, dummyX, 0, FIELD_WIDTH, 14, Text.literal(""));
                field.setText(String.valueOf(num.getValue()));
                field.setChangedListener(val -> {
                    if (val.isEmpty()) return;
                    try {
                        num.setValue(Integer.parseInt(val));
                        ConfigManager.save();
                    } catch (NumberFormatException ignored) {}
                });
                this.addDrawableChild(field);
                numberFields.put(num, field);
            }
            else if (setting instanceof FloatSetting flt) {
                TextFieldWidget field = new TextFieldWidget(textRenderer, dummyX, 0, FIELD_WIDTH, 14, Text.literal(""));
                field.setText(String.format("%.2f", flt.getValue()));
                field.setChangedListener(val -> {
                    if (val.isEmpty()) return;
                    try {
                        flt.setValue(Float.parseFloat(val));
                        ConfigManager.save();
                    } catch (NumberFormatException ignored) {}
                });
                this.addDrawableChild(field);
                floatFields.put(flt, field);
            }
            else if (setting instanceof KeySetting key) {
                String btnText = key.getKeyName();
                ButtonWidget btn = ButtonWidget.builder(Text.literal(btnText), b -> {
                    bindingKey = key;
                    b.setMessage(Text.literal("..."));
                }).dimensions(dummyX, 0, WIDGET_WIDTH + FIELD_WIDTH + 5, 20).build();

                this.addDrawableChild(btn);
                keyButtons.put(key, btn);
            }
            else if (setting instanceof RangeSetting rs) {
                TextFieldWidget minField = new TextFieldWidget(textRenderer, dummyX, 0, 30, 14, Text.literal(""));
                minField.setText(String.valueOf(rs.getMin()));
                minField.setChangedListener(val -> {
                    if (val.isEmpty()) return;
                    try { rs.setMin(Integer.parseInt(val)); ConfigManager.save(); } catch (NumberFormatException ignored) {}
                });

                TextFieldWidget maxField = new TextFieldWidget(textRenderer, dummyX, 0, 30, 14, Text.literal(""));
                maxField.setText(String.valueOf(rs.getMax()));
                maxField.setChangedListener(val -> {
                    if (val.isEmpty()) return;
                    try { rs.setMax(Integer.parseInt(val)); ConfigManager.save(); } catch (NumberFormatException ignored) {}
                });

                this.addDrawableChild(minField);
                this.addDrawableChild(maxField);
                rangeFields.put(rs, new TextFieldWidget[]{minField, maxField});
            }
        }
        calculateContentHeight();
    }

    private void calculateContentHeight() {
        int h = 0;
        for (Settings s : feature.getSettings()) {
            if (!s.isVisible()) continue;
            if (s instanceof ColorSetting) h += 85;
            else if (s instanceof SoundSetting) h += 70;
            else if (s instanceof TextSetting) h += 45;
            else if (s instanceof TagSetting) h += 45;
            else if (s instanceof MultiSelectSetting) h += 25;
            else h += 25;
        }
        this.contentHeight = h + 10;
    }

    private TextFieldWidget createColorField(int x, int y, ColorSetting setting, String type) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, 35, 14, Text.literal(""));
        field.setMaxLength(3);
        field.setChangedListener(val -> {
            if (val.isEmpty()) return;
            try {
                int i = MathHelper.clamp(Integer.parseInt(val), 0, 255);
                updateColorFromField(setting, type, i);
                ConfigManager.save();
            } catch (NumberFormatException ignored) {}
        });
        this.addDrawableChild(field);
        return field;
    }

    private void updateColorFromField(ColorSetting setting, String type, int value) {
        Color c = new Color(setting.getRGB(), true);
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();

        switch (type) {
            case "R" -> r = value;
            case "G" -> g = value;
            case "B" -> b = value;
        }
        setting.setColor(new Color(r, g, b, 255));
    }

    private List<String> getFilteredSounds() {
        if (soundSearchQuery.isEmpty()) return SoundSetting.ALL_SOUNDS;
        String query = soundSearchQuery.toLowerCase();
        return SoundSetting.ALL_SOUNDS.stream()
                .filter(s -> s.replace("minecraft:", "").replace('.', ' ').replace('_', ' ').toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    private List<String> getFilteredMultiOptions() {
        if (activeMultiDropdown == null) return List.of();

        String query = multiSearchQuery.toLowerCase();
        List<String> selectedOptions = activeMultiDropdown.getSelectedOptions();

        return activeMultiDropdown.getOptions().stream()
                .filter(s -> query.isEmpty() || s.toLowerCase().contains(query))
                .sorted((a, b) -> {
                    boolean aSelected = selectedOptions.contains(a);
                    boolean bSelected = selectedOptions.contains(b);
                    if (aSelected != bSelected) {
                        return aSelected ? -1 : 1;
                    }
                    return a.compareToIgnoreCase(b);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (activeDropdown != null) {
            if (verticalAmount > 0) dropdownScrollIndex = Math.max(0, dropdownScrollIndex - 4);
            if (verticalAmount < 0) {
                int maxScroll = Math.max(0, getFilteredSounds().size() - 10);
                dropdownScrollIndex = Math.min(maxScroll, dropdownScrollIndex + 4);
            }
            return true;
        }

        if (activeMultiDropdown != null) {
            if (verticalAmount > 0) multiScrollIndex = Math.max(0, multiScrollIndex - 4);
            if (verticalAmount < 0) {
                int visibleItems = 15;
                int maxScroll = Math.max(0, getFilteredMultiOptions().size() - visibleItems);
                multiScrollIndex = Math.min(maxScroll, multiScrollIndex + 4);
            }
            return true;
        }

        double amount = verticalAmount != 0 ? verticalAmount : horizontalAmount;
        if (amount != 0) {
            targetScrollOffsetY -= amount * 35;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastRenderTime == 0 ? 0.016f : (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        int mainHeight = this.height - 70;
        int maxMainScroll = Math.max(0, contentHeight - mainHeight);

        if (isDraggingScrollbar) {
            if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                isDraggingScrollbar = false;
            } else {
                int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
                float scrollFraction = (float) (mouseY - scrollbarDragOffsetY - 30) / (mainHeight - scrollbarHeight);
                scrollFraction = MathHelper.clamp(scrollFraction, 0.0f, 1.0f);
                targetScrollOffsetY = scrollFraction * maxMainScroll;
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

        context.drawCenteredTextWithShadow(this.textRenderer, "§n" + featureName + " Settings", mainX + (mainWidth / 2), 15, 0xFFFFFFFF);

        context.fill(mainX, mainY, mainX + mainWidth, mainY + mainHeight, 0x60000000);
        context.enableScissor(mainX, mainY, mainX + mainWidth, mainY + mainHeight);

        int currentY = mainY + 10 - (int)scrollOffsetY;
        int labelX = mainX + 15;

        int widgetX = mainX + mainWidth - WIDGET_WIDTH - FIELD_WIDTH - 20;

        for (Settings setting : feature.getSettings()) {
            if (!setting.isVisible()) continue;
            int settingHeight = 25;
            if (setting instanceof ColorSetting) settingHeight = 85;
            else if (setting instanceof SoundSetting) settingHeight = 70;
            else if (setting instanceof TextSetting) settingHeight = 45;
            else if (setting instanceof TagSetting) settingHeight = 45;

            if (currentY + settingHeight > mainY && currentY < mainY + mainHeight) {
                context.drawTextWithShadow(textRenderer, setting.name, labelX, currentY + 6, 0xFFFFFFFF);

                if (setting instanceof BooleanSetting bool) {
                    int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    int switchWidth = 24;
                    int knobSize = 12;
                    int trackHeight = 8;
                    int switchY = currentY + 4;
                    int switchX = widgetX + dWidth - switchWidth;
                    int trackY = switchY + (knobSize / 2) - (trackHeight / 2);

                    if (bool.isEnabled()) {
                        context.fill(switchX, trackY, switchX + switchWidth, trackY + trackHeight, 0xFFDDDDDD);
                        context.fill(switchX + switchWidth - knobSize, switchY, switchX + switchWidth, switchY + knobSize, 0xFFFFFFFF);
                    } else {
                        context.fill(switchX, trackY, switchX + switchWidth, trackY + trackHeight, 0xFF444444);
                        context.fill(switchX, switchY, switchX + knobSize, switchY + knobSize, 0xFF666666);
                    }
                }
                else if (setting instanceof TagSetting tagSetting) {
                    TextFieldWidget field = tagFields.get(tagSetting);
                    if (field != null) {
                        field.setX(widgetX); field.setY(currentY + 2); field.visible = true;
                    }
                    int pillX = widgetX; int pillY = currentY + 24;
                    int maxWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    for (String tag : tagSetting.getTags()) {
                        int tagWidth = textRenderer.getWidth(tag) + 8;
                        if (pillX + tagWidth > widgetX + maxWidth) { pillX = widgetX; pillY += 14; }
                        context.fill(pillX, pillY, pillX + tagWidth, pillY + 12, 0xFF222255);
                        context.drawText(textRenderer, tag, pillX + 4, pillY + 2, 0xFFFFFFFF, false);
                        pillX += tagWidth + 4;
                    }
                }
                else if (setting instanceof MultiSelectSetting multi) {
                    int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    context.fill(widgetX, currentY, widgetX + dWidth, currentY + 15, 0xFF333333);
                    context.drawCenteredTextWithShadow(textRenderer, "Select (" + multi.getSelectedOptions().size() + ")", widgetX + (dWidth / 2), currentY + 4, 0xFFFFFFFF);
                    context.drawTextWithShadow(textRenderer, "v", widgetX + dWidth - 10, currentY + 4, 0xFFAAAAAA);
                    if (activeMultiDropdown == multi) { dropdownX = widgetX; dropdownY = currentY + 15; }
                }
                else if (setting instanceof CheckMarkSetting check) {
                    int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    int boxSize = 12; int boxY = currentY + 4;
                    int boxX = widgetX + dWidth - boxSize - 2;

                    context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF555555);
                    String textStr = check.isEnabled() ? "Enabled" : "Disabled";
                    int textColor = check.isEnabled() ? 0xFF55FF55 : 0xFFAAAAAA;

                    if (check.isEnabled()) {
                        context.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, 0xFF22CC22);
                        context.drawTextWithShadow(textRenderer, "✔", boxX + 3, boxY + 1, 0xFFFFFFFF);
                    } else {
                        context.fill(boxX + 1, boxY + 1, boxX + boxSize - 1, boxY + boxSize - 1, 0xFF111111);
                    }
                    int textWidth = textRenderer.getWidth(textStr);
                    context.drawTextWithShadow(textRenderer, textStr, boxX - textWidth - 8, currentY + 6, textColor);
                }
                else if (setting instanceof TextSetting textSetting) {
                    TextFieldWidget field = textFields.get(textSetting);
                    if (field != null) { field.setX(widgetX); field.setY(currentY + 2); field.visible = true; }

                    String raw = textSetting.getValue();
                    if (raw == null) raw = "";
                    String preview = raw;
                    if (textSetting.getPreviewPlaceholders() != null) {
                        for (Map.Entry<String, java.util.function.Supplier<String>> entry : textSetting.getPreviewPlaceholders().entrySet()) {
                            preview = preview.replace(entry.getKey(), entry.getValue().get());
                        }
                    }
                    Text coloredPreview = ColorUtils.parseColor(preview);
                    context.drawText(textRenderer, coloredPreview, widgetX, currentY + 22, 0xFFAAAAAA, true);
                }
                else if (setting instanceof ModeSetting mode) {
                    int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    context.fill(widgetX, currentY, widgetX + dWidth, currentY + 15, 0xFF333333);
                    context.drawCenteredTextWithShadow(textRenderer, mode.getMode(), widgetX + (dWidth / 2), currentY + 4, 0xFFFFFFFF);
                    context.drawTextWithShadow(textRenderer, "v", widgetX + dWidth - 10, currentY + 4, 0xFFAAAAAA);
                    if (activeModeDropdown == mode) { dropdownX = widgetX; dropdownY = currentY + 15; }
                }
                else if (setting instanceof StringSetting str) {
                    TextFieldWidget field = stringFields.get(str);
                    if (field != null) { field.setX(widgetX); field.setY(currentY + 2); field.visible = true; }
                }
                else if (setting instanceof NumberSetting num) {
                    drawNumberSlider(context, num, widgetX, currentY + 6, mouseX, mouseY);
                    TextFieldWidget field = numberFields.get(num);
                    if (field != null) { field.setX(widgetX + WIDGET_WIDTH + 5); field.setY(currentY + 3); field.visible = true; }
                }
                else if (setting instanceof FloatSetting flt) {
                    drawFloatSlider(context, flt, widgetX, currentY + 6, mouseX, mouseY);
                    TextFieldWidget field = floatFields.get(flt);
                    if (field != null) { field.setX(widgetX + WIDGET_WIDTH + 5); field.setY(currentY + 3); field.visible = true; }
                }
                else if (setting instanceof RangeSetting rs) {
                    drawRangeSlider(context, rs, widgetX + 35, currentY + 6, 75, mouseX, mouseY);
                    TextFieldWidget[] fields = rangeFields.get(rs);
                    if (fields != null) {
                        fields[0].setX(widgetX); fields[0].setY(currentY + 3); fields[0].visible = true;
                        fields[1].setX(widgetX + 115); fields[1].setY(currentY + 3); fields[1].visible = true;
                    }
                }
                else if (setting instanceof ColorSetting color) {
                    drawColorSection(context, color, widgetX, currentY, mouseX, mouseY);
                    TextFieldWidget[] fields = colorFields.get(color);
                    if (fields != null) {
                        int fieldX = widgetX + WIDGET_WIDTH + 5;
                        fields[0].setX(fieldX); fields[0].setY(currentY + 15);
                        fields[1].setX(fieldX); fields[1].setY(currentY + 35);
                        fields[2].setX(fieldX); fields[2].setY(currentY + 55);
                        for (TextFieldWidget f : fields) f.visible = true;
                    }
                }
                else if (setting instanceof KeySetting key) {
                    ButtonWidget btn = keyButtons.get(key);
                    if (btn != null) {
                        btn.setX(widgetX); btn.setY(currentY);
                        if (bindingKey == key) btn.setMessage(Text.literal("..."));
                        else btn.setMessage(Text.literal(key.getKeyName()));
                        btn.visible = true;
                    }
                }
                else if (setting instanceof SoundSetting sound) {
                    int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                    context.fill(widgetX, currentY, widgetX + dWidth, currentY + 15, 0xFF333333);

                    String display = sound.getSoundId().replace("minecraft:", "").replace('.', ' ').replace('_', ' ').toUpperCase();
                    if (display.length() > 22) display = display.substring(0, 19) + "...";
                    context.drawTextWithShadow(textRenderer, display, widgetX + 4, currentY + 4, 0xFFFFFFFF);
                    context.drawTextWithShadow(textRenderer, "v", widgetX + dWidth - 10, currentY + 4, 0xFFAAAAAA);

                    int sliderX = widgetX + 55; int sliderW = dWidth - 60;
                    int volY = currentY + 20;
                    context.drawTextWithShadow(textRenderer, "Vol: " + String.format("%.2f", sound.getVolume()), widgetX, volY - 2, 0xFFFFFFFF);
                    context.fill(sliderX, volY, sliderX + sliderW, volY + 4, 0xFF444444);

                    if (draggingSoundVol == sound) {
                        float percent = (float)(mouseX - sliderX) / sliderW;
                        percent = MathHelper.clamp(percent, 0, 1);
                        sound.setVolume(percent);
                        ConfigManager.save();
                    }
                    int volKnocker = sliderX + (int)((sound.getVolume()) * sliderW);
                    context.fill(volKnocker - 2, volY - 2, volKnocker + 2, volY + 6, 0xFF55FF55);

                    int pitchY = currentY + 35;
                    context.drawTextWithShadow(textRenderer, "Pitch: " + String.format("%.1f", sound.getPitch()), widgetX, pitchY - 2, 0xFFFFFFFF);
                    context.fill(sliderX, pitchY, sliderX + sliderW, pitchY + 4, 0xFF444444);

                    if (draggingSoundPitch == sound) {
                        float percent = (float)(mouseX - sliderX) / sliderW;
                        percent = MathHelper.clamp(percent, 0, 1);
                        sound.setPitch(0.5f + (percent * 1.5f));
                        ConfigManager.save();
                    }
                    float pitchPercent = (sound.getPitch() - 0.5f) / 1.5f;
                    int pitchKnocker = sliderX + (int)(pitchPercent * sliderW);
                    context.fill(pitchKnocker - 2, pitchY - 2, pitchKnocker + 2, pitchY + 6, 0xFF55FF55);

                    int playY = currentY + 50;
                    context.fill(widgetX, playY, widgetX + dWidth, playY + 12, 0xFF00AA00);
                    context.drawCenteredTextWithShadow(textRenderer, "Play Test Sound", widgetX + (dWidth / 2), playY + 2, 0xFFFFFFFF);
                    if (activeDropdown == sound) { dropdownX = widgetX; dropdownY = currentY + 15; }
                }
            } else {
                hideWidgetsFor(setting);
            }
            currentY += settingHeight;
        }

        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        if (contentHeight > mainHeight) {
            int scrollbarHeight = Math.max(20, (int) ((float) mainHeight / contentHeight * mainHeight));
            int scrollbarY = mainY + (int) ((scrollOffsetY / maxMainScroll) * (mainHeight - scrollbarHeight));

            context.fill(mainX + mainWidth - 4, mainY, mainX + mainWidth, mainY + mainHeight, 0x40000000);

            int scrollColor = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.fill(mainX + mainWidth - 4, scrollbarY, mainX + mainWidth, scrollbarY + scrollbarHeight, scrollColor);
        }

        if (activeDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            int searchHeight = 15;
            int listHeight = 100;
            int itemHeight = 10;
            int visibleItems = listHeight / itemHeight;

            List<String> filtered = getFilteredSounds();
            int maxDropdownScroll = Math.max(0, filtered.size() - visibleItems);

            if (isDraggingDropdownScrollbar) {
                if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    isDraggingDropdownScrollbar = false;
                } else {
                    int fullListHeight = filtered.size() * itemHeight;
                    int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                    float scrollFraction = (float) (mouseY - dropdownScrollbarDragOffsetY - (dropdownY + searchHeight)) / (listHeight - scrollbarHeight);
                    scrollFraction = MathHelper.clamp(scrollFraction, 0.0f, 1.0f);
                    dropdownScrollIndex = Math.round(scrollFraction * maxDropdownScroll);
                }
            }

            dropdownScrollIndex = MathHelper.clamp(dropdownScrollIndex, 0, maxDropdownScroll);

            context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + searchHeight + listHeight, 0xFF111111);
            context.fill(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + searchHeight - 1, 0xFF222222);
            String searchStr = soundSearchQuery.isEmpty() ? "§8Type to search" : soundSearchQuery;
            if (searchStr.length() > 22) searchStr = searchStr.substring(searchStr.length() - 22);
            context.drawText(this.textRenderer, searchStr + "_", dropdownX + 4, dropdownY + 4, 0xFFFFFFFF, false);

            int maxIndex = Math.min(dropdownScrollIndex + visibleItems, filtered.size());
            int drawY = dropdownY + searchHeight + 2;

            for (int i = dropdownScrollIndex; i < maxIndex; i++) {
                String soundName = filtered.get(i);
                boolean isHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth - 6 && mouseY >= drawY && mouseY <= drawY + itemHeight;
                if (isHovered) context.fill(dropdownX + 1, drawY, dropdownX + dropdownWidth - 6, drawY + itemHeight, 0xFF44AAFF);
                String display = soundName.replace("minecraft:", "").replace('.', ' ').replace('_', ' ').toUpperCase();
                if (display.length() > 20) display = display.substring(0, 18) + "...";
                context.drawText(this.textRenderer, display, dropdownX + 4, drawY + 1, isHovered ? 0xFFFFFFFF : 0xFFAAAAAA, false);
                drawY += itemHeight;
            }

            if (filtered.size() > visibleItems) {
                int scrollbarX = dropdownX + dropdownWidth - 5;
                int scrollbarYStart = dropdownY + searchHeight;
                int fullListHeight = filtered.size() * itemHeight;
                int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                int scrollbarY = scrollbarYStart + (int) (((float) dropdownScrollIndex / maxDropdownScroll) * (listHeight - scrollbarHeight));

                context.fill(scrollbarX, scrollbarYStart, scrollbarX + 4, scrollbarYStart + listHeight, 0x40000000);
                int scrollColor = isDraggingDropdownScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
                context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, scrollColor);
            }
        }

        if (activeModeDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            List<String> modes = activeModeDropdown.getModes();
            int listHeight = modes.size() * 10;

            context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + listHeight, 0xFF111111);
            int drawY = dropdownY;
            for (String modeStr : modes) {
                boolean isHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth && mouseY >= drawY && mouseY <= drawY + 10;
                if (isHovered) context.fill(dropdownX + 1, drawY, dropdownX + dropdownWidth - 1, drawY + 10, 0xFF44AAFF);
                context.drawCenteredTextWithShadow(this.textRenderer, modeStr, dropdownX + (dropdownWidth / 2), drawY + 1, isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                drawY += 10;
            }
        }

        if (activeMultiDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            int searchHeight = 15;
            List<String> filteredOptions = getFilteredMultiOptions();
            int itemHeight = 10;
            int visibleItems = 15;
            int listHeight = Math.min(filteredOptions.size(), visibleItems) * itemHeight;
            int maxMultiScroll = Math.max(0, filteredOptions.size() - visibleItems);

            if (isDraggingMultiScrollbar) {
                if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    isDraggingMultiScrollbar = false;
                } else {
                    int fullListHeight = filteredOptions.size() * itemHeight;
                    int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                    float scrollFraction = (float) (mouseY - multiScrollbarDragOffsetY - (dropdownY + searchHeight)) / (listHeight - scrollbarHeight);
                    scrollFraction = MathHelper.clamp(scrollFraction, 0.0f, 1.0f);
                    multiScrollIndex = Math.round(scrollFraction * maxMultiScroll);
                }
            }

            multiScrollIndex = MathHelper.clamp(multiScrollIndex, 0, maxMultiScroll);

            context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + searchHeight + listHeight, 0xFF111111);

            context.fill(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth - 1, dropdownY + searchHeight - 1, 0xFF222222);
            String searchStr = multiSearchQuery.isEmpty() ? "§8Type to search" : multiSearchQuery;
            if (searchStr.length() > 22) searchStr = searchStr.substring(searchStr.length() - 22);
            context.drawText(this.textRenderer, searchStr + "_", dropdownX + 4, dropdownY + 4, 0xFFFFFFFF, false);

            int maxIndex = Math.min(multiScrollIndex + visibleItems, filteredOptions.size());
            int drawY = dropdownY + searchHeight + 2;

            for (int i = multiScrollIndex; i < maxIndex; i++) {
                String opt = filteredOptions.get(i);
                boolean isHovered = mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth - 6 && mouseY >= drawY && mouseY <= drawY + itemHeight;
                if (isHovered) context.fill(dropdownX + 1, drawY, dropdownX + dropdownWidth - 6, drawY + itemHeight, 0xFF44AAFF);
                if (activeMultiDropdown.isSelected(opt)) context.drawTextWithShadow(textRenderer, "✔", dropdownX + 4, drawY + 1, 0xFF55FF55);
                context.drawTextWithShadow(textRenderer, opt, dropdownX + 16, drawY + 1, isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
                drawY += itemHeight;
            }

            if (filteredOptions.size() > visibleItems) {
                int scrollbarX = dropdownX + dropdownWidth - 5;
                int scrollbarYStart = dropdownY + searchHeight;
                int fullListHeight = filteredOptions.size() * itemHeight;
                int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                int scrollbarY = scrollbarYStart + (int) (((float) multiScrollIndex / maxMultiScroll) * (listHeight - scrollbarHeight));

                context.fill(scrollbarX, scrollbarYStart, scrollbarX + 4, scrollbarYStart + listHeight, 0x40000000);
                int scrollColor = isDraggingMultiScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
                context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, scrollColor);
            }
        }
    }

    private void hideWidgetsFor(Settings setting) {
        if (setting instanceof BooleanSetting && booleanButtons.containsKey(setting)) booleanButtons.get(setting).visible = false;
        else if (setting instanceof ModeSetting && modeButtons.containsKey(setting)) modeButtons.get(setting).visible = false;
        else if (setting instanceof TagSetting && tagFields.containsKey(setting)) tagFields.get(setting).visible = false;
        else if (setting instanceof StringSetting && stringFields.containsKey(setting)) stringFields.get(setting).visible = false;
        else if (setting instanceof NumberSetting && numberFields.containsKey(setting)) numberFields.get(setting).visible = false;
        else if (setting instanceof FloatSetting && floatFields.containsKey(setting)) floatFields.get(setting).visible = false;
        else if (setting instanceof ColorSetting && colorFields.containsKey(setting)) for (TextFieldWidget f : colorFields.get(setting)) f.visible = false;
        else if (setting instanceof TextSetting && textFields.containsKey(setting)) textFields.get(setting).visible = false;
        else if (setting instanceof KeySetting && keyButtons.containsKey(setting)) keyButtons.get(setting).visible = false;
        else if (setting instanceof RangeSetting && rangeFields.containsKey(setting)) for (TextFieldWidget f : rangeFields.get(setting)) f.visible = false;
    }

    private void drawRangeSlider(DrawContext context, RangeSetting setting, int x, int y, int width, int mouseX, int mouseY) {
        context.fill(x, y, x + width, y + 4, 0xFF444444);
        double totalRange = setting.getAbsoluteMax() - setting.getAbsoluteMin();

        if (draggingRangeSetting == setting) {
            float percent = (float)(mouseX - x) / (float)width;
            percent = MathHelper.clamp(percent, 0, 1);
            int newVal = (int) Math.round(setting.getAbsoluteMin() + (totalRange * percent));

            if (draggingRangeIsMax) {
                if (newVal < setting.getMin()) { draggingRangeIsMax = false; setting.setMin(newVal); }
                else { setting.setMax(newVal); }
            } else {
                if (newVal > setting.getMax()) { draggingRangeIsMax = true; setting.setMax(newVal); }
                else { setting.setMin(newVal); }
            }
            rangeFields.get(setting)[0].setText(String.valueOf(setting.getMin()));
            rangeFields.get(setting)[1].setText(String.valueOf(setting.getMax()));
            ConfigManager.save();
        }

        double minPercent = (double)(setting.getMin() - setting.getAbsoluteMin()) / totalRange;
        double maxPercent = (double)(setting.getMax() - setting.getAbsoluteMin()) / totalRange;
        int minX = x + (int)(minPercent * width);
        int maxX = x + (int)(maxPercent * width);

        context.fill(minX, y, maxX, y + 4, 0xFF55FF55);
        context.fill(minX - 2, y - 2, minX + 2, y + 6, 0xFFFFFFFF);
        context.fill(maxX - 2, y - 2, maxX + 2, y + 6, 0xFFFFFFFF);
    }

    private void drawColorSection(DrawContext context, ColorSetting setting, int x, int y, int mouseX, int mouseY) {
        context.fill(x + WIDGET_WIDTH + 5, y, x + WIDGET_WIDTH + 40, y + 14, setting.getRGB());
        Color c = new Color(setting.getRGB(), true);
        drawSingleColorSlider(context, setting, "R", c.getRed(), x, y + 15, 0xFFFF5555, mouseX);
        drawSingleColorSlider(context, setting, "G", c.getGreen(), x, y + 35, 0xFF55FF55, mouseX);
        drawSingleColorSlider(context, setting, "B", c.getBlue(), x, y + 55, 0xFF5555FF, mouseX);
    }

    private void drawSingleColorSlider(DrawContext context, ColorSetting setting, String type, int value, int x, int y, int color, int mouseX) {
        context.drawText(textRenderer, type, x - 10, y - 2, 0xFFFFFFFF, true);
        context.fill(x, y + 2, x + WIDGET_WIDTH, y + 6, 0xFF444444);

        if (draggingColor == setting && draggingColorComponent.equals(type)) {
            float percent = (float)(mouseX - x) / (float)WIDGET_WIDTH;
            int newVal = (int)(MathHelper.clamp(percent, 0, 1) * 255);
            updateColorFromField(setting, type, newVal);

            TextFieldWidget[] fields = colorFields.get(setting);
            if (fields != null) {
                if (type.equals("R")) fields[0].setText(String.valueOf(newVal));
                if (type.equals("G")) fields[1].setText(String.valueOf(newVal));
                if (type.equals("B")) fields[2].setText(String.valueOf(newVal));
            }
            ConfigManager.save();
            value = newVal;
        }
        int knockerX = x + (int)((value / 255.0f) * WIDGET_WIDTH);
        context.fill(knockerX - 2, y - 2, knockerX + 2, y + 10, color);
    }

    private void drawFloatSlider(DrawContext context, FloatSetting setting, int x, int y, int mouseX, int mouseY) {
        context.fill(x, y, x + WIDGET_WIDTH, y + 4, 0xFF444444);
        float range = setting.getMax() - setting.getMin();

        if (draggingFloat == setting) {
            float percent = (float)(mouseX - x) / (float)WIDGET_WIDTH;
            percent = MathHelper.clamp(percent, 0, 1);
            float newVal = setting.getMin() + (range * percent);
            if (newVal != setting.getValue()) {
                setting.setValue(newVal);
                ConfigManager.save();
                if (floatFields.containsKey(setting)) floatFields.get(setting).setText(String.format("%.2f", setting.getValue()));
            }
        }
        float percent = (setting.getValue() - setting.getMin()) / range;
        int knockerX = x + (int)(percent * WIDGET_WIDTH);
        context.fill(knockerX - 2, y - 2, knockerX + 2, y + 6, 0xFF55FF55);
    }

    private void drawNumberSlider(DrawContext context, NumberSetting setting, int x, int y, int mouseX, int mouseY) {
        context.fill(x, y, x + WIDGET_WIDTH, y + 4, 0xFF444444);
        double range = setting.getMax() - setting.getMin();

        if (draggingSetting == setting) {
            float percent = (float)(mouseX - x) / (float)WIDGET_WIDTH;
            percent = MathHelper.clamp(percent, 0, 1);
            int newVal = (int) (setting.getMin() + (range * percent));
            if (newVal != setting.getValue()) {
                setting.setValue(newVal);
                ConfigManager.save();
                if (numberFields.containsKey(setting)) numberFields.get(setting).setText(String.valueOf(setting.getValue()));
            }
        }
        double percent = (double) (setting.getValue() - setting.getMin()) / range;
        int knockerX = x + (int)(percent * WIDGET_WIDTH);
        context.fill(knockerX - 2, y - 2, knockerX + 2, y + 6, 0xFF55FF55);
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

        if (activeModeDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            List<String> modes = activeModeDropdown.getModes();
            int listHeight = modes.size() * 10;
            if (click.x() >= dropdownX && click.x() <= dropdownX + dropdownWidth && click.y() >= dropdownY && click.y() <= dropdownY + listHeight) {
                int clickedIndex = (int) ((click.y() - dropdownY) / 10);
                if (clickedIndex >= 0 && clickedIndex < modes.size()) {
                    activeModeDropdown.setMode(modes.get(clickedIndex));
                    client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    ConfigManager.save();
                }
            }
            activeModeDropdown = null;
            return true;
        }

        if (activeMultiDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            int searchHeight = 15;
            List<String> filteredOptions = getFilteredMultiOptions();
            int itemHeight = 10;
            int visibleItems = 15;
            int listHeight = Math.min(filteredOptions.size(), visibleItems) * itemHeight;
            int maxMultiScroll = Math.max(0, filteredOptions.size() - visibleItems);

            if (filteredOptions.size() > visibleItems) {
                int scrollbarX = dropdownX + dropdownWidth - 5;
                int scrollbarYStart = dropdownY + searchHeight;
                int fullListHeight = filteredOptions.size() * itemHeight;
                int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                int scrollbarY = scrollbarYStart + (int) (((float) multiScrollIndex / maxMultiScroll) * (listHeight - scrollbarHeight));

                if (click.x() >= scrollbarX && click.x() <= scrollbarX + 4 && click.y() >= scrollbarY && click.y() <= scrollbarY + scrollbarHeight) {
                    if (click.button() == 0) {
                        isDraggingMultiScrollbar = true;
                        multiScrollbarDragOffsetY = click.y() - scrollbarY;
                        return true;
                    }
                }
            }

            if (click.x() >= dropdownX && click.x() <= dropdownX + dropdownWidth && click.y() >= dropdownY && click.y() <= dropdownY + searchHeight + listHeight) {
                if (click.y() < dropdownY + searchHeight) return true;

                if (click.x() < dropdownX + dropdownWidth - 5) {
                    int clickedIndex = multiScrollIndex + (int) ((click.y() - (dropdownY + searchHeight)) / itemHeight);
                    if (clickedIndex >= 0 && clickedIndex < filteredOptions.size()) {
                        activeMultiDropdown.toggleOption(filteredOptions.get(clickedIndex));
                        client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        ConfigManager.save();
                    }
                }
                return true;
            }
            activeMultiDropdown = null;
            multiSearchQuery = "";
            return true;
        }

        if (activeDropdown != null) {
            dropdownWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
            int searchHeight = 15;
            int listHeight = 100;
            List<String> filtered = getFilteredSounds();

            if (filtered.size() > 10) {
                int scrollbarX = dropdownX + dropdownWidth - 5;
                int scrollbarYStart = dropdownY + searchHeight;
                int fullListHeight = filtered.size() * 10;
                int scrollbarHeight = Math.max(20, (int) ((float) listHeight / fullListHeight * listHeight));
                int maxDropdownScroll = Math.max(0, filtered.size() - 10);
                int scrollbarY = scrollbarYStart + (int) (((float) dropdownScrollIndex / maxDropdownScroll) * (listHeight - scrollbarHeight));

                if (click.x() >= scrollbarX && click.x() <= scrollbarX + 4 && click.y() >= scrollbarY && click.y() <= scrollbarY + scrollbarHeight) {
                    if (click.button() == 0) {
                        isDraggingDropdownScrollbar = true;
                        dropdownScrollbarDragOffsetY = click.y() - scrollbarY;
                        return true;
                    }
                }
            }

            if (click.x() >= dropdownX && click.x() <= dropdownX + dropdownWidth && click.y() >= dropdownY && click.y() <= dropdownY + searchHeight + listHeight) {
                if (click.y() < dropdownY + searchHeight) return true;

                if (click.x() < dropdownX + dropdownWidth - 5) {
                    int clickedIndex = dropdownScrollIndex + (int) ((click.y() - (dropdownY + searchHeight)) / 10);
                    if (clickedIndex >= 0 && clickedIndex < filtered.size()) {
                        activeDropdown.setSoundId(filtered.get(clickedIndex));
                        client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        ConfigManager.save();
                        activeDropdown = null;
                        soundSearchQuery = "";
                    }
                }
                return true;
            }
            activeDropdown = null;
            soundSearchQuery = "";
            return true;
        }

        if (bindingKey != null) {
            bindingKey.setCode(-100 - click.button());
            ButtonWidget btn = keyButtons.get(bindingKey);
            if (btn != null) btn.setMessage(Text.literal(bindingKey.getKeyName()));
            ConfigManager.save();
            bindingKey = null;
            return true;
        }

        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() != 0) return false;

        if (click.x() < mainX || click.x() > mainX + mainWidth || click.y() < mainY || click.y() > mainY + mainHeight) return false;

        int widgetX = mainX + mainWidth - WIDGET_WIDTH - FIELD_WIDTH - 20;
        int currentY = mainY + 10 - (int)scrollOffsetY;

        for (Settings setting : feature.getSettings()) {
            if (!setting.isVisible()) continue;

            if (setting instanceof NumberSetting) {
                if (checkSliderClick(click.x(), click.y(), widgetX, currentY + 6, WIDGET_WIDTH)) { draggingSetting = (NumberSetting) setting; return true; }
                currentY += 25;
            } else if (setting instanceof FloatSetting) {
                if (checkSliderClick(click.x(), click.y(), widgetX, currentY + 6, WIDGET_WIDTH)) { draggingFloat = (FloatSetting) setting; return true; }
                currentY += 25;
            } else if (setting instanceof CheckMarkSetting check) {
                int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY && click.y() <= currentY + 20) {
                    check.toggle();
                    client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    ConfigManager.save();
                    return true;
                }
                currentY += 25;
            } else if (setting instanceof RangeSetting rs) {
                if (checkSliderClick(click.x(), click.y(), widgetX + 35, currentY + 6, 75)) {
                    draggingRangeSetting = rs;
                    double totalRange = rs.getAbsoluteMax() - rs.getAbsoluteMin();
                    double minP = (double)(rs.getMin() - rs.getAbsoluteMin()) / totalRange;
                    double maxP = (double)(rs.getMax() - rs.getAbsoluteMin()) / totalRange;
                    int minX = (widgetX + 35) + (int)(minP * 75);
                    int maxX = (widgetX + 35) + (int)(maxP * 75);
                    draggingRangeIsMax = Math.abs(click.x() - maxX) <= Math.abs(click.x() - minX);
                    return true;
                }
                currentY += 25;
            } else if (setting instanceof MultiSelectSetting multi) {
                int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY && click.y() <= currentY + 15) {
                    activeMultiDropdown = multi;
                    multiScrollIndex = 0;
                    multiSearchQuery = "";
                    return true;
                }
                currentY += 25;
            } else if (setting instanceof BooleanSetting bool) {
                int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY && click.y() <= currentY + 20) {
                    bool.toggle();
                    client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    ConfigManager.save();
                    return true;
                }
                currentY += 25;
            } else if (setting instanceof ColorSetting) {
                if (checkSliderClick(click.x(), click.y(), widgetX, currentY + 15, WIDGET_WIDTH)) { draggingColor = (ColorSetting) setting; draggingColorComponent = "R"; return true; }
                if (checkSliderClick(click.x(), click.y(), widgetX, currentY + 35, WIDGET_WIDTH)) { draggingColor = (ColorSetting) setting; draggingColorComponent = "G"; return true; }
                if (checkSliderClick(click.x(), click.y(), widgetX, currentY + 55, WIDGET_WIDTH)) { draggingColor = (ColorSetting) setting; draggingColorComponent = "B"; return true; }
                currentY += 85;
            } else if (setting instanceof SoundSetting sound) {
                int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY && click.y() <= currentY + 15) {
                    activeDropdown = sound; soundSearchQuery = ""; dropdownScrollIndex = 0; return true;
                }
                int sliderX = widgetX + 55; int sliderW = dWidth - 60;
                if (checkSliderClick(click.x(), click.y(), sliderX, currentY + 20, sliderW)) { draggingSoundVol = sound; return true; }
                if (checkSliderClick(click.x(), click.y(), sliderX, currentY + 35, sliderW)) { draggingSoundPitch = sound; return true; }
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY + 50 && click.y() <= currentY + 62) {
                    sound.playTestSound(); return true;
                }
                currentY += 70;
            } else if (setting instanceof ModeSetting mode) {
                int dWidth = WIDGET_WIDTH + FIELD_WIDTH + 5;
                if (click.x() >= widgetX && click.x() <= widgetX + dWidth && click.y() >= currentY && click.y() <= currentY + 15) {
                    activeModeDropdown = mode; return true;
                }
                currentY += 25;
            } else if (setting instanceof TextSetting || setting instanceof TagSetting) {
                currentY += 45;
            } else {
                currentY += 25;
            }
        }
        return false;
    }

    private boolean checkSliderClick(double mouseX, double mouseY, int x, int y, int width) {
        return mouseX >= x - 5 && mouseX <= x + width + 5 && mouseY >= y - 5 && mouseY <= y + 15;
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingSetting = null; draggingColor = null; draggingFloat = null; draggingColorComponent = "";
        draggingRangeSetting = null; draggingSoundVol = null; draggingSoundPitch = null;
        isDraggingScrollbar = false;
        isDraggingDropdownScrollbar = false;
        isDraggingMultiScrollbar = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        int code = key.getKeycode();

        if (activeMultiDropdown != null) {
            if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !multiSearchQuery.isEmpty()) {
                multiSearchQuery = multiSearchQuery.substring(0, multiSearchQuery.length() - 1); multiScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || code == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                activeMultiDropdown = null; multiSearchQuery = "";
            }
            else if (code >= org.lwjgl.glfw.GLFW.GLFW_KEY_A && code <= org.lwjgl.glfw.GLFW.GLFW_KEY_Z) {
                multiSearchQuery += (char) ('a' + (code - org.lwjgl.glfw.GLFW.GLFW_KEY_A)); multiScrollIndex = 0;
            }
            else if (code >= org.lwjgl.glfw.GLFW.GLFW_KEY_0 && code <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
                multiSearchQuery += (char) ('0' + (code - org.lwjgl.glfw.GLFW.GLFW_KEY_0)); multiScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
                multiSearchQuery += " "; multiScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS) {
                multiSearchQuery += "_"; multiScrollIndex = 0;
            }
            return true;
        }

        if (activeDropdown != null) {
            if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE && !soundSearchQuery.isEmpty()) {
                soundSearchQuery = soundSearchQuery.substring(0, soundSearchQuery.length() - 1); dropdownScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || code == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                activeDropdown = null; soundSearchQuery = "";
            }
            else if (code >= org.lwjgl.glfw.GLFW.GLFW_KEY_A && code <= org.lwjgl.glfw.GLFW.GLFW_KEY_Z) {
                soundSearchQuery += (char) ('a' + (code - org.lwjgl.glfw.GLFW.GLFW_KEY_A)); dropdownScrollIndex = 0;
            }
            else if (code >= org.lwjgl.glfw.GLFW.GLFW_KEY_0 && code <= org.lwjgl.glfw.GLFW.GLFW_KEY_9) {
                soundSearchQuery += (char) ('0' + (code - org.lwjgl.glfw.GLFW.GLFW_KEY_0)); dropdownScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE) {
                soundSearchQuery += " "; dropdownScrollIndex = 0;
            }
            else if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS) {
                soundSearchQuery += "_"; dropdownScrollIndex = 0;
            }
            return true;
        }

        if (bindingKey != null) {
            if (code == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) bindingKey.setCode(-1);
            else {
                for (Settings s : feature.getSettings()) {
                    if (s instanceof KeySetting otherKey && otherKey != bindingKey && otherKey.getCode() == code) otherKey.setCode(-1);
                }
                bindingKey.setCode(code);
            }
            ConfigManager.save();
            bindingKey = null;
            return true;
        }
        return super.keyPressed(key);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}