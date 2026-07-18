package net.otsutsukimiho.nozomiaddon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.otsutsukimiho.nozomiaddon.features.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/nozomiaddon/features.json");

    private static final Map<String, Boolean> features = new LinkedHashMap<>();
    private static final Map<String, UiElementConfig> elements = new LinkedHashMap<>();
    private static final Map<String, Integer> colors = new LinkedHashMap<>();

    private static final Map<String, Map<String, Object>> featureSettings = new LinkedHashMap<>();

    public static void init() {
        load();
    }

    private static String simplify(String key) {
        if (key.contains(" - ")) {
            return key.split(" - ", 2)[1];
        }
        return key;
    }

    public static boolean get(String key) {
        return features.getOrDefault(simplify(key), false);
    }

    public static void set(String key, boolean value) {
        features.put(simplify(key), value);
        save();
    }

    public static Map<String, Boolean> getAllFeatures() {
        return new LinkedHashMap<>(features);
    }

    public static UiElementConfig getElement(String key) {
        return elements.getOrDefault(key, new UiElementConfig());
    }

    public static void setElement(String key, UiElementConfig config) {
        elements.put(key, config);
        save();
    }

    public static void setColor(String key, int color) {
        colors.put(simplify(key), color);
        save();
    }

    public static int getColor(String key) {
        return colors.getOrDefault(simplify(key), 0xFF55FF55);
    }

    private static class FullConfig {
        Map<String, Boolean> features = new LinkedHashMap<>();
        Map<String, UiElementConfig> elements = new LinkedHashMap<>();
        Map<String, Integer> colors = new LinkedHashMap<>();
        Map<String, Map<String, Object>> featureSettings = new LinkedHashMap<>();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            syncSettingsFromFeatures();

            FullConfig full = new FullConfig();
            full.features = features;
            full.elements = elements;
            full.colors = colors;
            full.featureSettings = featureSettings;

            String json = GSON.toJson(full);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    private static void syncSettingsFromFeatures() {
        for (Map.Entry<String, FeatureManager.Feature> entry : FeatureManager.getRegistered().entrySet()) {
            String featureName = simplify(entry.getKey());
            Map<String, Object> settingsMap = new LinkedHashMap<>();

            for (Settings setting : entry.getValue().getSettings()) {
                if (setting instanceof BooleanSetting) {
                    settingsMap.put(setting.name, ((BooleanSetting) setting).isEnabled());
                } else if (setting instanceof CheckMarkSetting) {
                    settingsMap.put(setting.name, ((CheckMarkSetting) setting).isEnabled());
                } else if (setting instanceof NumberSetting) {
                    settingsMap.put(setting.name, ((NumberSetting) setting).getValue());
                } else if (setting instanceof ModeSetting) {
                    settingsMap.put(setting.name, ((ModeSetting) setting).getMode());
                } else if (setting instanceof ColorSetting) {
                    settingsMap.put(setting.name, ((ColorSetting) setting).getRGB());
                } else if (setting instanceof StringSetting) {
                    settingsMap.put(setting.name, ((StringSetting) setting).getValue());
                } else if (setting instanceof TextSetting textSetting) {
                    settingsMap.put(setting.name, textSetting.getValue());
                } else if (setting instanceof TagSetting tagSetting) {
                    settingsMap.put(setting.name, tagSetting.getTags());
                } else if (setting instanceof MultiSelectSetting multi) {
                    settingsMap.put(setting.name, multi.getSelectedOptions());
                } else if (setting instanceof FloatSetting) {
                    settingsMap.put(setting.name, ((FloatSetting) setting).getValue());
                } else if (setting instanceof KeySetting) {
                    settingsMap.put(setting.name, ((KeySetting) setting).getCode());
                } else if (setting instanceof RangeSetting rs) {
                    settingsMap.put(setting.name, List.of(rs.getMin(), rs.getMax()));
                } else if (setting instanceof SoundSetting ss) {
                    settingsMap.put(setting.name, Map.of(
                            "id", ss.getSoundId(),
                            "vol", ss.getVolume(),
                            "pitch", ss.getPitch()
                    ));
                }
            }
            if (!settingsMap.isEmpty()) {
                featureSettings.put(featureName, settingsMap);
            }
        }
    }

    private static void syncSettingsToFeatures() {
        for (Map.Entry<String, FeatureManager.Feature> entry : FeatureManager.getRegistered().entrySet()) {
            String featureName = simplify(entry.getKey());
            Map<String, Object> savedSettings = featureSettings.get(featureName);

            if (savedSettings == null) continue;

            for (Settings setting : entry.getValue().getSettings()) {
                if (savedSettings.containsKey(setting.name)) {
                    Object val = savedSettings.get(setting.name);

                    try {
                        if (setting instanceof BooleanSetting && val instanceof Boolean) {
                            ((BooleanSetting) setting).setEnabled((Boolean) val);
                        } else if (setting instanceof NumberSetting && val instanceof Number) {
                            ((NumberSetting) setting).setValue(((Number) val).intValue());
                        } else if (setting instanceof CheckMarkSetting && val instanceof Boolean) {
                            ((CheckMarkSetting) setting).setEnabled((Boolean) val);
                        } else if (setting instanceof ColorSetting && val instanceof Number) {
                            ((ColorSetting) setting).setColor(new java.awt.Color(((Number) val).intValue(), true));
                        } else if (setting instanceof StringSetting && val instanceof String) {
                            ((StringSetting) setting).setValue((String) val);
                        } else if (setting instanceof MultiSelectSetting multi && val instanceof List<?> list) {
                            List<String> strList = new java.util.ArrayList<>();
                            for (Object o : list) strList.add(String.valueOf(o));
                            multi.setSelectedOptions(strList);
                        } else if (setting instanceof TagSetting tagSetting && val instanceof List<?> list) {
                            String joined = list.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(", "));
                            tagSetting.setTagsFromString(joined);
                        } else if (setting instanceof FloatSetting && val instanceof Number) {
                            ((FloatSetting) setting).setValue(((Number) val).floatValue());
                        } else if (setting instanceof ModeSetting && val instanceof String) {
                            ((ModeSetting) setting).setMode((String) val);
                        } else if (setting instanceof TextSetting textSetting && val instanceof String) {
                            textSetting.setValue((String) val);
                        } else if (setting instanceof KeySetting && val instanceof Number) {
                            ((KeySetting) setting).setCode(((Number) val).intValue());
                        } else if (setting instanceof RangeSetting rs && val instanceof List<?> list) {
                            if (list.size() >= 2) {
                                rs.setMin(((Number) list.get(0)).intValue());
                                rs.setMax(((Number) list.get(1)).intValue());
                            }
                        } else if (setting instanceof SoundSetting ss && val instanceof Map<?, ?> map) {
                            if (map.containsKey("id")) ss.setSoundId((String) map.get("id"));
                            if (map.containsKey("vol")) ss.setVolume(((Number) map.get("vol")).floatValue());
                            if (map.containsKey("pitch")) ss.setPitch(((Number) map.get("pitch")).floatValue());
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to load setting: {}", setting.name, e);
                    }
                }
            }
        }
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                Type type = new TypeToken<FullConfig>(){}.getType();
                FullConfig full = GSON.fromJson(json, type);
                if (full != null) {
                    features.clear();
                    if (full.features != null) features.putAll(full.features);

                    elements.clear();
                    if (full.elements != null) elements.putAll(full.elements);

                    colors.clear();
                    if (full.colors != null) colors.putAll(full.colors);

                    featureSettings.clear();
                    if (full.featureSettings != null) featureSettings.putAll(full.featureSettings);

                    syncSettingsToFeatures();
                }
            } else {
                save();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }
}
