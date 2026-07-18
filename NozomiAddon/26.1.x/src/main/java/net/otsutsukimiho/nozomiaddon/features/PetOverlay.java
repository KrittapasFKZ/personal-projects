package net.otsutsukimiho.nozomiaddon.features;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.config.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetOverlay extends DraggableHudElement implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new Gson();
    private final List<PetInfo> petCache = new ArrayList<>();

    public PetOverlay() {
        super("PetOverlay", 10, 10, 120, 20, 10);
    }

    public static BooleanSetting showPetLevel = new BooleanSetting("ShowPetLevel", true);
    public static BooleanSetting showPetIcon = new BooleanSetting("ShowPetIcon", true);
    public static BooleanSetting showTitle = new BooleanSetting("ShowTitle", true);
    public static BooleanSetting readPetDataConfirmation = new BooleanSetting("ReadPetDataConfirmation", true);
    public static BooleanSetting playSoundOnChange = new BooleanSetting("PlaySoundOnChange", true);
    public static SoundSetting customSound1 = new SoundSetting("Pet Changed", "minecraft:entity.cat.ambient", 1.0f, 1.0f);
    public static SoundSetting customSound2 = new SoundSetting("No Pet", "minecraft:entity.cat.ambient", 1.0f, 0.5f);
    @Override
    public List<Settings> getSettings() {
        return List.of(showPetLevel, showPetIcon, showTitle, readPetDataConfirmation, playSoundOnChange, customSound1, customSound2);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTYyMDM1MDA5ODgyNiwKICAicHJvZmlsZUlkIiA6ICJiNWRkZTVmODJlYjM0OTkzYmMwN2Q0MGFiNWY2ODYyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJsdXhlbWFuIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlOWY5YjFmYzAxNDE2NmNiNDZhMDkzZTUzNDliMmJmNmVkZDIwMWI2ODBkNjJlNDhkYmYzYWY5YjA0NTkxMTYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) loadPets();
    }

    public boolean isEnabled() { return enabled; }

    private volatile boolean enabled = false;
    public static boolean saveData = false;
    private MutableComponent currentPet = Component.literal("§cNone");

    private static final Pattern SUMMON_PATTERN = Pattern.compile("You summoned your (.+?)!");
    private static final Pattern DESPAWN_PATTERN = Pattern.compile("You despawned your (.+?)!");
    private static final Pattern AUTOPET_PATTERN = Pattern.compile("Autopet equipped your (.+?)!");

    private static final Path DATA_DIR =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("nozomiaddon");
    private static final Path DATA_FILE = DATA_DIR.resolve("pets.json");

    private static String PET_TEXTURE = "";
    private static ItemStack PET_ITEM;

    private static class PetInfo {
        String cleanName;
        JsonElement name;
        int level;
        String texture;
    }

    private void loadPets() {
        try {
            if (!Files.exists(DATA_FILE)) {
                return;
            }

            String json = Files.readString(DATA_FILE);

            List<PetInfo> loaded = GSON.fromJson(json, new TypeToken<List<PetInfo>>(){}.getType());

            if (loaded == null) {
                return;
            }

            petCache.clear();
            petCache.addAll(loaded);
        } catch (Exception e) {
            LOGGER.error("Failed to load pets.json", e);
        }
    }

    public void initClient() {
        loadPets();

        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            if (!enabled) return;
            loadPets();
            String raw = message.getString();
            String plain = removeFormat(raw).trim();

            Matcher mSummon = SUMMON_PATTERN.matcher(plain);
            if (mSummon.find()) {
                String petName = mSummon.group(1).trim();
                updatePetDisplay(petName);
                return;
            }

            Matcher mDespawn = DESPAWN_PATTERN.matcher(plain);
            if (mDespawn.find()) {
                onPetDespawned();
                return;
            }

            Matcher mAuto = AUTOPET_PATTERN.matcher(plain);
            if (mAuto.find()) {
                String petName = mAuto.group(1).trim();
                updatePetDisplay(petName);
            }
        });
        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            if (!PetOverlay.readPetDataConfirmation.isEnabled()) return;
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int chestHeight = 192;
            int chestY = (screenHeight - chestHeight) / 2;

            if (screen instanceof AbstractContainerScreen<?> handled) {
                Component title = handled.getTitle();
                String titleStr = title.getString();

                Matcher pageMatcher = Pattern.compile("\\((\\d+)/(\\d+)\\) Pets?").matcher(titleStr);

                if (pageMatcher.find()) {

                    ScreenEvents.afterExtract(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                        String toggleText = saveData ? "§a§lYES" : "§c§lNO";
                        String fullText = "§d§lNA §f§l» §bSave pets§f: " + toggleText;

                        drawContext.text(Minecraft.getInstance().font, Component.literal(fullText), (screenWidth - Minecraft.getInstance().font.width(fullText)) / 2, chestY - 30, 0xFFFFFFFF, true);
                    });

                    ScreenMouseEvents.afterMouseClick(screen).register((screen2, context, consumed) -> {
                        if (context.button() != 0) return false;

                        double mouseX = context.x();
                        double mouseY = context.y();

                        String toggleText = saveData ? "§a§lYES" : "§c§lNO";
                        String fullText = "§d§lNA §f§l» §bSave pets§f: " + toggleText;
                        int textWidth = Minecraft.getInstance().font.width(fullText);

                        int x1 = (screenWidth - textWidth) / 2;
                        int x2 = x1 + textWidth;
                        int y1 = chestY - 30;
                        int y2 = y1 + 9;

                        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                            if (Minecraft.getInstance().player != null) {
                                Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1.0f);
                            }
                            saveData = !saveData;

                            if (saveData) {
                                LoadPets.forceRescan((AbstractContainerScreen<?>) screen2);
                            }

                            return true;
                        }

                        return false;
                    });
                }
            }
        });
    }

    private void updatePetDisplay(String rawPetName) {
        if (rawPetName == null) return;

        String clean = cleanPetName(rawPetName);
        MutableComponent title = Component.empty();

        PetInfo found = null;
        for (PetInfo p : petCache) {
            if (p.cleanName != null && p.cleanName.equalsIgnoreCase(clean)) {
                found = p;
                break;
            }
        }

        MutableComponent displayName;
        if (found != null) {
            Component petName = stripPetDecorations(parsePetName(found));

            if (PetOverlay.showPetLevel.isEnabled()) {
                displayName = Component.literal("§7[Lvl " + found.level + "] ")
                        .append(petName.copy());
            } else {
                displayName = petName.copy();
            }

            title = petName.copy();


            PET_TEXTURE = found.texture;
            PET_ITEM = HeadUtils.getSkull(PET_TEXTURE);
        } else {
            displayName = Component.literal("§cUnknown");
            PET_TEXTURE = "";
        }

        currentPet = displayName;
        onPetSummoned(title);
    }

    private void onPetSummoned(MutableComponent rawPetName) {
        if (rawPetName == null) return;
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                if (PetOverlay.playSoundOnChange.isEnabled()) customSound1.playTestSound();
                if (PetOverlay.showTitle.isEnabled()) {
                    Minecraft.getInstance().gui.setTitle(Component.literal(" "));
                    Minecraft.getInstance().gui.setSubtitle(rawPetName);
                    Minecraft.getInstance().gui.setTimes(0, 10, 0);
                }
            }
        });
    }

    private void onPetDespawned() {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                if (PetOverlay.playSoundOnChange.isEnabled()) customSound2.playTestSound();
                if (PetOverlay.showTitle.isEnabled()) {
                    Minecraft.getInstance().gui.setTitle(Component.literal(" "));
                    Minecraft.getInstance().gui.setSubtitle(Component.literal("§cNo Pet"));
                    Minecraft.getInstance().gui.setTimes(0, 10, 0);
                }
                PET_TEXTURE = null;
                PET_ITEM = null;
                currentPet = Component.literal("§cNone");
            }
        });
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();

        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        if (PetOverlay.showPetIcon.isEnabled()) {
            if (PET_TEXTURE == null || PET_TEXTURE.isEmpty() || PET_ITEM == null) {
                ctx.item(new ItemStack(Items.PLAYER_HEAD), 0, 0);
            } else {
                ctx.item(PET_ITEM, 0, 0);
            }
        }

        ctx.text(
                Minecraft.getInstance().font,
                currentPet,
                PetOverlay.showPetIcon.isEnabled() ? 16 : 0,
                4,
                0xFFFFFFFF,
                true
        );

        ctx.pose().popMatrix();

        this.width = Math.round(120 * scale);
        this.height = Math.round(20 * scale);
    }

    private Component parsePetName(PetInfo p) {
        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, p.name)
                .result()
                .orElse(Component.literal("Invalid Pet"));
    }

    private Component stripPetDecorations(Component original) {
        MutableComponent cleaned = Component.empty();

        for (Component sibling : original.getSiblings()) {
            String content = sibling.getString().trim();

            if (content.equals("⭐") || content.equals("✦")) continue;
            if (content.matches("\\[Lvl \\d+]")) continue;
            if (content.matches("\\[\\d+✦]")) continue;

            cleaned.append(Component.literal(sibling.getString().trim()).setStyle(sibling.getStyle()));
        }

        return cleaned;
    }


    private String cleanPetName(String name) {
        if (name == null) return "";
        return name.replaceAll("\\[[^\\]]*\\]\\s*", "").trim();
    }

    private String removeFormat(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)\\u00A7[0-9A-FK-OR]", "").trim();
    }
}