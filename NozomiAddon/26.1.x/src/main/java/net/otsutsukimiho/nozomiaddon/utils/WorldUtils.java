package net.otsutsukimiho.nozomiaddon.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldUtils {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Minecraft client = Minecraft.getInstance();
    private static String world = "None";
    private static int tier = 0;
    private static int noFind = 0;

    public static String getWorld() { return world; }
    public static int getTier() { return tier; }
    private static int findWorldCooldown = 0;

    public static String findZone() {
        try {
            if (client.player == null || client.getConnection() == null) return "None";

            List<PlayerInfo> entries = new java.util.ArrayList<>(client.getConnection().getOnlinePlayers());
            for (PlayerInfo entry : entries) {
                String name = (entry.getTabListDisplayName() != null) ? entry.getTabListDisplayName().getString() : entry.getProfile().name();
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

            if (client.player == null || client.getConnection() == null) return;

            List<PlayerInfo> entries = new java.util.ArrayList<>(client.getConnection().getOnlinePlayers());
            String found = entries.stream()
                    .map(e -> (e.getTabListDisplayName() != null) ? e.getTabListDisplayName().getString() : e.getProfile().name())
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

    public static void sendWorldToPlayer(LocalPlayer player) {
        if (player != null) {
            noFind = 0;
            findWorld();
            client.gui.getChat().addClientSystemMessage(Component.nullToEmpty("§d§lNA §f§l» §aCurrent World is §b" + world));
        }
    }
}