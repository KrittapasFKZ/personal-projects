package net.otsutsukimiho.nozomiaddon.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagSetting extends Settings {
    private List<String> tags = new ArrayList<>();
    private final List<String> defaultTags;

    public TagSetting(String name, String... defaultTags) {
        super(name);
        this.defaultTags = Arrays.asList(defaultTags);
        this.tags.addAll(this.defaultTags);
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean containsTag(String check) {
        return tags.stream().anyMatch(t -> t.equalsIgnoreCase(check.trim()));
    }

    public String getTagsAsString() {
        return String.join(", ", tags);
    }

    public void setTagsFromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            this.tags.clear();
            return;
        }
        this.tags = Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}