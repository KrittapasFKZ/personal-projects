package net.otsutsukimiho.nozomiaddon.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.otsutsukimiho.nozomiaddon.features.PetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadPets {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_DIR =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("nozomiaddon");
    private static final Path DATA_FILE = DATA_DIR.resolve("pets.json");

    private static final List<PetData> collectedPets = new ArrayList<>();
    private static final Set<Integer> scannedPages = new HashSet<>();
    private static int currentPetPage = 1;
    private static int totalPetPages = 1;

    public static void initClient() {
        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {

            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}

                if (screen instanceof AbstractContainerScreen<?> handled) {
                    scanInventory(handled);
                }

            }, "na-load pet").start();

        });
    }

    private static void scanInventory(AbstractContainerScreen<?> handled) {
        if (handled.getMenu() == null) return;

        String title = handled.getTitle().getString();
        var handler = handled.getMenu();
        var slots = handler.slots;

        Matcher pageMatcher = Pattern.compile("\\((\\d+)/(\\d+)\\) Pets?").matcher(title);

        if (pageMatcher.find()) {
            currentPetPage = Integer.parseInt(pageMatcher.group(1));
            totalPetPages = Integer.parseInt(pageMatcher.group(2));
        } else if (title.contains("Pet")) {
            currentPetPage = 1;
            totalPetPages = 1;
        } else {
            if (!collectedPets.isEmpty()) {
                collectedPets.clear();
                scannedPages.clear();
            }
            return;
        }

        if (scannedPages.contains(currentPetPage)) {
            return;
        }

        if (slots.size() > 4) {
            var bone = slots.get(4);
            if (!bone.getItem().getItem().toString().contains("bone")) {
                return;
            }
        }

        for (int i = 0; i < slots.size(); i++) {
            var stack = slots.get(i);
            if (!stack.hasItem()) continue;
            String rawName = stack.getItem().getHoverName().getString();

            Matcher lvlMatcher = Pattern.compile("\\[Lvl (\\d+)] (.+)").matcher(rawName);

            if (!lvlMatcher.find()) continue;

            int level = Integer.parseInt(lvlMatcher.group(1));

            String cleanName = rawName
                    .replace("⭐", "")
                    .replace("✦", "")
                    .replaceAll("\\[Lvl \\d+]", "")
                    .trim();

            Matcher cosMatcher = Pattern.compile("\\[(\\d+)✦] (.+)").matcher(cleanName);
            String finalName = cleanName;
            if (cosMatcher.find()) finalName = cosMatcher.group(2) + " ✦";

            String texture = "None";
            ResolvableProfile profileComponent = stack.getItem().get(DataComponents.PROFILE);
            if (profileComponent != null && profileComponent.partialProfile() != null) {
                GameProfile profile = profileComponent.partialProfile();

                if (profile.properties().containsKey("textures")) {
                    for (Property property : profile.properties().get("textures")) {
                        texture = property.value();
                        break;
                    }
                }
            }

            var lores = stack.getItem().getComponents().get(DataComponents.LORE);
            String heldItem = "§cNone";

            if (lores != null) {
                for (Component line : lores.lines()) {
                    String lineText = line.getString();
                    if (lineText.contains("Held Item: ")) {
                        heldItem = lineText.split("Held Item: ")[1];
                        break;
                    }
                }
            }

            boolean hasSkin = rawName.contains("✦");
            if (hasSkin) {
                cleanName = cleanName + " ✦";
            }

            Component nameText = stack.getItem().getHoverName();

            var encoded = ComponentSerialization.CODEC
                    .encodeStart(JsonOps.INSTANCE, nameText)
                    .result()
                    .orElse(null);

            collectedPets.add(new PetData(i,
                    encoded,
                    level,
                    cleanName,
                    heldItem,
                    hasSkin,
                    texture.equals("None") ? null : texture)
            );
        }
        scannedPages.add(currentPetPage);

        if (currentPetPage == totalPetPages) {
            savePets();
        }
    }

    private static void savePets() {
        if (PetOverlay.readPetDataConfirmation.isEnabled() && !PetOverlay.saveData) {
            collectedPets.clear();
            scannedPages.clear();
            return;
        }

        Set<String> seen = new HashSet<>();
        List<PetData> uniquePets = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();

        for (PetData pet : collectedPets) {
            String key = pet.slot + "-" + pet.name + "-" + pet.level + "-" + pet.cleanName + "-" + pet.item;
            if (!seen.contains(key)) {
                seen.add(key);
                uniquePets.add(pet);
            }
        }

        try {
            Files.writeString(DATA_FILE, GSON.toJson(uniquePets));
            PetOverlay.saveData = false;
            if (client.player != null) {
                client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §aLoaded §f" + uniquePets.size() + " §apets!"));
                client.player.playSound(SoundEvents.BOOK_PAGE_TURN, 1, 1.0f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        collectedPets.clear();
        scannedPages.clear();

    }

    public static void forceRescan(AbstractContainerScreen<?> handled) {
        scannedPages.remove(currentPetPage);
        scanInventory(handled);
    }

    public static class PetData {
        int slot;
        JsonElement name;
        String cleanName;
        int level;
        String item;
        boolean appliedSkin;
        String texture;

        public PetData(int slot, JsonElement name, int level, String cleanName, String item, boolean appliedSkin, String texture) {
            this.slot = slot;
            this.name = name;
            this.cleanName = cleanName;
            this.level = level;
            this.item = item;
            this.appliedSkin = appliedSkin;
            this.texture = texture;
        }
    }
}