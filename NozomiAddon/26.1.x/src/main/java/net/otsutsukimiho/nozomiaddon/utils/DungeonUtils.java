package net.otsutsukimiho.nozomiaddon.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonUtils {
    public long bossEntry = -1;
    public long bloodOpened = -1;
    public long watcherSpawned = -1;
    public long watcherCleared = -1;
    public long runStarted = -1;
    public long runEnded = -1;
    public long tickElapsed = 0;
    public int openedWitherDoors = 0;
    public int crypts = 0;
    public boolean inDungeon = false;
    public boolean checkOnce = true;
    private final List<Pattern> entryPatterns = new ArrayList<>();

    private static final Pattern CRYPT_PATTERN = Pattern.compile("Crypts: (\\d+)");

    public DungeonUtils() {
        String[] regexes = new String[] {
                "^\\[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable\\.$",
                "^\\[BOSS] Scarf: This is where the journey ends for you, Adventurers\\.$",
                "^\\[BOSS] The Professor: I was burdened with terrible news recently\\.\\.\\.$",
                "^\\[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!$",
                "^\\[BOSS] Livid: Welcome, you've arrived right on time\\. I am Livid, the Master of Shadows\\.$",
                "^\\[BOSS] Sadan: So you made it all the way here\\.\\.\\. Now you wish to defy me\\? Sadan\\?!$",
                "^\\[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!$"
        };
        for (String r : regexes)
            entryPatterns.add(Pattern.compile(r));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!this.inDungeon) return;
            String msg = message.getString();
            for (Pattern p : entryPatterns) {
                if (p.matcher(msg).matches()) {
                    this.bossEntry = System.currentTimeMillis();
                    return;
                }
            }
            if (this.bloodOpened == -1
                    && msg.matches("^\\[BOSS] The Watcher: .+$")) {
                this.bloodOpened = System.currentTimeMillis();
            }
            if (msg.matches("\\[BOSS] The Watcher: That will be enough for now\\.")) {
                this.watcherSpawned = System.currentTimeMillis();
            }
            if (msg.matches("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\.")) {
                this.watcherCleared = System.currentTimeMillis();
            }
            if (this.runStarted == -1) {
                Minecraft client = Minecraft.getInstance();
                if (client.getConnection() != null) {
                    Scoreboard scoreboard = client.getConnection().scoreboard();
                    Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
                    if (objective != null) {
                        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
                            String owner = entry.owner();
                            PlayerTeam team = scoreboard.getPlayersTeam(owner);
                            Component fullText = PlayerTeam.formatNameForTeam(team, Component.literal(owner));
                            String lineString = fullText.getString();
                            lineString = lineString.replaceAll("§.", "");
                            if (lineString.contains("Time Elapsed")) {
                                this.runStarted = System.currentTimeMillis();
                            }
                        }
                    }
                }
            }
            if (msg.contains("EXTRA STATS") && this.runStarted != -1) {
                this.runEnded = System.currentTimeMillis();
            }
            if (msg.matches(".+ opened a WITHER door!")) {
                this.openedWitherDoors++;
            }
        });
        ClientTickEvents.END_LEVEL_TICK.register(c -> {
            if (this.checkOnce) {
                this.inDungeon = checkInDungeon();
            }
            if (!this.inDungeon) return;
            updateCryptCount();
            if (this.runStarted != -1 && this.runEnded == -1) {
                this.tickElapsed++;
            }
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            this.bossEntry = -1;
            this.bloodOpened = -1;
            this.watcherSpawned = -1;
            this.watcherCleared = -1;
            this.runStarted = -1;
            this.runEnded = -1;
            this.openedWitherDoors = 0;
            this.crypts = 0;
            this.tickElapsed = 0;
            this.inDungeon = false;
            this.checkOnce = true;
        });
    }

    private void updateCryptCount() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) return;

        Collection<PlayerInfo> entries = client.getConnection().getOnlinePlayers();

        for (PlayerInfo entry : entries) {
            Component displayName = entry.getTabListDisplayName();
            String text = (displayName != null) ? displayName.getString() : entry.getProfile().name();
            String cleanText = text.replaceAll("§.", "").trim();

            if (cleanText.contains("Crypts:")) {
                Matcher matcher = CRYPT_PATTERN.matcher(cleanText);
                if (matcher.find()) {
                    this.crypts = Integer.parseInt(matcher.group(1));
                }
                break;
            }
        }
    }

    public static String getCurrentDungeon() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) return null;
        Scoreboard scoreboard = client.getConnection().scoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return null;
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            String owner = entry.owner();
            PlayerTeam team = scoreboard.getPlayersTeam(owner);
            Component fullText = PlayerTeam.formatNameForTeam(team, Component.literal(owner));
            String lineString = fullText.getString();
            lineString = lineString.replaceAll("§.", "");
            if (lineString.contains("The Catacombs")) {
                Pattern pattern = Pattern.compile("\\((.*?)\\)");
                Matcher matcher = pattern.matcher(lineString);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    public boolean checkInDungeon() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null) return false;
        Scoreboard scoreboard = client.getConnection().scoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return false;
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            String owner = entry.owner();
            PlayerTeam team = scoreboard.getPlayersTeam(owner);
            Component fullText = PlayerTeam.formatNameForTeam(team, Component.literal(owner));
            String lineString = fullText.getString();
            lineString = lineString.replaceAll("§.", "");
            if (lineString.contains("The Catacombs")) {
                this.checkOnce = false;
                return true;
            }
        }
        return false;
    }

}