package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import net.otsutsukimiho.nozomiaddon.gui.DraggableHudElement;
import net.otsutsukimiho.nozomiaddon.gui.EditHudScreen;
import net.otsutsukimiho.nozomiaddon.mixin.HandledScreenAccessor;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyFinder extends DraggableHudElement implements FeatureManager.Feature {
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PartyFinder() {
        super("PartyFinder", 10, 10, 100, 10, 10);
    }

    private volatile boolean enabled = false;
    private String markClass = "None";
    private String findingClass = "Healer";
    private int partiesTotal = 0;
    private int partiesNum = 0;
    private String dungeonType = "???";
    private String dungeonFloor = "???";
    private boolean isPartyFinder = false;
    private final Map<Integer, String> highlightedSlots = new java.util.HashMap<>();
    private final Map<Integer, String> playerSlots = new java.util.HashMap<>();

    Pattern pattern_partyMembers = Pattern.compile("Party Members \\((\\d+)\\)");
    Pattern pattern_pfJoin = Pattern.compile("^Party Finder > (.+) joined the dungeon group! \\(([a-zA-Z]+) Level (\\d+)\\)$");
    Pattern pattern_partyJoin = Pattern.compile("^(.+) joined the party\\.$");
    Pattern pattern_partyLeave = Pattern.compile("^(.+) has (?:left|been removed from) the party\\.$");
    private boolean partyQueued = false;
    private long queuedTime = -1;
    private boolean debounce_partyMembers = false;
    private int partyAmount = 0;
    private int fullSoundPlaysLeft = 0;
    private int fullSoundTickDelay = 0;

    private static int partyAmount_Healer = 0;
    private static int partyAmount_Tank = 0;
    private static int partyAmount_Archer = 0;
    private static int partyAmount_Berserk = 0;
    private static int partyAmount_Mage = 0;

    public static ModeSetting highlightMode = new ModeSetting("Highlight Style", "Over Item", "Under Item", "Over Item");
    public static BooleanSetting queueAddon = new BooleanSetting("QueueAddon", true);
    public static BooleanSetting queueAddonShowTitle = new BooleanSetting("QueueAddon alertTitle", true);
    public static BooleanSetting queueTimerHud = new BooleanSetting("QueueAddon TimerHUD", true);
    public static BooleanSetting showClassMissingAmount = new BooleanSetting("Show Class Missing Amount", false);
    public static SoundSetting customSound1 = new SoundSetting("Queued Sound", "minecraft:block.piston.extend", 1.0f, 1.0f);
    public static SoundSetting customSound2 = new SoundSetting("Dequeue Sound", "minecraft:block.piston.contract", 1.0f, 1.0f);
    public static SoundSetting customSound3 = new SoundSetting("Full Sound", "minecraft:block.note_block.pling", 1.0f, 2.0f);
    public static SoundSetting customSound4 = new SoundSetting("Join Sound", "minecraft:block.note_block.bell", 1.0f, 1.0f);
    public static SoundSetting customSound5 = new SoundSetting("Left Sound", "minecraft:block.note_block.snare", 1.0f, 1.0f);
    @Override
    public List<Settings> getSettings() {
        return List.of(highlightMode, queueAddon, queueAddonShowTitle, queueTimerHud, showClassMissingAmount, customSound1, customSound2, customSound3, customSound4, customSound5);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1ODcwMTg2Nzk1NzYsInByb2ZpbGVJZCI6IjJkYzc3YWU3OTQ2MzQ4MDI5NDI4MGM4NDIyNzRiNTY3IiwicHJvZmlsZU5hbWUiOiJzYWR5MDYxMCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI1Njg5NWI5NjU5ODk2YWQ2NDdmNTg1OTkyMzhhZjUzMmQ0NmRiOWMxYjAzODliOGJiZWI3MDk5OWRhYjMzZCJ9fX0=");
    }

    private String removeFormat(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "").trim();
    }

    private static final Map<String, String> CLASS_COLORS = Map.ofEntries(
            Map.entry("Healer", "§dHealer"),
            Map.entry("Tank", "§aTank"),
            Map.entry("Mage", "§bMage"),
            Map.entry("Archer", "§6Archer"),
            Map.entry("Berserk", "§cBerserk"),
            Map.entry("None", "§7None")
    );

    private static final Map<String, String> CLASS_MAP_DEVOVIAN = Map.ofEntries(
            Map.entry("Healer", "[H"),
            Map.entry("Tank", "[T"),
            Map.entry("Mage", "[M"),
            Map.entry("Archer", "[A"),
            Map.entry("Berserk", "[B"),
            Map.entry("None", "[??????????????????????????????????")
    );

    public static String getLevelColor(int level) {
        if (level >= 50) return "§4§l";
        if (level >= 45) return "§c";
        if (level >= 40) return "§6";
        if (level >= 35) return "§d";
        if (level >= 30) return "§9";
        if (level >= 25) return "§b";
        if (level >= 20) return "§2";
        if (level >= 15) return "§a";
        if (level >= 10) return "§e";
        if (level >= 5)  return "§f";
        return "§7";
    }

    public void initClient() {
        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            isPartyFinder = false;
            highlightedSlots.clear();
            playerSlots.clear();
            partiesTotal = 0;
            partiesNum = 0;

            if (screen instanceof AbstractContainerScreen<?> handled) {
                Component title = handled.getTitle();
                String titleStr = title.getString();

                ScreenEvents.afterExtract(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                    var handler = handled.getMenu();
                    var slots = handler.slots;

                    if (titleStr.contains("Party Finder") || titleStr.contains("Catacombs Gate")) {
                        boolean dungeonPF = false;
                        var slotFilter = slots.get(50);
                        var filterLore = slotFilter.getItem().getComponents().get(DataComponents.LORE);
                        if (filterLore != null && !filterLore.lines().isEmpty()) {
                            var filterLine = filterLore.lines().get(3).getString();
                            dungeonPF = filterLine.contains("Dungeon:");
                            dungeonType = filterLine.replace("Dungeon: ", "");
                            dungeonFloor = filterLore.lines().get(4).getString().replace("Floor: ", "");
                        }

                        if (titleStr.contains("Party Finder") && dungeonPF) {
                            isPartyFinder = true;
                            readParties(handled);

                            int mainX = (screen1.width / 2) - 215;
                            int mainY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() - 192) / 2 - 10;
                            int spacing = 10;

                            String fdungeonFloor;
                            if (dungeonFloor.contains("Entrance")) {
                                fdungeonFloor = "§aEntrance";
                            } else {
                                if (dungeonFloor.contains("Floor")) {
                                    dungeonFloor = dungeonFloor.replace("Floor ", "");
                                    fdungeonFloor = (dungeonType.equals("Master Mode The Catacombs") ? "§4§lM" : "§aF") + romanToInt(dungeonFloor);
                                } else {
                                    fdungeonFloor = "§bAny";
                                }
                            }

                            String[] mainLines = {
                                    "§e§lParty Highlight",
                                    " §fYour Class: " + CLASS_COLORS.get(markClass),
                                    " §fMark Missing: " + CLASS_COLORS.get(findingClass),
                                    " §fParty Amount: §e" + partiesNum + "§f/§a" + partiesTotal,
                                    " §fFloor: " + fdungeonFloor,
                                    "",
                                    "§e§lChange Mark"
                            };

                            for (int i = 0; i < mainLines.length; i++) {
                                if (!mainLines[i].isEmpty()) {
                                    drawContext.text(Minecraft.getInstance().font, Component.literal(mainLines[i]), mainX, mainY + (i * spacing), 0xFFFFFFFF, true);
                                }
                            }

                            String[] classes = {"Tank", "Healer", "Berserk", "Archer", "Mage"};
                            String[] colors = {"§a", "§d", "§c", "§6", "§b"};

                            int startY = mainY + 70;

                            for (int i = 0; i < classes.length; i++) {
                                String className = classes[i];
                                String color = colors[i];
                                String finalString;

                                int missingAmt = switch (className) {
                                    case "Healer" -> partyAmount_Healer;
                                    case "Tank" -> partyAmount_Tank;
                                    case "Mage" -> partyAmount_Mage;
                                    case "Archer" -> partyAmount_Archer;
                                    case "Berserk" -> partyAmount_Berserk;
                                    default -> 0;
                                };

                                String missingStr;
                                if (missingAmt >= 3) {
                                    missingStr = "§a" + missingAmt;
                                } else if (missingAmt >= 1) {
                                    missingStr = "§e" + missingAmt;
                                } else {
                                    missingStr = "§c" + missingAmt;
                                }

                                if (showClassMissingAmount.isEnabled()) {
                                    if (className.equals(findingClass)) {
                                        finalString = String.format(" §8(§f%s§8) %s§l> %s", missingStr, color, className);
                                    } else {
                                        finalString = String.format(" §8(§f%s§8) %s%s", missingStr, color, className);
                                    }
                                } else {
                                    if (className.equals(findingClass)) {
                                        finalString = String.format(" %s§l> %s", color, className);
                                    } else {
                                        finalString = String.format(" %s%s", color, className);
                                    }
                                }

                                drawContext.text(Minecraft.getInstance().font, Component.literal(finalString), mainX, startY + (i * 10), 0xFFFFFFFF, true);
                            }

                            if (screen1 instanceof AbstractContainerScreen<?> handledScreen) {
                                for (Map.Entry<Integer, String> entry : highlightedSlots.entrySet()) {
                                    int slotIndex = entry.getKey();

                                    highlightSlot(drawContext, handledScreen, slotIndex, 0x8055FF55);
                                }
                                for (Map.Entry<Integer, String> entry : playerSlots.entrySet()) {
                                    int slotIndex = entry.getKey();

                                    highlightSlot(drawContext, handledScreen, slotIndex, 0x8055FFFF);
                                }
                            }

                        } else {
                            if (titleStr.contains("Catacombs Gate")) {
                                var slotChest = slots.get(45);
                                var classLore = slotChest.getItem().getComponents().get(DataComponents.LORE);
                                if (classLore != null && !classLore.lines().isEmpty()) {
                                    var classLine = classLore.lines().get(2).getString();
                                    markClass = classLine.replace("Currently Selected: ", "");
                                }
                            }
                        }
                    }

                });

                ScreenMouseEvents.afterMouseClick(screen).register((screen2, context, consumed) -> {
                    if (!isPartyFinder) return false;
                    if (context.button() != 0) return false;

                    double mouseX = context.x();
                    double mouseY = context.y();

                    int mainX = (screen2.width / 2) - 215;
                    int mainY = (Minecraft.getInstance().getWindow().getGuiScaledHeight() - 192) / 2 - 10;
                    int startY = mainY + 70;

                    String[] classes = {"Tank", "Healer", "Berserk", "Archer", "Mage"};

                    for (int i = 0; i < classes.length; i++) {
                        int x2 = mainX + 60;
                        int y1 = startY + (i * 10);
                        int y2 = y1 + 10;

                        if (mouseX >= mainX && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                            findingClass = classes[i];
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(),1,1.0f);
                            }
                            readParties(handled);
                            return true;
                        }
                    }

                    return false;
                });

            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !enabled || !queueAddon.isEnabled()) return;

            if (fullSoundPlaysLeft > 0) {
                if (fullSoundTickDelay <= 0) {
                    customSound3.playTestSound();
                    fullSoundPlaysLeft--;
                    fullSoundTickDelay = 4;
                } else {
                    fullSoundTickDelay--;
                }
            }

            if (partyQueued) checkFullParty();
        });
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null || !enabled || !queueAddon.isEnabled()) return true;

            String msg = message.getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "");
            boolean hideMessage = false;
            String delistAlert = null;

            if (msg.contains("Party Finder > Your party has been queued in the dungeon finder") && !partyQueued) {
                partyQueued = true;
                queuedTime = System.currentTimeMillis();
                customSound1.playTestSound();
                client.player.connection.sendCommand("pl");
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aQueued to §dParty Finder§a!"));
                if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                    client.gui.setTitle(Component.literal("§a§lQueued!"));
                    client.gui.setSubtitle(Component.literal(" "));
                    client.gui.setTimes(0,30,5);
                }
                hideMessage = true;
            }

            if (partyQueued && msg.contains("Party Finder > Your dungeon group is full! Click here to warp to the dungeon!")) {
                checkFullParty();
                hideMessage = true;
            }

            Matcher matcher_partyLeave = pattern_partyLeave.matcher(msg);
            if (matcher_partyLeave.find() && matcher_partyLeave.group(1) != null && partyQueued) {
                String player_name = matcher_partyLeave.group(1);
                player_name = stripRank(player_name);
                partyAmount -= 1;
                customSound5.playTestSound();
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §e" + player_name + " §cleft! §e" + partyAmount + "§f/§a5"));
                if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                    client.gui.setTitle(Component.literal("§e§l" + partyAmount + "§f§l/§a§l5"));
                    client.gui.setSubtitle(Component.literal("§e" + player_name + " §cleft!"));
                    client.gui.setTimes(0,30,5);
                }
                hideMessage = true;
            }

            Matcher matcher_pfJoin = pattern_pfJoin.matcher(msg);
            boolean isPfJoin = matcher_pfJoin.find();
            if (isPfJoin && matcher_pfJoin.group(1) != null && matcher_pfJoin.group(2) != null) {
                if (partyQueued && partyAmount <= 4) {
                    String player_name = matcher_pfJoin.group(1).trim();
                    player_name = stripRank(player_name);
                    String class_name = matcher_pfJoin.group(2);
                    int class_level = Integer.parseInt(matcher_pfJoin.group(3));
                    String class_level_name = CLASS_COLORS.get(class_name) + " §8(" + getLevelColor(class_level) + class_level + "§8)";
                    partyAmount += 1;
                    customSound4.playTestSound();
                    client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §e" + player_name + " " + class_level_name + " §ajoined! §e" + partyAmount + "§f/§a5"));
                    if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                        client.gui.setTitle(Component.literal("§e§l" + partyAmount + "§f§l/§a§l5"));
                        client.gui.setSubtitle(Component.literal("§e" + player_name + " §ajoined!"));
                        client.gui.setTimes(0,30,5);
                    }
                } else if (!partyQueued) {
                    partyQueued = true;
                    queuedTime = System.currentTimeMillis();
                    customSound1.playTestSound();
                    String class_name = matcher_pfJoin.group(2);
                    int class_level = Integer.parseInt(matcher_pfJoin.group(3));
                    String class_level_name = CLASS_COLORS.get(class_name) + " §8(" + getLevelColor(class_level) + class_level + "§8)";
                    client.player.connection.sendCommand("pl");
                    client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aJoined §dParty Finder§a! " + class_level_name));
                    if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                        client.gui.setTitle(Component.literal("§a§lJoined!"));
                        client.gui.setSubtitle(Component.literal(" "));
                        client.gui.setTimes(0,30,5);
                    }
                }
                hideMessage = true;
            }

            Matcher matcher_partyJoin = pattern_partyJoin.matcher(msg);
            if (matcher_partyJoin.find() && matcher_partyJoin.group(1) != null && partyQueued && partyAmount <= 4) {
                String player_name = matcher_partyJoin.group(1);
                player_name = stripRank(player_name);
                partyAmount += 1;
                customSound4.playTestSound();
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §e" + player_name + " §ajoined! §e" + partyAmount + "§f/§a5"));
                if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                    client.gui.setTitle(Component.literal("§e§l" + partyAmount + "§f§l/§a§l5"));
                    client.gui.setSubtitle(Component.literal("§e" + player_name + " §ajoined!"));
                    client.gui.setTimes(0,30,5);
                }
                hideMessage = true;
            }

            Matcher matcher_partyMembers = pattern_partyMembers.matcher(msg);
            if (matcher_partyMembers.find() && matcher_partyMembers.group(1) != null && partyQueued && !debounce_partyMembers) {
                int amount = Integer.parseInt(matcher_partyMembers.group(1));
                debounce_partyMembers = true;
                partyAmount = amount;
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aCurrent Party Members: §e" + amount + "§f/§a5"));
                hideMessage = true;
            }

            if (msg.contains("You left the party.")) {
                delistAlert = "§cYou left the party!";
            }
            if (msg.contains("You have been kicked from the party by")) {
                delistAlert = "§cYou have been kicked!";
            }
            else if (msg.contains("Party Finder > Your group has been de-listed!")) {
                delistAlert = "§cDe-listed from §dParty Finder";
            }
            else if (msg.contains("The party was disbanded because all invites expired") || msg.contains("has disbanded the party!")) {
                delistAlert = "§cParty was disbanded!";
            }
            else if (msg.contains("Your group has been removed from the party finder")) {
                delistAlert = "§cParty has been removed from §dParty Finder";
            }
            if (delistAlert != null && partyQueued) {
                partyQueued = false;
                partyAmount = 0;
                debounce_partyMembers = false;
                customSound2.playTestSound();
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aQueued for §b" + getTime(System.currentTimeMillis() - queuedTime)));
                client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» " + delistAlert));
                if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                    client.gui.setTitle(Component.literal("§c§lDe-listed!"));
                    client.gui.setSubtitle(Component.literal(" "));
                    client.gui.setTimes(0,30,5);
                }
                hideMessage = true;
            }
            return !hideMessage;
        });
    }

    private void checkFullParty() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || !enabled || !queueAddon.isEnabled()) return;
        if (partyQueued && partyAmount >= 5) {
            partyQueued = false;
            partyAmount = 0;
            debounce_partyMembers = false;

            customSound2.playTestSound();
            client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aQueued for §b" + getTime(System.currentTimeMillis() - queuedTime)));
            client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aDe-listed! §6§lParty Full! (5/5)"));
            if (PartyFinder.queueAddonShowTitle.isEnabled()) {
                client.gui.setTitle(Component.literal("§6§lPARTY FULL! (5/5)"));
                client.gui.setSubtitle(Component.literal(" "));
                client.gui.setTimes(0,40,5);
            }

            fullSoundPlaysLeft = 5;
            fullSoundTickDelay = 0;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;
        if (!EditHudScreen.isEditMode()) {
            if (!queueTimerHud.isEnabled() || !partyQueued) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        String displayTextString;
        if (EditHudScreen.isEditMode() && !partyQueued) {
            displayTextString = "§d§lParty Finder §7(§e3§f/§a5§7) §7(§b42s§7)";
        } else {
            displayTextString = "§d§lParty Finder §7(§e" + partyAmount + "§f/§a5§7) §7(§b" + getTime(System.currentTimeMillis() - queuedTime) + "§7)";
        }

        Component displayText = Component.literal(displayTextString);

        int textWidth = Minecraft.getInstance().font.width(displayText);
        int centerX = 70 - textWidth / 2;

        ctx.text(Minecraft.getInstance().font, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(140 * scale);
        this.height = Math.round(10 * scale);
    }

    public void readParties(AbstractContainerScreen<?> handled) {
        highlightedSlots.clear();
        playerSlots.clear();
        partiesTotal = 0;
        partiesNum = 0;

        partyAmount_Healer = 0;
        partyAmount_Tank = 0;
        partyAmount_Archer = 0;
        partyAmount_Berserk = 0;
        partyAmount_Mage = 0;

        var handler = handled.getMenu();
        var slots = handler.slots;
        for (int i = 0; i < 37; i++) {
            var slot = slots.get(i);
            var stack = slot.getItem();
            boolean MarkParty = false;
            boolean OwnParty = false;
            boolean hasHealer = false;
            boolean hasTank = false;
            boolean hasMage = false;
            boolean hasArcher = false;
            boolean hasBerserk = false;

            if (!slot.hasItem()) continue;
            if (slot.getItem().getItem().toString().contains("glass_pane")) continue;
            if (!slot.getItem().getItem().toString().contains("player_head")) continue;

            String itemName = removeFormat(stack.getHoverName().getString());

            if (itemName.contains("None")) continue;

            String partyLeader = itemName.replace("'s Party", "");
            var lore = stack.getComponents().get(DataComponents.LORE);
            if (lore == null) continue;

            partiesTotal++;

            for (Component line : lore.lines()) {
                String lineString = line.getString();
                String cleanLine = removeFormat(lineString);
                Minecraft client = Minecraft.getInstance();
                if (client == null) continue;
                if (client.player == null) continue;

                if (cleanLine.contains(client.player.getName().getString())) {
                    OwnParty = true;
                }
                if (cleanLine.contains(": " + findingClass + " (") || cleanLine.contains(CLASS_MAP_DEVOVIAN.get(findingClass))) {
                    MarkParty = true;
                }
                if (cleanLine.contains(": Healer (") || cleanLine.contains("[H")) hasHealer = true;
                if (cleanLine.contains(": Tank (") || cleanLine.contains("[T")) hasTank = true;
                if (cleanLine.contains(": Mage (") || cleanLine.contains("[M")) hasMage = true;
                if (cleanLine.contains(": Archer (") || cleanLine.contains("[A")) hasArcher = true;
                if (cleanLine.contains(": Berserk (") || cleanLine.contains("[B")) hasBerserk = true;

            }

            if (!hasHealer) partyAmount_Healer++;
            if (!hasTank) partyAmount_Tank++;
            if (!hasMage) partyAmount_Mage++;
            if (!hasArcher) partyAmount_Archer++;
            if (!hasBerserk) partyAmount_Berserk++;

            if (OwnParty) {
                playerSlots.put(i, findingClass);
            } else if (!MarkParty) {
                partiesNum++;
                highlightedSlots.put(i, findingClass);
            }


        }
    }

    private static int[] getSlotRenderPosition(AbstractContainerScreen<?> screen, int slotIndex) {
        var handler = screen.getMenu();
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return null;

        var slot = handler.slots.get(slotIndex);

        HandledScreenAccessor acc = (HandledScreenAccessor) screen;

        int x = acc.nozomi$getX() + slot.x;
        int y = acc.nozomi$getY() + slot.y;

        return new int[]{x, y};
    }

    private static void highlightSlot(GuiGraphicsExtractor ctx, AbstractContainerScreen<?> screen, int slotIndex, int colorARGB) {
        int[] pos = getSlotRenderPosition(screen, slotIndex);
        if (pos == null) return;

        int x = pos[0];
        int y = pos[1];

        ctx.fill(x, y, x + 16, y + 16, colorARGB);

        if (PartyFinder.highlightMode.getMode().equals("Under Item")) {
            var slot = screen.getMenu().slots.get(slotIndex);
            if (slot.hasItem()) {
                net.minecraft.world.item.ItemStack stack = slot.getItem();
                ctx.item(stack, x, y);
            }
        }
    }

    public static String stripRank(String rankedPlayer) {
        if (rankedPlayer == null || rankedPlayer.isEmpty()) return "";
        return rankedPlayer.replaceAll("\\[[\\w+\\-]+\\] ", "").trim();
    }

    public static int romanToInt(String roman) {
        if (roman == null) return 0;
        return switch (roman.toUpperCase().trim()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            default -> 0;
        };
    }

    public static String getTime(Long ms) {
        if (ms == null || ms == 0) return "?";

        long hours = ms / 3600000;
        long minutes = (ms / 60000) % 60;
        long seconds = (ms / 1000) % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

}