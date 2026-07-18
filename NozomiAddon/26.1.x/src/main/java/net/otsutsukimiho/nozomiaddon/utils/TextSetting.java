package net.otsutsukimiho.nozomiaddon.utils;

import java.util.Map;
import java.util.function.Supplier;

public class TextSetting extends Settings {
    private String value;
    private final String defaultValue;
    private final Map<String, Supplier<String>> previewPlaceholders;

    public TextSetting(String name, String defaultValue, Map<String, Supplier<String>> previewPlaceholders) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.previewPlaceholders = previewPlaceholders;
    }

    public TextSetting(String name, String defaultValue) {
        super(name);
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.previewPlaceholders = null;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Map<String, Supplier<String>> getPreviewPlaceholders() { return previewPlaceholders; }
}