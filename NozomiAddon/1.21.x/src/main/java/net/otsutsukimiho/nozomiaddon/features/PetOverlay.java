package net.otsutsukimiho.nozomiaddon.features;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

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
    private MutableText currentPet = Text.literal("§cNone");

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
        ScreenEvents.AFTER_INIT.register((MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            if (!PetOverlay.readPetDataConfirmation.isEnabled()) return;
            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
            int chestHeight = 192;
            int chestY = (screenHeight - chestHeight) / 2;

            if (screen instanceof HandledScreen<?> handled) {
                Text title = handled.getTitle();
                String titleStr = title.getString();

                Matcher pageMatcher = Pattern.compile("Pets? \\((\\d+)/(\\d+)\\)").matcher(titleStr);

                if (pageMatcher.find()) {

                    ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, delta) -> {
                        String toggleText = saveData ? "§a§lYES" : "§c§lNO";
                        String fullText = "§d§lNA §f§l» §bSave pets§f: " + toggleText;

                        drawContext.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(fullText), (screenWidth - MinecraftClient.getInstance().textRenderer.getWidth(fullText)) / 2, chestY - 30, 0xFFFFFFFF, true);
                    });

                    ScreenMouseEvents.afterMouseClick(screen).register((screen2, context, consumed) -> {
                        if (context.button() != 0) return false;

                        double mouseX = context.x();
                        double mouseY = context.y();

                        String toggleText = saveData ? "§a§lYES" : "§c§lNO";
                        String fullText = "§d§lNA §f§l» §bSave pets§f: " + toggleText;
                        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(fullText);

                        int x1 = (screenWidth - textWidth) / 2;
                        int x2 = x1 + textWidth;
                        int y1 = chestY - 30;
                        int y2 = y1 + 9;

                        if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
                            if (MinecraftClient.getInstance().player != null) {
                                MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1.0f);
                            }
                            saveData = !saveData;

                            if (saveData) {
                                LoadPets.forceRescan((HandledScreen<?>) screen2);
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
        MutableText title = Text.empty();

        PetInfo found = null;
        for (PetInfo p : petCache) {
            if (p.cleanName != null && p.cleanName.equalsIgnoreCase(clean)) {
                found = p;
                break;
            }
        }

        MutableText displayName;
        if (found != null) {
            Text petName = stripPetDecorations(parsePetName(found));

            if (PetOverlay.showPetLevel.isEnabled()) {
                displayName = Text.literal("§7[Lvl " + found.level + "] ")
                        .append(petName.copy());
            } else {
                displayName = petName.copy();
            }

            title = petName.copy();


            PET_TEXTURE = found.texture;
            PET_ITEM = HeadUtils.getSkull(PET_TEXTURE);
        } else {
            displayName = Text.literal("§cUnknown");
            PET_TEXTURE = "";
        }

        currentPet = displayName;
        onPetSummoned(title);
    }

    private void onPetSummoned(MutableText rawPetName) {
        if (rawPetName == null) return;
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) {
                if (PetOverlay.playSoundOnChange.isEnabled()) customSound1.playTestSound();
                if (PetOverlay.showTitle.isEnabled()) {
                    MinecraftClient.getInstance().inGameHud.setTitle(Text.literal(" "));
                    MinecraftClient.getInstance().inGameHud.setSubtitle(rawPetName);
                    MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 10, 0);
                }
            }
        });
    }

    private void onPetDespawned() {
        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) {
                if (PetOverlay.playSoundOnChange.isEnabled()) customSound2.playTestSound();
                if (PetOverlay.showTitle.isEnabled()) {
                    MinecraftClient.getInstance().inGameHud.setTitle(Text.literal(" "));
                    MinecraftClient.getInstance().inGameHud.setSubtitle(Text.literal("§cNo Pet"));
                    MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 10, 0);
                }
                PET_TEXTURE = null;
                PET_ITEM = null;
                currentPet = Text.literal("§cNone");
            }
        });
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();

        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        if (PetOverlay.showPetIcon.isEnabled()) {
            if (PET_TEXTURE == null || PET_TEXTURE.isEmpty() || PET_ITEM == null) {
                ctx.drawItem(new ItemStack(Items.PLAYER_HEAD), 0, 0);
            } else {
                ctx.drawItem(PET_ITEM, 0, 0);
            }
        }

        ctx.drawText(
                MinecraftClient.getInstance().textRenderer,
                currentPet,
                PetOverlay.showPetIcon.isEnabled() ? 16 : 0,
                4,
                0xFFFFFFFF,
                true
        );

        ctx.getMatrices().popMatrix();

        this.width = Math.round(120 * scale);
        this.height = Math.round(20 * scale);
    }

    private Text parsePetName(PetInfo p) {
        return TextCodecs.CODEC
                .parse(JsonOps.INSTANCE, p.name)
                .result()
                .orElse(Text.literal("Invalid Pet"));
    }

    private Text stripPetDecorations(Text original) {
        MutableText cleaned = Text.empty();

        for (Text sibling : original.getSiblings()) {
            String content = sibling.getString().trim();

            if (content.equals("⭐") || content.equals("✦")) continue;
            if (content.matches("\\[Lvl \\d+]")) continue;
            if (content.matches("\\[\\d+✦]")) continue;

            cleaned.append(Text.literal(sibling.getString().trim()).setStyle(sibling.getStyle()));
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