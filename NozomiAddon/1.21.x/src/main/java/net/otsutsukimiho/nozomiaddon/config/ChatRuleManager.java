package net.otsutsukimiho.nozomiaddon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatRuleManager {
    public static boolean enabled = true;
    public static List<ChatRule> rules = new ArrayList<>();

    private static final File CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("nozomiaddon").toFile();
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "chatRules.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data != null) {
                enabled = data.enabled;
                if (data.rules != null) {
                    rules = data.rules;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            SaveData data = new SaveData();
            data.enabled = enabled;
            data.rules = rules;

            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ChatRule {
        public String triggerMessage = "";
        public String actionType = "Alert Title";
        public String actionArg = "";

        public ChatRule() {}

        public ChatRule(String triggerMessage, String actionType, String actionArg) {
            this.triggerMessage = triggerMessage;
            this.actionType = actionType;
            this.actionArg = actionArg;
        }
    }

    private static class SaveData {
        public boolean enabled;
        public List<ChatRule> rules;
    }
}