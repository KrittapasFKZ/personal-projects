package net.otsutsukimiho.nozomiaddon.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.otsutsukimiho.nozomiaddon.features.ClickGUI;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemBrowserScreen extends Screen {

    private static final int BOX_WIDTH = 340;
    private static final int BOX_HEIGHT = 220;

    private TextFieldWidget searchField;
    private final Map<String, String> allItems = new LinkedHashMap<>();
    private final List<String> filteredNames = new ArrayList<>();

    private double scrollOffset = 0;
    private double targetScrollOffset = 0;
    private boolean isDraggingScrollbar = false;
    private double scrollbarDragOffsetY = 0;
    private long lastRenderTime = 0;

    private String displayPrice = "";
    private ItemStack displayItem = ItemStack.EMPTY;
    private boolean isLoading = false;
    private String selectedId = "";

    public ItemBrowserScreen() {
        super(Text.literal("Item Browser"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxLeft = centerX - (BOX_WIDTH / 2);
        int boxTop = centerY - (BOX_HEIGHT / 2);

        lastRenderTime = System.currentTimeMillis();

        loadLocalItems();

        searchField = new TextFieldWidget(this.textRenderer, boxLeft + 10, boxTop + 25, 140, 16, Text.literal("Search..."));
        searchField.setChangedListener(this::updateSearch);
        this.addDrawableChild(searchField);

        updateSearch("");
    }

    public static String shortNumber(double number) {
        if (number < 1000) return String.valueOf((long) number);
        String[] units = {"k", "m", "b", "t"};
        int unitIndex = (int) Math.floor((String.valueOf((long) number).length() - 1) / 3.0) - 1;
        double abbreviatedNumber = number / Math.pow(1000, unitIndex + 1);
        if (abbreviatedNumber < 1 && unitIndex > 0) {
            unitIndex--;
            abbreviatedNumber = number / Math.pow(1000, unitIndex + 1);
        }
        return String.format(Locale.US, "%.1f%s", abbreviatedNumber, units[unitIndex]);
    }

    private void loadLocalItems() {
        try {
            File itemsFile = new File("config/nozomiaddon/items.json");
            if (itemsFile.exists()) {
                JsonObject itemsObj = JsonParser.parseReader(new FileReader(itemsFile)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : itemsObj.entrySet()) {
                    allItems.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSearch(String query) {
        filteredNames.clear();
        scrollOffset = 0;
        targetScrollOffset = 0;
        String lowerQuery = query.toLowerCase();

        for (String name : allItems.keySet()) {
            if (name.toLowerCase().contains(lowerQuery)) {
                filteredNames.add(name);
            }
        }
        filteredNames.sort(String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = lastRenderTime == 0 ? 0.016f : (currentTime - lastRenderTime) / 1000f;
        lastRenderTime = currentTime;

        if (deltaTime > 0.05f) deltaTime = 0.05f;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxLeft = centerX - (BOX_WIDTH / 2);
        int boxTop = centerY - (BOX_HEIGHT / 2);
        int boxRight = centerX + (BOX_WIDTH / 2);
        int boxBottom = centerY + (BOX_HEIGHT / 2);

        int listDisplayHeight = (boxBottom - 10) - (boxTop + 45);
        int contentHeight = filteredNames.size() * 12;
        int maxScroll = Math.max(0, contentHeight - listDisplayHeight);

        if (isDraggingScrollbar) {
            if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                isDraggingScrollbar = false;
            } else {
                int scrollbarHeight = Math.max(20, (int) ((float) listDisplayHeight / contentHeight * listDisplayHeight));
                float scrollFraction = (float) (mouseY - scrollbarDragOffsetY - (boxTop + 45)) / (listDisplayHeight - scrollbarHeight);
                scrollFraction = MathHelper.clamp(scrollFraction, 0.0f, 1.0f);
                targetScrollOffset = scrollFraction * maxScroll;
                scrollOffset = targetScrollOffset;
            }
        }

        if (Math.abs(targetScrollOffset - scrollOffset) > 0.5f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * 15f * deltaTime;
        } else {
            scrollOffset = targetScrollOffset;
        }

        context.fill(boxLeft, boxTop, boxRight, boxBottom, 0xDD111111);
        context.fill(boxLeft, boxTop, boxRight, boxTop + 15, ClickGUI.headerColor.getRGB());
        context.drawCenteredTextWithShadow(textRenderer, "NozomiAddon Item Browser", centerX, boxTop + 4, 0xFFFFFFFF);

        context.fill(boxLeft + 5, boxTop + 45, boxLeft + 155, boxBottom - 10, 0xFF222222);
        context.fill(boxLeft + 165, boxTop + 25, boxRight - 10, boxBottom - 10, 0xFF222222);

        super.render(context, mouseX, mouseY, delta);

        context.enableScissor(boxLeft + 5, boxTop + 45, boxLeft + 155, boxBottom - 10);
        int listY = boxTop + 48 - (int)scrollOffset;

        for (int i = 0; i < filteredNames.size(); i++) {
            String name = filteredNames.get(i);

            boolean isHovered = mouseX >= boxLeft + 5 && mouseX <= boxLeft + 151 && mouseY >= listY && mouseY < listY + 12;
            boolean isSelected = allItems.get(name).equals(selectedId);

            if (isSelected) {
                context.fill(boxLeft + 5, listY, boxLeft + 151, listY + 12, 0xFF55AAFF);
            } else if (isHovered) {
                context.fill(boxLeft + 5, listY, boxLeft + 151, listY + 12, 0xFF444444);
            }

            context.drawTextWithShadow(textRenderer, name, boxLeft + 10, listY + 2, isSelected ? 0xFFFFFFFF : 0xFFAAAAAA);
            listY += 12;
        }
        context.disableScissor();

        if (contentHeight > listDisplayHeight) {
            int scrollbarX = boxLeft + 151;
            int scrollbarYStart = boxTop + 45;
            int scrollbarHeight = Math.max(20, (int) ((float) listDisplayHeight / contentHeight * listDisplayHeight));
            int scrollbarY = scrollbarYStart + (int) ((scrollOffset / maxScroll) * (listDisplayHeight - scrollbarHeight));

            context.fill(scrollbarX, scrollbarYStart, scrollbarX + 4, scrollbarYStart + listDisplayHeight, 0x40000000);

            int scrollColor = isDraggingScrollbar ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, scrollColor);
        }

        int displayCenterX = boxLeft + 165 + ((boxRight - 10) - (boxLeft + 165)) / 2;

        if (isLoading) {
            context.drawCenteredTextWithShadow(textRenderer, "Fetching API...", displayCenterX, centerY, 0xFF777777);
        }
        else if (!displayItem.isEmpty()) {
            int itemScale = 3;
            int itemDrawX = displayCenterX - (8 * itemScale);
            int itemDrawY = boxTop + 50;

            context.getMatrices().pushMatrix();
            context.getMatrices().translate(itemDrawX, itemDrawY);
            context.getMatrices().scale(itemScale, itemScale);
            context.drawItem(displayItem, 0, 0);
            context.getMatrices().popMatrix();

            context.drawCenteredTextWithShadow(textRenderer, displayItem.getName().getString(), displayCenterX, itemDrawY + 55, 0xFFFFFFFF);

            if (!displayPrice.isEmpty()) {
                context.drawCenteredTextWithShadow(textRenderer, displayPrice, displayCenterX, itemDrawY + 68, 0xFFFFFFFF);
            }

            if (mouseX >= itemDrawX && mouseX <= itemDrawX + (16 * itemScale) && mouseY >= itemDrawY && mouseY <= itemDrawY + (16 * itemScale)) {
                context.drawItemTooltip(this.textRenderer, displayItem, mouseX, mouseY);
            }
        } else {
            context.drawCenteredTextWithShadow(textRenderer, "Select an item to view", displayCenterX, centerY, 0xFF777777);
        }
    }

    @Override
    public boolean mouseReleased(Click click) {
        isDraggingScrollbar = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxLeft = centerX - (BOX_WIDTH / 2);
        int boxTop = centerY - (BOX_HEIGHT / 2);
        int boxBottom = centerY + (BOX_HEIGHT / 2);

        int listDisplayHeight = (boxBottom - 10) - (boxTop + 45);
        int contentHeight = filteredNames.size() * 12;

        if (contentHeight > listDisplayHeight) {
            int maxScroll = contentHeight - listDisplayHeight;
            int scrollbarX = boxLeft + 151;
            int scrollbarYStart = boxTop + 45;
            int scrollbarHeight = Math.max(20, (int) ((float) listDisplayHeight / contentHeight * listDisplayHeight));
            int scrollbarY = scrollbarYStart + (int) ((scrollOffset / maxScroll) * (listDisplayHeight - scrollbarHeight));

            if (click.x() >= scrollbarX && click.x() <= scrollbarX + 4 && click.y() >= scrollbarY && click.y() <= scrollbarY + scrollbarHeight) {
                if (click.button() == 0) {
                    isDraggingScrollbar = true;
                    scrollbarDragOffsetY = click.y() - scrollbarY;
                    return true;
                }
            }
        }

        if (click.x() >= boxLeft + 5 && click.x() <= boxLeft + 151 && click.y() >= boxTop + 45 && click.y() <= boxBottom - 10) {
            int clickIndex = (int) (((click.y() - (boxTop + 48)) + scrollOffset) / 12);

            if (clickIndex >= 0 && clickIndex < filteredNames.size()) {
                String clickedName = filteredNames.get(clickIndex);
                String internalId = allItems.get(clickedName);

                if (!internalId.equals(selectedId) && !isLoading) {
                    selectedId = internalId;
                    fetchItemDataFromApi(clickedName, internalId);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int listDisplayHeight = BOX_HEIGHT - 55;
        int contentHeight = filteredNames.size() * 12;
        int maxScroll = Math.max(0, contentHeight - listDisplayHeight);

        if (verticalAmount != 0) {
            targetScrollOffset -= verticalAmount * 24;
            if (targetScrollOffset < 0) targetScrollOffset = 0;
            if (targetScrollOffset > maxScroll) targetScrollOffset = maxScroll;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void fetchItemDataFromApi(String displayName, String internalId) {
        isLoading = true;
        displayItem = ItemStack.EMPTY;
        displayPrice = "";

        CompletableFuture.runAsync(() -> {
            try {
                double rawPrice = PriceUtils.getLowestBin(internalId);
                if (rawPrice <= 0) {
                    rawPrice = PriceUtils.getBazaarBuyPrice(internalId);
                }
                final double finalPrice = rawPrice;

                URL url = new URL("https://cdn.jsdelivr.net/gh/NotEnoughUpdates/NotEnoughUpdates-REPO@master/items/" + internalId + ".json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn.getResponseCode() == 200) {
                    InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

                    String fetchedName = response.has("displayname") ? response.get("displayname").getAsString() : displayName;

                    String itemIdStr = response.has("itemid") ? response.get("itemid").getAsString().replace("minecraft:", "") : "diamond_sword";
                    if (itemIdStr.equals("skull")) itemIdStr = "player_head";

                    String base64Texture = null;
                    Integer armorColor = null;

                    if (response.has("nbttag")) {
                        String nbt = response.get("nbttag").getAsString();

                        Matcher m = Pattern.compile("Value:\"([A-Za-z0-9+/=]+)\"").matcher(nbt);
                        if (m.find()) {
                            base64Texture = m.group(1);
                        }

                        Matcher colorMatcher = Pattern.compile("color:(\\d+)").matcher(nbt);
                        if (colorMatcher.find()) {
                            armorColor = Integer.parseInt(colorMatcher.group(1));
                        }
                    }

                    List<Text> loreLines = new ArrayList<>();
                    if (response.has("lore")) {
                        JsonArray loreArray = response.getAsJsonArray("lore");
                        for (JsonElement element : loreArray) {
                            loreLines.add(Text.literal(element.getAsString()));
                        }
                    }

                    final String finalItemId = itemIdStr;
                    final String finalTexture = base64Texture;
                    final Integer finalColor = armorColor;

                    MinecraftClient.getInstance().execute(() -> {
                        ItemStack newItem;

                        if (finalTexture != null && !finalTexture.isEmpty()) {
                            newItem = HeadUtils.getSkull(finalTexture);
                        } else {
                            net.minecraft.item.Item item = Registries.ITEM.get(Identifier.of("minecraft", finalItemId));
                            if (item == Items.AIR) item = Items.BARRIER;
                            newItem = new ItemStack(item);
                        }

                        newItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal(fetchedName));

                        if (!loreLines.isEmpty()) {
                            newItem.set(DataComponentTypes.LORE, new LoreComponent(loreLines));
                        }

                        if (finalColor != null) {
                            newItem.set(DataComponentTypes.DYED_COLOR, new net.minecraft.component.type.DyedColorComponent(finalColor));
                        }

                        if (newItem.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
                            newItem.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new net.minecraft.component.type.AttributeModifiersComponent(new ArrayList<>()));
                        }

                        if (finalPrice > 0) {
                            this.displayPrice = "§ePrice: §6" + shortNumber(finalPrice) + " Coins";
                        } else {
                            this.displayPrice = "§ePrice: §cN/A";
                        }

                        this.displayItem = newItem;
                        this.isLoading = false;
                    });
                } else {
                    MinecraftClient.getInstance().execute(() -> {
                        this.isLoading = false;
                        if (finalPrice > 0) {
                            this.displayPrice = "§ePrice: §6" + shortNumber(finalPrice) + " Coins";
                        }
                        this.displayItem = new ItemStack(Items.BARRIER);
                        this.displayItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§cItem Data Not Found"));
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    this.isLoading = false;
                    this.displayItem = new ItemStack(Items.BARRIER);
                    this.displayItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4API Request Failed"));
                });
                e.printStackTrace();
            }
        });
    }
}