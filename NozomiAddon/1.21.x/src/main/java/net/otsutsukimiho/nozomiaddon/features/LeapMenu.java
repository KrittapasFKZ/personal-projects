package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeapMenu implements FeatureManager.Feature {
    private boolean enabled = false;

    public static ModeSetting sortMode = new ModeSetting("Sorting", "Odin Sorting", "A-Z Name", "A-Z Class", "Odin Sorting");
    public static BooleanSetting instantClose = new BooleanSetting("Instant Close", true);
    public static BooleanSetting colorStyle = new BooleanSetting("Class Color Style", false);
    public static BooleanSetting leapAnnounce = new BooleanSetting("Leap Announce", true);
    public static StringSetting leapMessage = new StringSetting("Leap Message", "Leaped to {name}!");
    public static FloatSetting scale = new FloatSetting("Render Scale", 1f, 0.5f, 4f, 0.05f);
    public static KeySetting quadrant1 = new KeySetting("Leap #1", 49);
    public static KeySetting quadrant2 = new KeySetting("Leap #2", 50);
    public static KeySetting quadrant3 = new KeySetting("Leap #3", 51);
    public static KeySetting quadrant4 = new KeySetting("Leap #4", 52);
    @Override
    public List<Settings> getSettings() {
        return List.of(sortMode, instantClose, colorStyle, leapAnnounce, leapMessage, scale, quadrant1, quadrant2, quadrant3, quadrant4);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.ENDER_PEARL);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private static final int BOX_WIDTH = 120;
    private static final int BOX_HEIGHT = 45;
    public static boolean isOpen = false;

    private static final float[] boxScales = {1.0f, 1.0f, 1.0f, 1.0f};

    @Override
    public void initClient() {
        ScreenEvents.AFTER_INIT.register((MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled || !DUNGEON.inDungeon) return;
            if (screen instanceof HandledScreen<?> handled) {
                String title = handled.getTitle().getString();

                if (title.equals("Spirit Leap") || title.equals("Teleport to Player")) {
                    isOpen = true;
                    for (int i = 0; i < 4; i++) boxScales[i] = 1.0f;

                    ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                        List<LeapPlayer> players = parseLeapPlayers(handled, client);
                        if (players.isEmpty()) return;

                        int width = client.getWindow().getScaledWidth();
                        int height = client.getWindow().getScaledHeight();

                        int halfW = width / 2;
                        int halfH = height / 2;
                        float baseScale = scale.getValue();

                        float tickDelta = client.getRenderTickCounter().getTickProgress(true);

                        drawContext.getMatrices().pushMatrix();

                        for (int i = 0; i < players.size() && i < 4; i++) {
                            LeapPlayer player = players.get(i);
                            if (player == null) continue;

                            int col = i % 2;
                            int row = i / 2;

                            int quadrantIndex = (row * 2) + col;

                            float scaledW = BOX_WIDTH * baseScale;
                            float scaledH = BOX_HEIGHT * baseScale;

                            int xOffset = (col == 0) ? (int)(-scaledW - 10) : 10;
                            int yOffset = (row == 0) ? (int)(-scaledH - 10) : 10;

                            int renderX = halfW + xOffset;
                            int renderY = halfH + yOffset;

                            boolean isHovered = ((col == 0) == (mouseX < halfW)) && ((row == 0) == (mouseY < halfH));

                            float targetAnimScale = isHovered ? 1.04f : 1.0f;
                            boxScales[quadrantIndex] = MathHelper.lerp(0.25f * tickDelta, boxScales[quadrantIndex], targetAnimScale);

                            float finalScale = baseScale * boxScales[quadrantIndex];

                            int boxColor = colorStyle.isEnabled() ? player.dClass.colorCode : new Color(170, 170, 170, 255).getRGB();

                            int a = (boxColor >> 24) & 0xFF;
                            int r = (int)(((boxColor >> 16) & 0xFF) * 0.6f);
                            int g = (int)(((boxColor >> 8) & 0xFF) * 0.6f);
                            int b = (int)((boxColor & 0xFF) * 0.6f);
                            int shadowColor = (a << 24) | (r << 16) | (g << 8) | b;

                            drawContext.getMatrices().pushMatrix();

                            float centerX = renderX + (scaledW / 2f);
                            float centerY = renderY + (scaledH / 2f);
                            drawContext.getMatrices().translate(centerX, centerY);
                            drawContext.getMatrices().scale(finalScale, finalScale);

                            float localX = -BOX_WIDTH / 2f;
                            float localY = -BOX_HEIGHT / 2f;

                            drawRoundedGradientRect(drawContext, (int)localX, (int)localY, boxColor, shadowColor);

                            int faceSize = 30;
                            int faceX = (int)localX + 8;
                            int faceY = (int)localY + (BOX_HEIGHT - faceSize) / 2;

                            if (player.skin != null) {
                                drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, player.skin, faceX, faceY, 8f, 8f, faceSize, faceSize, 8, 8, 64, 64);
                            }

                            int textX = faceX + faceSize + 8;
                            String playerName = "§f" + player.name;
                            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(playerName), textX, faceY + 4, 0xFFFFFFFF);
                            String className = colorStyle.isEnabled() ? "§7" + player.dClass.name.substring(2) : player.dClass.name;
                            String classText = player.isDead ? "§c§lDEAD" : className;
                            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(classText), textX, faceY + 16, 0xFFFFFF55);

                            drawContext.getMatrices().popMatrix();
                        }

                        drawContext.getMatrices().popMatrix();
                    });

                    ScreenMouseEvents.allowMouseClick(screen).register((screen2, click) -> {
                        if (click.button() != 0) return true;

                        List<LeapPlayer> players = parseLeapPlayers(handled, client);
                        if (players.isEmpty()) return true;

                        double mouseX = click.x();
                        double mouseY = click.y();
                        int halfW = client.getWindow().getScaledWidth() / 2;
                        int halfH = client.getWindow().getScaledHeight() / 2;

                        int quadrant = (mouseY >= halfH ? 2 : 0) + (mouseX >= halfW ? 1 : 0);

                        if (quadrant < players.size()) {
                            LeapPlayer target = players.get(quadrant);
                            if (target.isDead) {
                                if (client.player != null) client.player.sendMessage(Text.literal("§d§lNA §f§l» §cThis player is dead!"), false);
                                return false;
                            }

                            if (client.interactionManager != null && client.player != null) {
                                client.interactionManager.clickSlot(handled.getScreenHandler().syncId, target.slotIndex, 0, SlotActionType.PICKUP, client.player);
                                client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                                client.player.sendMessage(Text.literal("§d§lNA §f§l» §aLeaping to " + target.name + "!"), false);
                                if (leapAnnounce.isEnabled()) sendPartyMessage(target.name);

                                if (instantClose.isEnabled()) {
                                    client.player.closeHandledScreen();
                                    client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 1f, 1f);
                                    isOpen = false;
                                }
                            }
                            return false;
                        }

                        return false;
                    });
                    ScreenKeyboardEvents.allowKeyPress(screen).register((scr, key) -> {
                        int code = key.getKeycode();
                        int targetQuadrant = -1;

                        if (code == quadrant1.getCode()) targetQuadrant = 0;
                        else if (code == quadrant2.getCode()) targetQuadrant = 1;
                        else if (code == quadrant3.getCode()) targetQuadrant = 2;
                        else if (code == quadrant4.getCode()) targetQuadrant = 3;

                        if (targetQuadrant != -1) {
                            List<LeapPlayer> players = parseLeapPlayers(handled, client);
                            if (targetQuadrant < players.size()) {
                                LeapPlayer target = players.get(targetQuadrant);

                                if (target.isDead) {
                                    if (client.player != null) client.player.sendMessage(Text.literal("§d§lNA §f§l» §cThis player is dead!"), false);
                                    return false;
                                }

                                if (client.interactionManager != null && client.player != null) {
                                    client.interactionManager.clickSlot(handled.getScreenHandler().syncId, target.slotIndex, 0, SlotActionType.PICKUP, client.player);
                                    client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                                    client.player.sendMessage(Text.literal("§d§lNA §f§l» §aLeaping to " + target.name + "!"), false);
                                    if (leapAnnounce.isEnabled()) sendPartyMessage(target.name);

                                    if (instantClose.isEnabled()) {
                                        client.player.closeHandledScreen();
                                        client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 1f, 1f);
                                        isOpen = false;
                                    }
                                }
                                return false;
                            }
                        }

                        return true;
                    });
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isOpen) {
                if (!(client.currentScreen instanceof HandledScreen<?> handled) || (!handled.getTitle().getString().equals("Spirit Leap") && !handled.getTitle().getString().equals("Teleport to Player"))) {
                    isOpen = false;
                }
            }
        });
    }

    private void sendPartyMessage(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (client.player.networkHandler == null) return;

        String text = LeapMenu.leapMessage.getValue();

        if (text.contains("{name}")) {
            text = text.replace("{name}", playerName);
        }

        client.player.networkHandler.sendChatCommand("pc " + text);
    }

    private static void drawRoundedGradientRect(DrawContext context, int x, int y, int colorTop, int colorBottom) {
        context.fillGradient(x + 2, y, x + LeapMenu.BOX_WIDTH - 2, y + LeapMenu.BOX_HEIGHT, colorTop, colorBottom);
        context.fillGradient(x, y + 2, x + 2, y + LeapMenu.BOX_HEIGHT - 2, colorTop, colorBottom);
        context.fillGradient(x + LeapMenu.BOX_WIDTH - 2, y + 2, x + LeapMenu.BOX_WIDTH, y + LeapMenu.BOX_HEIGHT - 2, colorTop, colorBottom);

        context.fill(x + 1, y + 1, x + 2, y + 2, colorTop);
        context.fill(x + LeapMenu.BOX_WIDTH - 2, y + 1, x + LeapMenu.BOX_WIDTH - 1, y + 2, colorTop);
        context.fill(x + 1, y + LeapMenu.BOX_HEIGHT - 2, x + 2, y + LeapMenu.BOX_HEIGHT - 1, colorBottom);
        context.fill(x + LeapMenu.BOX_WIDTH - 2, y + LeapMenu.BOX_HEIGHT - 2, x + LeapMenu.BOX_WIDTH - 1, y + LeapMenu.BOX_HEIGHT - 1, colorBottom);
    }

    private List<LeapPlayer> parseLeapPlayers(HandledScreen<?> handled, MinecraftClient client) {
        List<LeapPlayer> players = new ArrayList<>();
        var slots = handled.getScreenHandler().slots;

        DungeonClass myClass = DungeonClass.Unknown;
        String myRawName = client.player != null ? client.player.getName().getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "").trim() : "";
        String myPureName = myRawName.replaceAll("[^a-zA-Z0-9_]", "");

        if (client.player != null && client.player.networkHandler != null) {
            for (PlayerListEntry entry : client.player.networkHandler.getPlayerList()) {
                if (entry.getDisplayName() != null) {
                    String tabName = entry.getDisplayName().getString();
                    String cleanTab = tabName.replaceAll("(?i)[§&][0-9A-FK-OR]", "");

                    if (cleanTab.contains(myPureName)) {
                        if (cleanTab.contains("(Archer")) myClass = DungeonClass.Archer;
                        else if (cleanTab.contains("(Berserk")) myClass = DungeonClass.Berserk;
                        else if (cleanTab.contains("(Healer")) myClass = DungeonClass.Healer;
                        else if (cleanTab.contains("(Mage")) myClass = DungeonClass.Mage;
                        else if (cleanTab.contains("(Tank")) myClass = DungeonClass.Tank;
                    }
                }
            }
        }

        for (int i = 11; i <= 16; i++) {
            if (i >= slots.size()) break;
            ItemStack stack = slots.get(i).getStack();

            if (!stack.isEmpty() && stack.getItem() == Items.PLAYER_HEAD) {
                String rawName = stack.getName().getString();
                String cleanName = rawName.replaceAll("(?i)[§&][0-9A-FK-OR]", "").trim();
                String pureName = cleanName.replaceAll("[^a-zA-Z0-9_]", "");

                DungeonClass dClass = DungeonClass.Unknown;
                boolean isDead = false;
                Identifier skin = null;

                if (client.player != null && client.player.networkHandler != null) {
                    for (PlayerListEntry entry : client.player.networkHandler.getPlayerList()) {
                        if (entry.getDisplayName() != null) {
                            String tabName = entry.getDisplayName().getString();
                            String cleanTab = tabName.replaceAll("(?i)[§&][0-9A-FK-OR]", "");

                            if (cleanTab.contains(pureName)) {
                                skin = entry.getSkinTextures().body().id();

                                if (cleanTab.contains("DEAD")) isDead = true;
                                if (cleanTab.contains("(Archer")) dClass = DungeonClass.Archer;
                                else if (cleanTab.contains("(Berserk")) dClass = DungeonClass.Berserk;
                                else if (cleanTab.contains("(Healer")) dClass = DungeonClass.Healer;
                                else if (cleanTab.contains("(Mage")) dClass = DungeonClass.Mage;
                                else if (cleanTab.contains("(Tank")) dClass = DungeonClass.Tank;
                                break;
                            }
                        }
                    }
                }

                players.add(new LeapPlayer(cleanName, dClass, isDead, skin, stack, i));
            }
        }

        String currentMode = sortMode.getMode();
        if (currentMode.equals("Odin Sorting")) {
            List<DungeonClass> odinOrder = getOdinPriorityList(myClass);

            players.sort(Comparator.comparingInt((LeapPlayer p) -> {
                int index = odinOrder.indexOf(p.dClass);
                return index == -1 ? 99 : index;
            }).thenComparing(p -> p.name.toLowerCase()));

        } else if (currentMode.equals("A-Z Class")) {
            players.sort(Comparator.comparing((LeapPlayer p) -> p.dClass.name).thenComparing(p -> p.name.toLowerCase()));
        } else {
            players.sort((p1, p2) -> p1.name.compareToIgnoreCase(p2.name));
        }

        return players;
    }

    private static List<DungeonClass> getOdinPriorityList(DungeonClass myClass) {
        if (myClass == DungeonClass.Berserk) return List.of(DungeonClass.Archer, DungeonClass.Healer, DungeonClass.Mage, DungeonClass.Tank);
        if (myClass == DungeonClass.Archer) return List.of(DungeonClass.Mage, DungeonClass.Berserk, DungeonClass.Healer, DungeonClass.Tank);
        if (myClass == DungeonClass.Mage) return List.of(DungeonClass.Archer, DungeonClass.Berserk, DungeonClass.Healer, DungeonClass.Tank);
        if (myClass == DungeonClass.Healer) return List.of(DungeonClass.Archer, DungeonClass.Berserk, DungeonClass.Mage, DungeonClass.Tank);
        if (myClass == DungeonClass.Tank) return List.of(DungeonClass.Archer, DungeonClass.Berserk, DungeonClass.Healer, DungeonClass.Mage);

        return List.of(DungeonClass.Archer, DungeonClass.Berserk, DungeonClass.Mage, DungeonClass.Healer, DungeonClass.Tank);
    }

    private record LeapPlayer(String name, DungeonClass dClass, boolean isDead, net.minecraft.util.Identifier skin, ItemStack head, int slotIndex) {}

    private enum DungeonClass {
        Archer("§6Archer", new Color(255, 170, 0, 255).getRGB()),
        Berserk("§cBerserk", new Color(255, 85, 85, 255).getRGB()),
        Healer("§dHealer", new Color(255, 85, 255, 255).getRGB()),
        Mage("§bMage", new Color(85, 255, 255, 255).getRGB()),
        Tank("§aTank", new Color(0, 170, 0, 255).getRGB()),
        Unknown("§7Unknown", new Color(170, 170, 170, 255).getRGB());

        public final String name;
        public final int colorCode;

        DungeonClass(String name, int colorCode) {
            this.name = name;
            this.colorCode = colorCode;
        }
    }
}