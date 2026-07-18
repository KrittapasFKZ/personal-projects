package net.otsutsukimiho.nozomiaddon.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.otsutsukimiho.nozomiaddon.features.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalScreen {
    private final MinecraftClient client;
    private ScreenHandler handler;
    private final TerminalsSolver.TerminalType type;
    private final int chestSize;
    private final String windowTitle;

    private final Map<Integer, Integer> slotStates = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> rubixClicks = new ConcurrentHashMap<>();
    private final Queue<QueuedClick> packetQueue = new ConcurrentLinkedQueue<>();
    private final Map<Integer, Long> lastMelodyClickTime = new ConcurrentHashMap<>();

    private static final Pattern STARTS_WITH_PATTERN = Pattern.compile("^What starts with: '(\\w)'\\?$");

    private static final int GRID_COLS = 9;
    private static final int BOX_SIZE = 30;
    private static final int GAP = 4;

    private final long openedAt;
    private long lastDragTime = 0;

    private int melodyTargetCol = -1;
    private int melodyNoteRow = -1;
    private int melodyNoteCol = -1;
    private long lastAutoMelodyTime = 0;
    private int lastAutoMelodySlot = -1;
    private static final List<Integer> MELODY_BUTTONS = List.of(16, 25, 34, 43);

    private static int TICK = 0;
    private static boolean ALREADY_FIRST = false;
    private static long FIRST_CLICK = 0;
    private static boolean canClick = false;

    public TerminalScreen(TerminalsSolver.TerminalType type, HandledScreen<?> parent) {
        this.client = MinecraftClient.getInstance();
        this.type = type;
        this.handler = parent.getScreenHandler();
        this.chestSize = handler.slots.size() - 36;
        this.windowTitle = parent.getTitle().getString();
        this.openedAt = System.currentTimeMillis();

        canClick = false;
        TICK = 0;
        FIRST_CLICK = 0;
        ALREADY_FIRST = false;

        if (client.player != null) {
            client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }

        scanInventory();
    }

    public static void setAllowClick(boolean allow) {
        canClick = allow;
    }

    public void updateHandler(HandledScreen<?> newScreen) {
        this.handler = newScreen.getScreenHandler();
        canClick = true;
    }

    private void drawPanel(DrawContext context, int x, int y, int color) {
        String style = TerminalsSolver.panelStyle.getMode();
        if (style.equals("Circle")) {
            float r = BOX_SIZE / 2.0f;
            float cx = x + r;
            for (int i = 0; i < BOX_SIZE; i++) {
                float dy = i - r + 0.5f;
                int dx = (int) Math.round(Math.sqrt(r * r - dy * dy));
                context.fill((int)(cx - dx), y + i, (int)(cx + dx), y + i + 1, color);
            }
        } else if (style.equals("Smooth Square")) {
            context.fill(x + 2, y, x + BOX_SIZE - 2, y + 1, color);
            context.fill(x + 1, y + 1, x + BOX_SIZE - 1, y + 2, color);
            context.fill(x, y + 2, x + BOX_SIZE, y + BOX_SIZE - 2, color);
            context.fill(x + 1, y + BOX_SIZE - 2, x + BOX_SIZE - 1, y + BOX_SIZE - 1, color);
            context.fill(x + 2, y + BOX_SIZE - 1, x + BOX_SIZE - 2, y + BOX_SIZE, color);
        } else {
            context.fill(x, y, x + BOX_SIZE, y + BOX_SIZE, color);
        }
    }

    public void tick() {
        if (type == TerminalsSolver.TerminalType.Melody || TICK <= 5) {
            scanInventory();
            handleAutoMelody();
        }
        if (TICK <= 5) TICK++;
        processPacketQueue();
        if (client.player != null && TerminalsSolver.debug.isEnabled()) {
            long needToClick = 0;
            long pendingClick = 0;
            if (type == TerminalsSolver.TerminalType.Rubix) {
                needToClick = rubixClicks.values().stream().filter(clicks -> Math.abs(clicks) > 0).count();
            } else {
                needToClick = slotStates.values().stream().filter(state -> state == 1).count();
                pendingClick = slotStates.values().stream().filter(state -> state == 2).count();
            }
            int queueSize = packetQueue.size();
            int currentSyncId = handler.syncId;
            String debugMsg = String.format("§eNeed: §c%d §8| §ePending: §6%d §8| §eQueue: §b%d §8| §eSync: §d%d §8| §eCanClick: §f%s", needToClick, pendingClick, queueSize, currentSyncId, canClick);
            client.player.sendMessage(Text.literal(debugMsg), true);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        float scale = TerminalsSolver.uiSize.getValue();

        int rows = (int) Math.ceil((double) chestSize / (GRID_COLS - 1));
        int gridWidth = (GRID_COLS * BOX_SIZE) + ((GRID_COLS - 1) * GAP);
        int gridHeight = (rows * BOX_SIZE) + ((rows - 5) * GAP);

        float scaledGridWidth = gridWidth * scale;
        float scaledGridHeight = gridHeight * scale;

        float startX = (width - scaledGridWidth) / 2;
        float startY = (height - scaledGridHeight) / 2;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(startX, startY);
        context.getMatrices().scale(scale, scale);

        int padding = 1;
        int headerSpace = 5;

        int bgX = -padding;
        int bgY = -padding - headerSpace;
        int bgW = gridWidth + (padding * 2);
        int bgH = gridHeight + (padding * 2) + headerSpace;

        int baseColor = TerminalsSolver.bgColor.getRGB();
        int alpha = (int) (Math.clamp(TerminalsSolver.bgOpacity.getValue(), 0f, 1f) * 255);
        int finalBgColor = (alpha << 24) | (baseColor & 0x00FFFFFF);

        context.fill(bgX, bgY, bgX + bgW, bgY + bgH, finalBgColor);

        if (!TerminalsSolver.hideTitle.isEnabled()) {
            context.drawCenteredTextWithShadow(client.textRenderer, "§d§lNA §f§l» §a" + type.name(), gridWidth / 2, bgY + 5, 0xFFFFFFFF);
        }

        if (type == TerminalsSolver.TerminalType.Rubix) {
            for (int i = 0; i < chestSize; i++) {
                int clicks = rubixClicks.getOrDefault(i, 0);
                if (clicks == 0) continue;

                int col = i % GRID_COLS;
                int row = i / GRID_COLS;

                int x = (col * (BOX_SIZE + GAP));
                int y = (row * (BOX_SIZE + GAP));

                boolean positive = clicks > 0;
                int color = positive ? TerminalsSolver.color1.getRGB() : TerminalsSolver.colorNegative.getRGB();

                drawPanel(context, x, y, color);
                context.drawCenteredTextWithShadow(client.textRenderer, String.valueOf(clicks), x + BOX_SIZE / 2, y + BOX_SIZE / 2 - 4, 0xFFFFFFFF);
            }
        } else {
            if (type == TerminalsSolver.TerminalType.Melody && TerminalsSolver.melodyTerminal.isEnabled()) {
                for (int i = 0; i < chestSize; i++) {
                    int col = i % 9;
                    int row = i / 9;
                    int x = col * (BOX_SIZE + GAP);
                    int y = row * (BOX_SIZE + GAP);

                    if (col == melodyTargetCol && row >= 1 && row <= 4) {
                        drawPanel(context, x, y, 0x33FFFFFF);
                    }

                    if (MELODY_BUTTONS.contains(i)) {
                        int state = slotStates.getOrDefault(i, 0);
                        int color = (state == 1) ? TerminalsSolver.color1.getRGB() : TerminalsSolver.colorNegative.getRGB();
                        drawPanel(context, x, y, color);
                    }

                    if (col == melodyNoteCol && row == melodyNoteRow) {
                        drawPanel(context, x, y, TerminalsSolver.melodyNote.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < chestSize; i++) {
                    int state = slotStates.getOrDefault(i, 0);

                    if (state == 0 || state == 2) continue;

                    int col = i % GRID_COLS;
                    int row = i / GRID_COLS;

                    int x = col * (BOX_SIZE + GAP);
                    int y = row * (BOX_SIZE + GAP);

                    int color = TerminalsSolver.color1.getRGB();
                    if (state == 3) color = TerminalsSolver.color2.getRGB();
                    else if (state == 4) color = TerminalsSolver.color3.getRGB();

                    drawPanel(context, x, y, color);

                    if (type == TerminalsSolver.TerminalType.ClickInOrder && TerminalsSolver.showOrderNumber.isEnabled()) {
                        int count = handler.slots.get(i).getStack().getCount();
                        context.drawCenteredTextWithShadow(client.textRenderer, String.valueOf(count), x + BOX_SIZE / 2, y + BOX_SIZE / 2 - 4, 0xFFFFFFFF);
                    }
                }
            }
        }

        context.getMatrices().popMatrix();
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0 && click.button() != 1) return false;
        if (TerminalsSolver.clickTerm.is("Drag") && type != TerminalsSolver.TerminalType.Melody) return false;
        if (System.currentTimeMillis() - openedAt < TerminalsSolver.firstClickProtect.getValue()) return false;

        int slot = getSlotAt(click.x(), click.y());
        if (slot != -1) {
            return handleClick(slot, click.button());
        }
        return false;
    }

    public boolean mouseDragged(Click click, double dragX, double dragY) {
        if (click.button() != 0 && click.button() != 1) return false;
        if (type == TerminalsSolver.TerminalType.Melody) return true;
        if (TerminalsSolver.clickTerm.is("Click")) return false;
        if (System.currentTimeMillis() - openedAt < TerminalsSolver.firstClickProtect.getValue()) return false;

        long now = System.currentTimeMillis();
        if (now - lastDragTime < TerminalsSolver.dragDelay.getValue()) {
            return false;
        }

        int slot = getSlotAt(click.x(), click.y());

        if (slot != -1) {
            boolean clicked = handleClick(slot, click.button());
            if (clicked) {
                lastDragTime = now;
                return true;
            }
        }
        return false;
    }

    private int getSlotAt(double mouseX, double mouseY) {
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        float scale = TerminalsSolver.uiSize.getValue();

        int rows = (int) Math.ceil((double) chestSize / (GRID_COLS - 1));
        int gridWidth = (GRID_COLS * BOX_SIZE) + ((GRID_COLS - 1) * GAP);
        int gridHeight = (rows * BOX_SIZE) + ((rows - 5) * GAP);

        float scaledGridWidth = gridWidth * scale;
        float scaledGridHeight = gridHeight * scale;

        float startX = (width - scaledGridWidth) / 2f;
        float startY = (height - scaledGridHeight) / 2f;

        double localMouseX = (mouseX - startX) / scale;
        double localMouseY = (mouseY - startY) / scale;

        for (int i = 0; i < chestSize; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            int x = col * (BOX_SIZE + GAP);
            int y = row * (BOX_SIZE + GAP);

            if (localMouseX >= x && localMouseX < x + BOX_SIZE && localMouseY >= y && localMouseY < y + BOX_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void handleClickMelody(int i, int button) {
        if (TerminalsSolver.autoMelody.isEnabled()) return;
        int state = slotStates.getOrDefault(i, 0);
        if (TerminalsSolver.melodyBlockWrongClick.isEnabled() && state != 1) return;
        long now = System.currentTimeMillis();
        long lastClick = lastMelodyClickTime.getOrDefault(i, 0L);
        if (now - lastClick > TerminalsSolver.melodyClickDelay.getValue()) {
            lastMelodyClickTime.put(i, now);
            if (client != null && client.interactionManager != null && client.player != null) {
                client.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, client.player);
                client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
            TerminalsSolver.clickSound.playTestSound();
        }
    }

    private boolean handleClick(int i, int button) {
        if (type == TerminalsSolver.TerminalType.Rubix) {
            int clicksNeeded = rubixClicks.getOrDefault(i, 0);
            if (clicksNeeded == 0) return false;

            int buttonToSend = (clicksNeeded > 0) ? 0 : 1;
            int newCount = (clicksNeeded > 0) ? clicksNeeded - 1 : clicksNeeded + 1;
            rubixClicks.put(i, newCount);

            queueClick(i, buttonToSend);
            TerminalsSolver.clickSound.playTestSound();
            return true;
        } else {
            if (type == TerminalsSolver.TerminalType.Melody && TerminalsSolver.melodyTerminal.isEnabled()) {
                if (MELODY_BUTTONS.contains(i)) {
                    handleClickMelody(i, button);
                    return true;
                }
                return true;
            } else {
                int state = slotStates.getOrDefault(i, 0);
                if (state == 1) {
                    queueClick(i, 0);
                    TerminalsSolver.clickSound.playTestSound();
                    return true;
                }
            }
        }
        return false;
    }

    private static class QueuedClick {
        int slotId; int button; int delayTicks;
        QueuedClick(int slotId, int button, int delayTicks) { this.slotId = slotId; this.button = button; this.delayTicks = delayTicks; }
    }

    private void queueClick(int slotId, int button) {
        if (type != TerminalsSolver.TerminalType.Rubix) {
            slotStates.put(slotId, 2);
        }

        int maxTickDelay = TerminalsSolver.clickDelay.getValue();
        int randomDelay = new Random().nextInt(maxTickDelay + 1);
        packetQueue.add(new QueuedClick(slotId, button, randomDelay));

        if (type == TerminalsSolver.TerminalType.ClickInOrder) {
            recalculateClickInOrder();
        }
    }

    private void processPacketQueue() {
        if (client == null || client.interactionManager == null || client.player == null) return;
        if (System.currentTimeMillis() - openedAt < TerminalsSolver.firstClickProtect.getValue()) return;

        if (ALREADY_FIRST && !packetQueue.isEmpty() && System.currentTimeMillis() - FIRST_CLICK > TerminalsSolver.resyncDelay.getValue()) {
            canClick = true;
            FIRST_CLICK = System.currentTimeMillis();
            packetQueue.clear();
            if (type != TerminalsSolver.TerminalType.Rubix) {
                for (Map.Entry<Integer, Integer> entry : slotStates.entrySet()) {
                    if (entry.getValue() == 2) {
                        slotStates.put(entry.getKey(), 0);
                    }
                }
            }
            scanInventory();
            TICK = 0;

            if (client.player != null) {
                client.player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1f, 2f);
            }
            return;
        }

        if (packetQueue.isEmpty()) return;

        QueuedClick next = packetQueue.peek();
        if (next == null) return;

        if (next.delayTicks > 0) {
            next.delayTicks--;
            return;
        }

        if (canClick) {
            packetQueue.poll();
            canClick = false;
            int idToSend = handler.syncId;

            FIRST_CLICK = System.currentTimeMillis();
            if (!ALREADY_FIRST) ALREADY_FIRST = true;

            client.interactionManager.clickSlot(idToSend, next.slotId, next.button, SlotActionType.PICKUP, client.player);
            if (client.player != null) client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }
    }

    private void scanInventory() {
        if (type == TerminalsSolver.TerminalType.ClickInOrder) {
            recalculateClickInOrder();
            return;
        } else if (type == TerminalsSolver.TerminalType.Rubix) {
            solveRubix();
            return;
        } else if (type == TerminalsSolver.TerminalType.Melody && TerminalsSolver.melodyTerminal.isEnabled()) {
            scanMelody();
            return;
        }

        String target = "";
        if (type == TerminalsSolver.TerminalType.SelectAll && windowTitle.length() > 22) {
            target = windowTitle.substring(15, windowTitle.length() - 7).toLowerCase();
        } else if (type == TerminalsSolver.TerminalType.StartsWith) {
            Matcher matcher = STARTS_WITH_PATTERN.matcher(windowTitle);
            if (matcher.find()) {
                target = matcher.group(1).toLowerCase();
            }
        }

        for (int i = 0; i < chestSize; i++) {
            if (slotStates.getOrDefault(i, 0) == 2) continue;

            ItemStack stack = handler.slots.get(i).getStack();
            boolean correct = false;

            if (type == TerminalsSolver.TerminalType.CorrectPanes) {
                correct = isStainedGlassPane(stack) && getGlassColor(stack) == Formatting.RED;
            } else if (!target.isEmpty() && !stack.isEmpty() && !isGreyBackground(stack) && !stack.hasEnchantments()) {
                String name;
                if (type == TerminalsSolver.TerminalType.StartsWith) {
                    name = cleanItemName(stack.getName().getString());
                } else {
                    name = cleanAndFixItemName(stack.getName().getString());
                }
                if (name.startsWith(target)) correct = true;
            }

            int currentState = slotStates.getOrDefault(i, 0);
            if (currentState == 2) {
                if (!correct) {
                    slotStates.put(i, 0);
                }
                continue;
            }

            slotStates.put(i, correct ? 1 : 0);
        }
    }

    private void handleAutoMelody() {
        if (type != TerminalsSolver.TerminalType.Melody) return;
        if (!TerminalsSolver.autoMelody.isEnabled()) return;
        if (System.currentTimeMillis() - openedAt < TerminalsSolver.firstClickProtect.getValue()) return;

        if (melodyNoteRow != -1 && melodyNoteCol == melodyTargetCol) {
            int targetButtonSlot = (melodyNoteRow * 9) + 7;

            long now = System.currentTimeMillis();
            if (lastAutoMelodySlot == targetButtonSlot && now - lastAutoMelodyTime < 500) return;
            if (now - lastAutoMelodyTime < TerminalsSolver.melodyClickDelay.getValue()) return;

            lastAutoMelodyTime = now;
            lastAutoMelodySlot = targetButtonSlot;

            if (client != null && client.interactionManager != null && client.player != null) {
                client.interactionManager.clickSlot(handler.syncId, targetButtonSlot, 0, SlotActionType.PICKUP, client.player);
                client.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            }
            TerminalsSolver.clickSound.playTestSound();

            long delay = 50 + (long)((Math.random() - 0.5) * 60);
            if (TerminalsSolver.trySkipMelody.isEnabled() && melodyNoteRow <= 3) {
                sendMelodySkipPacket(targetButtonSlot + 9, delay);
            }
        } else {
            lastAutoMelodySlot = -1;
        }
    }

    private void sendMelodySkipPacket(int slot, long delayMs) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {}

            if (client != null && client.player != null) {
                client.execute(() -> {
                    if (client.currentScreen != null && client.interactionManager != null && client.currentScreen.getTitle().getString().contains("Click the button on time!")) {
                        client.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, client.player);
                    }
                });
            }
        });
    }

    private void recalculateClickInOrder() {
        List<Integer> redIndices = new ArrayList<>();
        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = handler.slots.get(i).getStack();
            if (slotStates.getOrDefault(i, 0) == 2) continue;

            if (isStainedGlassPane(stack) && getGlassColor(stack) == Formatting.RED) {
                redIndices.add(i);
            }

            if (slotStates.getOrDefault(i, 0) != 2) {
                if (!redIndices.contains(i)) {
                    slotStates.put(i, 0);
                }
            }
        }

        redIndices.sort(Comparator.comparingInt(i -> handler.slots.get(i).getStack().getCount()));

        for (int k = 0; k < redIndices.size(); k++) {
            int i = redIndices.get(k);
            if (slotStates.getOrDefault(i, 0) == 2) continue;
            if (k == 0) slotStates.put(i, 1);
            else if (k == 1) slotStates.put(i, 3);
            else if (k == 2) slotStates.put(i, 4);
            else slotStates.put(i, 0);
        }
    }

    private void scanMelody() {
        melodyTargetCol = -1;
        melodyNoteRow = -1;
        melodyNoteCol = -1;

        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = handler.slots.get(i).getStack();
            if (stack.isEmpty()) continue;

            Formatting color = getGlassColor(stack);

            if (color == Formatting.LIGHT_PURPLE || color == Formatting.DARK_PURPLE) {
                melodyTargetCol = i % 9;
            }

            if (color == Formatting.GREEN) {
                if (i % 9 != 7) {
                    melodyNoteRow = i / 9;
                    melodyNoteCol = i % 9;
                }
            }
        }

        if (melodyNoteRow != -1 && melodyNoteCol == melodyTargetCol) {
            int targetButtonSlot = (melodyNoteRow * 9) + 7;
            slotStates.put(targetButtonSlot, 1);
        } else {
            for (int slot : MELODY_BUTTONS) {
                slotStates.put(slot, 0);
            }
        }
    }

    private void solveRubix() {
        rubixClicks.clear();

        List<Integer> panes = new ArrayList<>();
        for (int i = 0; i < chestSize; i++) {
            ItemStack stack = handler.slots.get(i).getStack();
            if (isStainedGlassPane(stack) && getGlassColor(stack) != Formatting.BLACK) {
                panes.add(i);
            }
        }

        if (panes.isEmpty()) return;

        Map<Integer, Integer> bestMoves = new HashMap<>();
        int bestTotalCost = Integer.MAX_VALUE;

        for (Formatting target : RUBIX_ORDER) {
            Map<Integer, Integer> currentMoves = new HashMap<>();
            int currentTotalCost = 0;
            int targetIndex = RUBIX_ORDER.indexOf(target);

            for (int slot : panes) {
                ItemStack stack = handler.slots.get(slot).getStack();
                int currentIndex = RUBIX_ORDER.indexOf(getGlassColor(stack));
                if (currentIndex == -1) continue;

                int diff = targetIndex - currentIndex;

                int leftClicks = (diff + 5) % 5;
                int rightClicks = (5 - leftClicks) % 5;

                if (leftClicks <= rightClicks) {
                    currentMoves.put(slot, leftClicks);
                    currentTotalCost += leftClicks;
                } else {
                    currentMoves.put(slot, -rightClicks);
                    currentTotalCost += rightClicks;
                }
            }

            if (currentTotalCost < bestTotalCost) {
                bestTotalCost = currentTotalCost;
                bestMoves = new HashMap<>(currentMoves);
            }
        }

        rubixClicks.putAll(bestMoves);
    }

    private static final List<Formatting> RUBIX_ORDER = List.of(
            Formatting.GOLD,
            Formatting.YELLOW,
            Formatting.GREEN,
            Formatting.BLUE,
            Formatting.RED
    );
    private static String cleanItemName(String original) {
        return original.toLowerCase().replaceAll("§.", "").trim();
    }
    private static String cleanAndFixItemName(String original) {
        String name = original.toLowerCase().replaceAll("§.", "").trim();
        if (name.contains("light gray")) return name.replace("light gray", "silver");
        if (name.contains("wool")) return name.replace("wool", "white");
        if (name.contains("bone")) return name.replace("bone", "white");
        if (name.contains("ink") && !name.contains("pink")) return name.replace("ink", "black");
        if (name.contains("lapis")) return name.replace("lapis", "blue");
        if (name.contains("cocoa")) return name.replace("cocoa", "brown");
        if (name.contains("dandelion")) return name.replace("dandelion", "yellow");
        if (name.contains("rose")) return name.replace("rose", "red");
        if (name.contains("cactus")) return name.replace("cactus", "green");
        return name;
    }
    private static boolean isStainedGlassPane(ItemStack stack) {
        return stack.getItem().getTranslationKey().contains("stained_glass_pane");
    }
    private static boolean isGreyBackground(ItemStack stack) {
        return isStainedGlassPane(stack) && getGlassColor(stack) == Formatting.GRAY;
    }
    private static Formatting getGlassColor(ItemStack stack) {
        String key = stack.getItem().getTranslationKey();
        if (key.contains("red")) return Formatting.RED;
        if (key.contains("orange")) return Formatting.GOLD;
        if (key.contains("green") || key.contains("lime")) return Formatting.GREEN;
        if (key.contains("gray")) return Formatting.GRAY;
        if (key.contains("black")) return Formatting.BLACK;
        if (key.contains("blue") || key.contains("light_blue") || key.contains("cyan")) return Formatting.BLUE;
        if (key.contains("yellow")) return Formatting.YELLOW;
        if (key.contains("magenta") || key.contains("pink")) return Formatting.LIGHT_PURPLE;
        if (key.contains("purple")) return Formatting.DARK_PURPLE;
        return Formatting.WHITE;
    }
}