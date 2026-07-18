package net.otsutsukimiho.nozomiaddon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static String world = "None";
    private static int tier = 0;
    private static int noFind = 0;

    public static String getWorld() { return world; }
    public static int getTier() { return tier; }
    private static int findWorldCooldown = 0;

    public static String findZone() {
        try {
            if (client.player == null || client.getNetworkHandler() == null) return "None";

            List<PlayerListEntry> entries = new java.util.ArrayList<>(client.getNetworkHandler().getPlayerList());
            for (PlayerListEntry entry : entries) {
                String name = (entry.getDisplayName() != null) ? entry.getDisplayName().getString() : entry.getProfile().name();
                if (name.contains("⏣") || name.contains("ф")) return name;
            }
        } catch (Exception e) { throw e; }
        return "None";
    }

    public static void findWorld() {
        findWorldCooldown = 0;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (findWorldCooldown > 0) {
                findWorldCooldown--;
                return;
            }
            findWorldCooldown = 20;

            if (noFind >= 10) return;
            noFind++;

            if (client.player == null || client.getNetworkHandler() == null) return;

            List<PlayerListEntry> entries = new java.util.ArrayList<>(client.getNetworkHandler().getPlayerList());
            String found = entries.stream()
                    .map(e -> (e.getDisplayName() != null) ? e.getDisplayName().getString() : e.getProfile().name())
                    .filter(s -> s.contains("Area:") || s.contains("Dungeon:"))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                world = found.substring(found.indexOf(": ") + 2);
                noFind = 10;
            }
        });
    }

    public static void onWorldLoad() {
        noFind = 0;
        findWorld();
    }

    public static void reset() {
        world = "None";
    }

    public static void checkMineshaft() {
        noFind = 0;
        findWorld();
    }

    public static void sendWorldToPlayer(ClientPlayerEntity player) {
        if (player != null) {
            noFind = 0;
            findWorld();
            player.sendMessage(Text.of("§d§lNA §f§l» §aCurrent World is §b" + world), false);
        }
    }
}