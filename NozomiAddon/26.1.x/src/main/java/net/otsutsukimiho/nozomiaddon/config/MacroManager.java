package net.otsutsukimiho.nozomiaddon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MacroManager {
    private static final Path PATH = Path.of("config/nozomiaddon/macros.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean allowInGui = false;
    public static List<Macro> macros = new ArrayList<>();

    public static class Macro {
        public String command = "";
        public int keycode = -1;
        public boolean enabled = true;

        public String getKeyName() {
            if (keycode == -1) return "NONE";
            if (keycode <= -100) {
                int mouseBtn = Math.abs(keycode + 100);
                if (mouseBtn == 0) return "MB1";
                if (mouseBtn == 1) return "MB2";
                if (mouseBtn == 2) return "MB3";
                if (mouseBtn == 3) return "MB4";
                if (mouseBtn == 4) return "MB5";
                return "MB" + (mouseBtn + 1);
            }
            try {
                return InputConstants.Type.KEYSYM.getOrCreate(keycode).getDisplayName().getString().toUpperCase();
            } catch (Exception e) {
                return "UNKNOWN";
            }
        }
    }

    private static class SaveData {
        public boolean allowInGui;
        public List<Macro> macros;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                String json = Files.readString(PATH);
                Type type = new TypeToken<SaveData>(){}.getType();
                SaveData data = GSON.fromJson(json, type);
                if (data != null) {
                    allowInGui = data.allowInGui;
                    if (data.macros != null) macros = data.macros;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            SaveData data = new SaveData();
            data.allowInGui = allowInGui;
            data.macros = macros;
            Files.writeString(PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}