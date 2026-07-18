package net.otsutsukimiho.nozomiaddon.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiSelectSetting extends Settings {
    private final List<String> options;
    private final List<String> selectedOptions;

    public MultiSelectSetting(String name, List<String> options, String... defaultSelected) {
        super(name);
        this.options = new ArrayList<>(options);
        this.selectedOptions = new ArrayList<>(Arrays.asList(defaultSelected));
    }

    public List<String> getOptions() {
        return options;
    }

    public List<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void toggleOption(String option) {
        if (selectedOptions.contains(option)) {
            selectedOptions.remove(option);
        } else {
            selectedOptions.add(option);
        }
    }

    public boolean isSelected(String option) {
        return selectedOptions.contains(option);
    }

    public void setSelectedOptions(List<String> selected) {
        this.selectedOptions.clear();
        if (selected != null) {
            for (String s : selected) {
                if (options.contains(s)) {
                    this.selectedOptions.add(s);
                }
            }
        }
    }
}