package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.mixin.HandledScreenAccessor;
import net.otsutsukimiho.nozomiaddon.mixin.MinecraftClientAccessor;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonChestProfit implements FeatureManager.Feature {
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private static Text hudText = Text.empty();
    public static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence(?: x(?<amount>\\d+))?");
    public static final Pattern SHARD_PATTERN = Pattern.compile("(?<type>[A-Za-z]+(?: [A-Za-z]+)*) Shard(?: x(?<amount>\\d+))?");
    private static final Pattern COINS_PATTERN = Pattern.compile("^([\\d,]+) Coins$");

    private final Map<Integer, CroesusSlotData> croesusSlots = new ConcurrentHashMap<>();
    private final Map<Integer, ChestProfitData> chestsPrice = new ConcurrentHashMap<>();
    private final Set<Integer> processedSlots = new HashSet<>();
    private int uiTicks = 0;
    private boolean uiActive = false;
    private boolean croesusActive = false;

    public static BooleanSetting showTitle = new BooleanSetting("ShowTitle", true);
    public static BooleanSetting playSound = new BooleanSetting("PlaySound", true);
    public static ModeSetting highlightMode = new ModeSetting("Highlight Style", "Over Item", "Under Item", "Over Item");
    public static FloatSetting highlightColorOpacity = new FloatSetting("ColorOpacity", 0.5f, 0f, 1f, 0.05f);
    public static ColorSetting highlightColorOpen = new ColorSetting("Opened", new Color(255, 168, 0, 255), false);
    public static ColorSetting highlightColorEmpty = new ColorSetting("Empty", new Color(168, 0, 0, 255), false);
    public static ColorSetting highlightColorAlreadyKismet = new ColorSetting("Rerolled", new Color(0, 255, 0, 255), false);
    public static ColorSetting highlightColorNoKismet = new ColorSetting("NOT Reroll", new Color(0, 168, 168, 255), false);
    public static SoundSetting customSound1 = new SoundSetting("RNG Drop Sound", "minecraft:entity.ender_dragon.growl", 1.0f, 2.0f);
    public static SoundSetting customSound2 = new SoundSetting("Star Drop Sound", "minecraft:entity.wither.death", 1.0f, 1.5f);
    public static SoundSetting customSound3 = new SoundSetting("Rare Drop Sound", "minecraft:block.note_block.pling", 1.0f, 1.0f);
    @Override
    public List<Settings> getSettings() {
        return List.of(showTitle, playSound, highlightMode, highlightColorOpacity, highlightColorOpen, highlightColorEmpty, highlightColorAlreadyKismet, highlightColorNoKismet, customSound1, customSound2, customSound3);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1MTc0MTU2MDkxMDQsInByb2ZpbGVJZCI6IjdjZjc2MTFkYmY2YjQxOWRiNjlkMmQzY2Q4NzUxZjRjIiwicHJvZmlsZU5hbWUiOiJrYXJldGg5OTkiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNGNhNDM1NmUzMzhiOGIyY2FiZmVjZDJkY2NkZjM2YzlkZTllZmU3N2U4ODUxNTcxMWFhNGFiMWNjIn19fQ==");
    }

    private String removeFormat(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "").trim();
    }

    public static String normalizeItemName(String name) {
        if (name == null) return "";
        return name.replaceAll("(?i)[§&][0-9A-FK-OR]", "")
                .replaceAll("(?i)^(Shiny |✪ |✪✪ |✪✪✪ |✪✪✪✪ |✪✪✪✪✪)", "")
                .replaceAll("\\s+", " ")
                .trim();
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

    public class CroesusSlotData {
        public final int slot;
        public final boolean opened;
        public final boolean empty;
        public final boolean kismet;
        public CroesusSlotData(int slot, boolean opened, boolean empty, boolean kismet) {
            this.slot = slot;
            this.opened = opened;
            this.empty = empty;
            this.kismet = kismet;
        }
    }

    public class ChestProfitData {
        public final int slot;
        public final double totalProfit;
        public final boolean profit;
        public ChestProfitData(int slot, double totalProfit, boolean profit) {
            this.slot = slot;
            this.totalProfit = totalProfit;
            this.profit = profit;
        }
    }

    private static final Map<String, String> CHESTS_MAP = Map.ofEntries(
            Map.entry("Wood Chest", "§fWood Chest"),
            Map.entry("Gold Chest", "§6Gold Chest"),
            Map.entry("Diamond Chest", "§bDiamond Chest"),
            Map.entry("Emerald Chest", "§2Emerald Chest"),
            Map.entry("Obsidian Chest", "§5Obsidian Chest"),
            Map.entry("Bedrock Chest", "§8Bedrock Chest"),
            Map.entry("Wood", "§fWood Chest"),
            Map.entry("Gold", "§6Gold Chest"),
            Map.entry("Diamond", "§bDiamond Chest"),
            Map.entry("Emerald", "§2Emerald Chest"),
            Map.entry("Obsidian", "§5Obsidian Chest"),
            Map.entry("Bedrock", "§8Bedrock Chest")
    );

    private static final Map<String, String> DUNGEON_DROPS = Map.ofEntries(
            Map.entry("Enchanted Book (Ultimate Jerry I)", "ENCHANTMENT_ULTIMATE_JERRY_1"),
            Map.entry("Enchanted Book (Ultimate Jerry II)", "ENCHANTMENT_ULTIMATE_JERRY_2"),
            Map.entry("Enchanted Book (Ultimate Jerry III)", "ENCHANTMENT_ULTIMATE_JERRY_3"),
            Map.entry("Enchanted Book (Bank I)", "ENCHANTMENT_ULTIMATE_BANK_1"),
            Map.entry("Enchanted Book (Bank II)", "ENCHANTMENT_ULTIMATE_BANK_2"),
            Map.entry("Enchanted Book (Bank III)", "ENCHANTMENT_ULTIMATE_BANK_3"),
            Map.entry("Enchanted Book (Combo I)", "ENCHANTMENT_ULTIMATE_COMBO_1"),
            Map.entry("Enchanted Book (Combo II)", "ENCHANTMENT_ULTIMATE_COMBO_2"),
            Map.entry("Enchanted Book (No Pain No Gain I)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1"),
            Map.entry("Enchanted Book (No Pain No Gain II)", "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2"),
            Map.entry("Enchanted Book (Ultimate Wise I)", "ENCHANTMENT_ULTIMATE_WISE_1"),
            Map.entry("Enchanted Book (Ultimate Wise II)", "ENCHANTMENT_ULTIMATE_WISE_2"),
            Map.entry("Enchanted Book (Wisdom I)", "ENCHANTMENT_ULTIMATE_WISDOM_1"),
            Map.entry("Enchanted Book (Wisdom II)", "ENCHANTMENT_ULTIMATE_WISDOM_2"),
            Map.entry("Enchanted Book (Last Stand I)", "ENCHANTMENT_ULTIMATE_LAST_STAND_1"),
            Map.entry("Enchanted Book (Last Stand II)", "ENCHANTMENT_ULTIMATE_LAST_STAND_2"),
            Map.entry("Enchanted Book (Rend I)", "ENCHANTMENT_ULTIMATE_REND_1"),
            Map.entry("Enchanted Book (Rend II)", "ENCHANTMENT_ULTIMATE_REND_2"),
            Map.entry("Enchanted Book (Legion I)", "ENCHANTMENT_ULTIMATE_LEGION_1"),
            Map.entry("Enchanted Book (Swarm I)", "ENCHANTMENT_ULTIMATE_SWARM_1"),
            Map.entry("Enchanted Book (One For All I)", "ENCHANTMENT_ULTIMATE_ONE_FOR_ALL_1"),
            Map.entry("Enchanted Book (Soul Eater I)", "ENCHANTMENT_ULTIMATE_SOUL_EATER_1"),
            Map.entry("Enchanted Book (Infinite Quiver VI)", "ENCHANTMENT_INFINITE_QUIVER_6"),
            Map.entry("Enchanted Book (Infinite Quiver VII)", "ENCHANTMENT_INFINITE_QUIVER_7"),
            Map.entry("Enchanted Book (Feather Falling VI)", "ENCHANTMENT_FEATHER_FALLING_6"),
            Map.entry("Enchanted Book (Feather Falling VII)", "ENCHANTMENT_FEATHER_FALLING_7"),
            Map.entry("Enchanted Book (Rejuvenate I)", "ENCHANTMENT_REJUVENATE_1"),
            Map.entry("Enchanted Book (Rejuvenate II)", "ENCHANTMENT_REJUVENATE_2"),
            Map.entry("Enchanted Book (Rejuvenate III)", "ENCHANTMENT_REJUVENATE_3"),
            Map.entry("Enchanted Book (Overload I)", "ENCHANTMENT_OVERLOAD_1"),
            Map.entry("Enchanted Book (Lethality VI)", "ENCHANTMENT_LETHALITY_6"),
            Map.entry("Enchanted Book (Thunderlord VII)", "ENCHANTMENT_THUNDERLORD_7"),

            Map.entry("Hot Potato Book", "HOT_POTATO_BOOK"),
            Map.entry("Fuming Potato Book", "FUMING_POTATO_BOOK"),
            Map.entry("Recombobulator 3000", "RECOMBOBULATOR_3000"),

            Map.entry("Necromancer's Brooch", "NECROMANCER_BROOCH"),

            Map.entry("Bonzo's Staff", "BONZO_STAFF"),
            Map.entry("Master Skull - Tier 1", "MASTER_SKULL_TIER_1"),
            Map.entry("Bonzo's Mask", "BONZO_MASK"),
            Map.entry("Balloon Snake", "BALLOON_SNAKE"),
            Map.entry("Red Nose", "RED_NOSE"),

            Map.entry("Red Scarf", "RED_SCARF"),
            Map.entry("Adaptive Blade", "STONE_BLADE"),
            Map.entry("Master Skull - Tier 2", "MASTER_SKULL_TIER_2"),
            Map.entry("Adaptive Belt", "ADAPTIVE_BELT"),
            Map.entry("Scarf's Studies", "SCARF_STUDIES"),

            Map.entry("First Master Star", "FIRST_MASTER_STAR"),
            Map.entry("Adaptive Helmet", "ADAPTIVE_HELMET"),
            Map.entry("Adaptive Chestplate", "ADAPTIVE_CHESTPLATE"),
            Map.entry("Adaptive Leggings", "ADAPTIVE_LEGGINGS"),
            Map.entry("Adaptive Boots", "ADAPTIVE_BOOTS"),
            Map.entry("Master Skull - Tier 3", "MASTER_SKULL_TIER_3"),
            Map.entry("Suspicious Vial", "SUSPICIOUS_VIAL"),

            Map.entry("Spirit Sword", "SPIRIT_SWORD"),
            Map.entry("Spirit Shortbow", "ITEM_SPIRIT_BOW"),
            Map.entry("Spirit Boots", "THORNS_BOOTS"),
            Map.entry("Spirit", "LVL_1_LEGENDARY_SPIRIT"),
            Map.entry("Spirit Epic", "LVL_1_EPIC_SPIRIT"),

            Map.entry("Second Master Star", "SECOND_MASTER_STAR"),
            Map.entry("Spirit Wing", "SPIRIT_WING"),
            Map.entry("Spirit Bone", "SPIRIT_BONE"),
            Map.entry("Spirit Stone", "SPIRIT_DECOY"),

            Map.entry("Shadow Fury", "SHADOW_FURY"),
            Map.entry("Last Breath", "LAST_BREATH"),
            Map.entry("Third Master Star", "THIRD_MASTER_STAR"),
            Map.entry("Warped Stone", "AOTE_STONE"),
            Map.entry("Livid Dagger", "LIVID_DAGGER"),
            Map.entry("Livid Dye", "DYE_LIVID"),
            Map.entry("Shadow Assassin Helmet", "SHADOW_ASSASSIN_HELMET"),
            Map.entry("Shadow Assassin Chestplate", "SHADOW_ASSASSIN_CHESTPLATE"),
            Map.entry("Shadow Assassin Leggings", "SHADOW_ASSASSIN_LEGGINGS"),
            Map.entry("Shadow Assassin Boots", "SHADOW_ASSASSIN_BOOTS"),
            Map.entry("Shadow Assassin Cloak", "SHADOW_ASSASSIN_CLOAK"),
            Map.entry("Master Skull - Tier 4", "MASTER_SKULL_TIER_4"),
            Map.entry("Dark Orb", "DARK_ORB"),

            Map.entry("Precursor Eye", "PRECURSOR_EYE"),
            Map.entry("Giant's Sword", "GIANTS_SWORD"),
            Map.entry("Necromancer Lord Helmet", "NECROMANCER_LORD_HELMET"),
            Map.entry("Necromancer Lord Chestplate", "NECROMANCER_LORD_CHESTPLATE"),
            Map.entry("Necromancer Lord Leggings", "NECROMANCER_LORD_LEGGINGS"),
            Map.entry("Necromancer Lord Boots", "NECROMANCER_LORD_BOOTS"),
            Map.entry("Fourth Master Star", "FOURTH_MASTER_STAR"),
            Map.entry("Summoning Ring", "SUMMONING_RING"),
            Map.entry("Fel Skull", "FEL_SKULL"),
            Map.entry("Necromancer Sword", "NECROMANCER_SWORD"),
            Map.entry("Soulweaver Gloves", "SOULWEAVER_GLOVES"),
            Map.entry("Sadan's Brooch", "SADAN_BROOCH"),
            Map.entry("Giant Tooth", "GIANT_TOOTH"),

            Map.entry("Precursor Gear", "PRECURSOR_GEAR"),
            Map.entry("Necron Dye", "DYE_NECRON"),
            Map.entry("Storm the Fish", "STORM_THE_FISH"),
            Map.entry("Maxor the Fish", "MAXOR_THE_FISH"),
            Map.entry("Goldor the Fish", "GOLDOR_THE_FISH"),
            Map.entry("Dark Claymore", "DARK_CLAYMORE"),
            Map.entry("Necron's Handle", "NECRON_HANDLE"),
            Map.entry("Master Skull - Tier 5", "MASTER_SKULL_TIER_5"),
            Map.entry("Shadow Warp", "SHADOW_WARP_SCROLL"),
            Map.entry("Wither Shield", "WITHER_SHIELD_SCROLL"),
            Map.entry("Implosion", "IMPLOSION_SCROLL"),
            Map.entry("Fifth Master Star", "FIFTH_MASTER_STAR"),
            Map.entry("Auto Recombobulator", "AUTO_RECOMBOBULATOR"),
            Map.entry("Wither Helmet", "WITHER_HELMET"),
            Map.entry("Wither Chestplate", "WITHER_CHESTPLATE"),
            Map.entry("Wither Leggings", "WITHER_LEGGINGS"),
            Map.entry("Wither Boots", "WITHER_BOOTS"),
            Map.entry("Wither Catalyst", "WITHER_CATALYST"),
            Map.entry("Wither Cloak Sword", "WITHER_CLOAK"),
            Map.entry("Wither Blood", "WITHER_BLOOD"),

            Map.entry("Shiny Wither Helmet", "SHINY_WITHER_HELMET"),
            Map.entry("Shiny Wither Chestplate", "SHINY_WITHER_CHESTPLATE"),
            Map.entry("Shiny Wither Leggings", "SHINY_WITHER_LEGGINGS"),
            Map.entry("Shiny Wither Boots", "SHINY_WITHER_BOOTS"),
            Map.entry("Shiny Necron's Handle", "SHINY_NECRON_HANDLE"),

            Map.entry("Dungeon Disc", "DUNGEON_DISC_1"),
            Map.entry("Clown Disc", "DUNGEON_DISC_2"),
            Map.entry("Watcher Disc", "DUNGEON_DISC_3"),
            Map.entry("Old Disc", "DUNGEON_DISC_4"),
            Map.entry("Necron Disc", "DUNGEON_DISC_5"),

            Map.entry("Scarf Shard", "SHARD_SCARF"),
            Map.entry("Thorn Shard", "SHARD_THORN"),
            Map.entry("Wither Shard", "SHARD_WITHER"),
            Map.entry("Apex Dragon Shard", "SHARD_APEX_DRAGON"),
            Map.entry("Power Dragon Shard", "SHARD_POWER_DRAGON")
    );

    private static final Map<String, Object> DROPS_MASTER_STAR = Map.ofEntries(
            Map.entry("FIRST_MASTER_STAR", SoundEvents.ENTITY_WITHER_DEATH),
            Map.entry("SECOND_MASTER_STAR", SoundEvents.ENTITY_WITHER_DEATH),
            Map.entry("THIRD_MASTER_STAR", SoundEvents.ENTITY_WITHER_DEATH),
            Map.entry("FOURTH_MASTER_STAR", SoundEvents.ENTITY_WITHER_DEATH),
            Map.entry("FIFTH_MASTER_STAR", SoundEvents.ENTITY_WITHER_DEATH)
    );

    private static final Map<String, Object> DROPS_RNG = Map.ofEntries(
            Map.entry("SHADOW_FURY", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("GIANTS_SWORD", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("PRECURSOR_EYE", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("SHADOW_WARP_SCROLL", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("IMPLOSION_SCROLL", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("WITHER_SHIELD_SCROLL", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("NECRON_HANDLE", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("DARK_CLAYMORE", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("DYE_LIVID", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("DYE_NECRON", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("ENCHANTMENT_THUNDERLORD_7", SoundEvents.ENTITY_ENDER_DRAGON_GROWL),
            Map.entry("MASTER_SKULL_TIER_5", SoundEvents.ENTITY_ENDER_DRAGON_GROWL)
    );

    private static final Map<String, Object> DROPS_NORMAL = Map.ofEntries(
            Map.entry("RECOMBOBULATOR_3000", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("BONZO_STAFF", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("SPIRIT_WING", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("SPIRIT_BONE", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("LIVID_DAGGER", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("SHADOW_ASSASSIN_CHESTPLATE", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("NECROMANCER_LORD_CHESTPLATE", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("SUMMONING_RING", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("WITHER_CHESTPLATE", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("ITEM_SPIRIT_BOW", SoundEvents.BLOCK_NOTE_BLOCK_PLING),
            Map.entry("LAST_BREATH", SoundEvents.BLOCK_NOTE_BLOCK_PLING)
    );

    public void initClient() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (uiActive) {
                uiTicks++;
                if (uiTicks < 2) return;
                if (!(client.currentScreen instanceof HandledScreen<?> handled)) return;
                if (croesusActive) {
                    croesusActive = false;
                    readCroesus(handled);
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
            int chestHeight = 192;
            int chestY = (screenHeight - chestHeight) / 2;

            uiActive = false;
            croesusActive = false;
            uiTicks = 0;
            croesusSlots.clear();
            chestsPrice.clear();
            processedSlots.clear();

            if (screen instanceof HandledScreen<?> handled) {
                uiActive = true;
                Text title = handled.getTitle();
                String titleStr = title.getString();
                hudText = Text.empty();

                var handler = handled.getScreenHandler();

                if (titleStr.contains("Catacombs - Floor")) {
                    ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {

                        List<Text> lines = new ArrayList<>();
                        var slots = handler.slots;
                        for (int i = 0; i < 19; i++) {
                            double totalProfit = 0;
                            double chestPrice = 0;
                            boolean alreadyOpen = false;
                            boolean noMoreChest = false;
                            var slot = slots.get(i);
                            if (!slot.hasStack()) continue;
                            if (slot.getStack().getItem().toString().contains("glass_pane")) continue;
                            if (!slot.getStack().getItem().toString().contains("player_head")) continue;

                            var stack = slot.getStack();
                            String itemName = removeFormat(stack.getName().getString());
                            var lore = stack.getComponents().get(DataComponentTypes.LORE);
                            if (lore == null) return;

                            for (Text line : lore.lines()) {
                                String lineString = line.getString();
                                String cleanName = removeFormat(lineString);
                                if (cleanName.contains("Dungeon Chest Key")) {
                                    double keyPrice = PriceUtils.getBazaarBuyPrice("DUNGEON_CHEST_KEY");
                                    chestPrice += keyPrice;
                                }
                                if (cleanName.contains("Coins")) {
                                    Matcher matcher = COINS_PATTERN.matcher(cleanName);
                                    if (matcher.matches()) {
                                        String number = matcher.group(1).replace(",", "");
                                        chestPrice += Double.parseDouble(number);
                                    }
                                }
                                if (cleanName.contains("Already opened!")) alreadyOpen = true;
                                if (cleanName.contains("Can't open another chest!")) noMoreChest = true;
                            }
                            for (Text line : lore.lines()) {
                                double price = 0;
                                String lineString = removeFormat(line.getString());
                                String cleanName = normalizeItemName(lineString);
                                String ID_From_Map = "UNKNOWN";
                                if (cleanName.contains("Contents")) continue;
                                if (cleanName.contains("Cost")) break;
                                if (cleanName.contains("Essence")) {
                                    Matcher matcher = ESSENCE_PATTERN.matcher(cleanName);
                                    if (matcher.matches()) {
                                        String type = matcher.group("type").toUpperCase(Locale.ENGLISH);
                                        ID_From_Map = "ESSENCE_" + type;
                                        int amount = ItemUtils.parseOptionalIntFromMatcher(matcher, "amount").orElse(1);
                                        price += PriceUtils.getBazaarSellPrice(ID_From_Map) * amount;
                                    }
                                } else {
                                    ID_From_Map = DUNGEON_DROPS.get(cleanName);
                                    double getPrice = PriceUtils.getLowestBin(ID_From_Map);
                                    if (getPrice == -1) {
                                        price += PriceUtils.getBazaarSellPrice(ID_From_Map);
                                    } else {
                                        if (stack.getName().getString().contains("the Fish")) {
                                            price += 0;
                                        } else {
                                            price += PriceUtils.getLowestBin(ID_From_Map);
                                        }
                                    }
                                }

                                totalProfit += price;
                                String finalId = ID_From_Map;
                                String chestName = CHESTS_MAP.get(itemName) + "§f: ";

                                if (MinecraftClient.getInstance().player != null && finalId != null) {
                                    if (!processedSlots.contains(i)) {
                                        boolean announced = false;

                                        if (DROPS_NORMAL.containsKey(finalId)) {
                                            if (DungeonChestProfit.playSound.isEnabled()) customSound3.playTestSound();
                                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(line), false);
                                            if (DungeonChestProfit.showTitle.isEnabled()) {
                                                MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(line).append(""));
                                                MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§6§lRARE DROP"));
                                                MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                            }
                                            hudText = hudText.copy().append(Text.literal("§d§k§lA§r ").append(line).append(" §d§k§lA§r "));
                                            announced = true;
                                        } else if (DROPS_RNG.containsKey(finalId)) {
                                            if (DungeonChestProfit.playSound.isEnabled()) customSound1.playTestSound();
                                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(line), false);
                                            if (DungeonChestProfit.showTitle.isEnabled()) {
                                                MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(line).append(""));
                                                MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§d§lRNG DROP"));
                                                MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                            }
                                            hudText = hudText.copy().append(Text.literal("§d§k§lA§r ").append(line).append(" §d§k§lA§r "));
                                            announced = true;
                                        } else if (DROPS_MASTER_STAR.containsKey(finalId)) {
                                            if (DungeonChestProfit.playSound.isEnabled()) customSound2.playTestSound();
                                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(line), false);
                                            if (DungeonChestProfit.showTitle.isEnabled()) {
                                                MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(line).append(""));
                                                MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§d§lRNG DROP"));
                                                MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                            }
                                            hudText = hudText.copy().append(Text.literal("§d§k§lA§r ").append(line).append(" §d§k§lA§r "));
                                            announced = true;
                                        }

                                        if (announced) {
                                            processedSlots.add(i);
                                        }
                                    }

                                }

                            }

                            totalProfit -= chestPrice;

                            if (alreadyOpen) {
                                ChestProfitData data = new ChestProfitData(i, 0.0, false);
                                chestsPrice.put(data.slot, data);
                                lines.add(Text.literal("§r").append(stack.getName()).append(Text.literal("§f: §aAlready opened!")));
                            } else {
                                if (noMoreChest) {
                                    ChestProfitData data = new ChestProfitData(i, 0.0, false);
                                    chestsPrice.put(data.slot, data);
                                    lines.add(Text.literal("§r").append(stack.getName()).append(Text.literal("§f: §cCan't open another chest!")));
                                } else {
                                    if (totalProfit >= 0) {
                                        ChestProfitData data = new ChestProfitData(i, totalProfit, true);
                                        chestsPrice.put(data.slot, data);
                                        lines.add(Text.literal("§r").append(stack.getName()).append(Text.literal("§f: §a+" + shortNumber(totalProfit))));
                                    } else {
                                        ChestProfitData data = new ChestProfitData(i, 0.0, false);
                                        chestsPrice.put(data.slot, data);
                                        lines.add(Text.literal("§r").append(stack.getName()).append(Text.literal("§f: §c-" + shortNumber(totalProfit * -1))));
                                    }
                                }
                            }
                        }

                        ChestProfitData bestChest = chestsPrice.values()
                                .stream()
                                .filter(run -> run.profit)
                                .max(Comparator.comparingDouble(run -> run.totalProfit))
                                .orElse(null);

                        int mainX = (screen1.width / 2) + 95;
                        int mainY = (MinecraftClient.getInstance().getWindow().getScaledHeight() - 192) / 2 + 5;
                        int spacing = 10;

                        Text[] mainLines = lines.toArray(Text[]::new);
                        for (int i = 0; i < mainLines.length; i++) {
                            drawContext.drawText(MinecraftClient.getInstance().textRenderer, mainLines[i], mainX, mainY + (i * spacing), 0xFFFFFFFF, true);
                        }

                        if (screen1 instanceof HandledScreen<?> handledScreen) {
                            if (bestChest != null) {
                                highlightSlot(drawContext, handledScreen, bestChest.slot, 0x8000FF00);
                            }
                        }

                        drawContext.drawText(
                                MinecraftClient.getInstance().textRenderer,
                                hudText,
                                (screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(hudText)) / 2,
                                chestY - 15,
                                0xFFFFFFFF,
                                true
                        );
                    });
                } else {
                    if (titleStr.contains("Croesus")) {
                        croesusActive = true;
                        ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                            if (screen1 instanceof HandledScreen<?> handledScreen) {
                                if (croesusSlots.isEmpty()) return;
                                for (CroesusSlotData run : croesusSlots.values()) {
                                    int color;
                                    int alpha = (int) (Math.max(0f, Math.min(1f, DungeonChestProfit.highlightColorOpacity.getValue())) * 255);

                                    if (run.empty) {
                                        color = (alpha << 24) | (DungeonChestProfit.highlightColorEmpty.getRGB() & 0x00FFFFFF);
                                    } else if (run.opened) {
                                        if (run.kismet) {
                                            color = (alpha << 24) | (DungeonChestProfit.highlightColorAlreadyKismet.getRGB() & 0x00FFFFFF);
                                        } else {
                                            color = (alpha << 24) | (DungeonChestProfit.highlightColorNoKismet.getRGB() & 0x00FFFFFF);
                                        }
                                    } else {
                                        color = (alpha << 24) | (DungeonChestProfit.highlightColorOpen.getRGB() & 0x00FFFFFF);
                                    }
                                    highlightSlot(drawContext, handledScreen, run.slot, color);
                                }
                            }
                        });
                    } else {
                        if (handler.slots.size() >= 29) {
                            ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                                double totalProfit = 0;
                                double chestPrice = 0;
                                List<Text> lines = new ArrayList<>();
                                var slots = handler.slots;
                                var slotChest = slots.get(31);
                                if (slotChest.getStack().getName().getString().contains("Open Reward Chest")) {
                                    var chestLore = slotChest.getStack().getComponents().get(DataComponentTypes.LORE);
                                    if (chestLore != null) {
                                        var chestLines = chestLore.lines();
                                        for (Text line : chestLines) {
                                            String lineString = line.getString();
                                            String cleanName = removeFormat(lineString);
                                            if (cleanName.contains("Dungeon Chest Key")) {
                                                double keyPrice = PriceUtils.getBazaarBuyPrice("DUNGEON_CHEST_KEY");
                                                chestPrice += keyPrice;
                                            }
                                            if (cleanName.contains("Coins")) {
                                                Matcher matcher = COINS_PATTERN.matcher(cleanName);
                                                if (matcher.matches()) {
                                                    String number = matcher.group(1).replace(",", "");
                                                    chestPrice += Double.parseDouble(number);
                                                }
                                            }
                                        }
                                        for (int i = 0; i < 29; i++) {
                                            double price = 0;
                                            var slot = slots.get(i);
                                            if (!slot.hasStack()) continue;
                                            if (slot.getStack().getItem().toString().contains("glass_pane")) continue;

                                            var stack = slot.getStack();
                                            String itemName = removeFormat(stack.getName().getString());
                                            String cleanName = normalizeItemName(itemName);
                                            String ID_From_Map = "UNKNOWN";
                                            var lore = stack.getComponents().get(DataComponentTypes.LORE);

                                            if (cleanName.contains("Enchanted Book")) {
                                                if (lore != null && lore.lines().size() > 2) {
                                                    Text enchantLine = lore.lines().get(2);
                                                    String book = "(" + enchantLine.getString() + ")";
                                                    String foundId = DUNGEON_DROPS.get("Enchanted Book " + book);
                                                    if (foundId != null) {
                                                        ID_From_Map = foundId;
                                                    }
                                                    price += PriceUtils.getBazaarSellPrice(ID_From_Map);
                                                    lines.add(Text.literal("§r").append(enchantLine).append(String.format(" §6%s", shortNumber(price))));
                                                }
                                            } else {
                                                if (cleanName.contains("Essence")) {
                                                    Matcher matcher = ESSENCE_PATTERN.matcher(itemName);
                                                    if (matcher.matches()) {
                                                        String type = matcher.group("type").toUpperCase(Locale.ENGLISH);
                                                        ID_From_Map = "ESSENCE_" + type;
                                                        int amount = ItemUtils.parseOptionalIntFromMatcher(matcher, "amount").orElse(1);
                                                        price += PriceUtils.getBazaarSellPrice(ID_From_Map) * amount;
                                                        lines.add(Text.literal("§r").append(stack.getName()).append(String.format(" §6%s", shortNumber(price))));
                                                    }
                                                } else {
                                                    if (cleanName.contains("Shard")) {
                                                        Matcher matcher = SHARD_PATTERN.matcher(itemName);
                                                        if (matcher.matches()) {
                                                            String type = matcher.group("type").toUpperCase(Locale.ENGLISH).replaceAll("\\s+", "_");
                                                            ID_From_Map = "SHARD_" + type;
                                                            int amount = ItemUtils.parseOptionalIntFromMatcher(matcher, "amount").orElse(1);
                                                            price += PriceUtils.getBazaarSellPrice(ID_From_Map) * amount;
                                                            lines.add(Text.literal("§r").append(stack.getName()).append(String.format(" §6%s", shortNumber(price))));
                                                        }
                                                    } else {
                                                        String foundId = DUNGEON_DROPS.get(cleanName);
                                                        if (foundId != null) {
                                                            ID_From_Map = foundId;
                                                        }
                                                        double getPrice = PriceUtils.getLowestBin(ID_From_Map);
                                                        if (getPrice == -1) {
                                                            price += PriceUtils.getBazaarSellPrice(ID_From_Map);
                                                        } else {
                                                            if (cleanName.contains("the Fish")) {
                                                                price += 0;
                                                            } else {
                                                                price += PriceUtils.getLowestBin(ID_From_Map);
                                                            }
                                                        }
                                                        lines.add(Text.literal("§r").append(stack.getName()).append(String.format(" §6%s", shortNumber(price))));
                                                    }
                                                }
                                            }

                                            totalProfit += price;

                                            String finalId = ID_From_Map;
                                            String chestName = CHESTS_MAP.get(titleStr) + "§f: ";

                                            if (MinecraftClient.getInstance().player != null) {
                                                if (!processedSlots.contains(i)) {
                                                    boolean announced = false;

                                                    if (DROPS_NORMAL.containsKey(finalId)) {
                                                        if (DungeonChestProfit.playSound.isEnabled()) customSound3.playTestSound();
                                                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(stack.getName()), false);
                                                        if (DungeonChestProfit.showTitle.isEnabled()) {
                                                            MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(stack.getName()).append(""));
                                                            MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§6§lRARE DROP"));
                                                            MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                                        }
                                                        hudText = hudText.copy().append(Text.literal("§d§k§lA§r ")
                                                                .append(stack.getName())
                                                                .append(" §d§k§lA§r "));
                                                        announced = true;
                                                    } else if (DROPS_RNG.containsKey(finalId)) {
                                                        if (DungeonChestProfit.playSound.isEnabled()) customSound1.playTestSound();
                                                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(stack.getName()), false);
                                                        if (DungeonChestProfit.showTitle.isEnabled()) {
                                                            MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(stack.getName()).append(""));
                                                            MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§d§lRNG DROP"));
                                                            MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                                        }
                                                        hudText = hudText.copy().append(Text.literal("§d§k§lA§r ")
                                                                .append(stack.getName())
                                                                .append(" §d§k§lA§r "));
                                                        announced = true;
                                                    } else if (DROPS_MASTER_STAR.containsKey(finalId)) {
                                                        if (DungeonChestProfit.playSound.isEnabled()) customSound2.playTestSound();
                                                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§d§lNA §f§l» ").append(chestName).append(stack.getName()), false);
                                                        if (DungeonChestProfit.showTitle.isEnabled()) {
                                                            MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("").append(stack.getName()).append(""));
                                                            MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§d§lRNG DROP"));
                                                            MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 50, 10);
                                                        }
                                                        hudText = hudText.copy().append(Text.literal("§d§k§lA§r ")
                                                                .append(stack.getName())
                                                                .append(" §d§k§lA§r "));
                                                        announced = true;
                                                    }
                                                    if (announced) {
                                                        processedSlots.add(i);
                                                    }
                                                }
                                            }
                                        }

                                        totalProfit -= chestPrice;
                                        lines.add(Text.literal("§r"));

                                        Text calProfit;
                                        if (totalProfit >= 0) {
                                            calProfit = Text.literal("§a+" + shortNumber(totalProfit) + " Coins");
                                            lines.add(Text.literal(String.format("§fProfit: §a+%s", shortNumber(totalProfit))));
                                        } else {
                                            calProfit = Text.literal("§c-" + shortNumber(totalProfit * -1) + " Coins");
                                            lines.add(Text.literal(String.format("§fProfit: §c-%s", shortNumber(totalProfit * -1))));
                                        }
                                        Text titleProfit = calProfit;

                                        int mainX = (screen1.width / 2) + 95;
                                        int mainY = (MinecraftClient.getInstance().getWindow().getScaledHeight() - 192) / 2 - 10;
                                        int chestX = (MinecraftClient.getInstance().getWindow().getScaledWidth() - 6) / 2;
                                        int textY = chestY - 10;
                                        int spacing = 10;
                                        Text[] mainLines = lines.toArray(Text[]::new);
                                        for (int i = 0; i < mainLines.length; i++) {
                                            drawContext.drawText(MinecraftClient.getInstance().textRenderer, mainLines[i], mainX, mainY + (i * spacing), 0xFFFFFFFF, true);
                                        }
                                        drawContext.drawText(
                                                MinecraftClient.getInstance().textRenderer,
                                                titleProfit,
                                                chestX,
                                                textY,
                                                0xFFFFFFFF,
                                                true
                                        );
                                        drawContext.drawText(
                                                MinecraftClient.getInstance().textRenderer,
                                                hudText,
                                                (screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(hudText)) / 2,
                                                chestY - 30,
                                                0xFFFFFFFF,
                                                true
                                        );
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public void readCroesus(HandledScreen<?> handled) {
        var handler = handled.getScreenHandler();
        var slots = handler.slots;
        for (int i = 10; i < 45; i++) {
            var slot = slots.get(i);
            var stack = slot.getStack();

            if (!slot.hasStack()) continue;
            if (slot.getStack().getItem().toString().contains("glass_pane")) continue;
            if (!slot.getStack().getItem().toString().contains("player_head")) continue;

            boolean itemOpened = true;
            boolean itemEmpty = false;
            boolean itemKismet = false;

            var lore = stack.getComponents().get(DataComponentTypes.LORE);
            if (lore == null) continue;

            for (Text line : lore.lines()) {
                String lineString = line.getString();
                String cleanLine = removeFormat(lineString);
                if (cleanLine.contains("No chests opened yet!") && itemOpened) itemOpened = false;
                if (cleanLine.contains("No more chests to open!") && !itemEmpty) itemEmpty = true;
                if (cleanLine.contains("Kismet Feather") && !itemKismet) {
                    boolean isStruck = line.visit((style, text) -> {
                        if (text.contains("Kismet Feather") && style.isStrikethrough()) {
                            return Optional.of(true);
                        }
                        return Optional.empty();
                    }, Style.EMPTY).orElse(false);
                    if (isStruck) {
                        itemKismet = true;
                    }
                }
            }
            CroesusSlotData data = new CroesusSlotData(i, !itemOpened, itemEmpty, itemKismet);
            croesusSlots.put(data.slot, data);
        }
    }

    private static int[] getSlotRenderPosition(HandledScreen<?> screen, int slotIndex) {
        var handler = screen.getScreenHandler();
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return null;

        var slot = handler.slots.get(slotIndex);

        HandledScreenAccessor acc = (HandledScreenAccessor) screen;

        int x = acc.nozomi$getX() + slot.x;
        int y = acc.nozomi$getY() + slot.y;

        return new int[]{x, y};
    }

    private static void highlightSlot(DrawContext ctx, HandledScreen<?> screen, int slotIndex, int colorARGB) {
        int[] pos = getSlotRenderPosition(screen, slotIndex);
        if (pos == null) return;

        int x = pos[0];
        int y = pos[1];

        ctx.fill(x, y, x + 16, y + 16, colorARGB);

        if (DungeonChestProfit.highlightMode.getMode().equals("Under Item")) {
            var slot = screen.getScreenHandler().slots.get(slotIndex);
            if (slot.hasStack()) {
                net.minecraft.item.ItemStack stack = slot.getStack();

                ctx.drawItem(stack, x, y);
                ctx.drawItem(stack, x, y);
            }
        }
    }

}