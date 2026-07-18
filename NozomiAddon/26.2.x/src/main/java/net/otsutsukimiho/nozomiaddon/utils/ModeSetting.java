package net.otsutsukimiho.nozomiaddon.utils;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Settings {
    private int index;
    private final List<String> modes;

    public ModeSetting(String name, String current, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(current);
    }

    public String getMode() {
        return modes.get(index);
    }

    public List<String> getModes() {
        return modes;
    }

    public void cycle() {
        if (index < modes.size() - 1) index++;
        else index = 0;
    }

    public void setMode(String mode) {
        for (int i = 0; i < modes.size(); i++) {
            if (modes.get(i).equalsIgnoreCase(mode)) {
                this.index = i;
                return;
            }
        }
    }

    public boolean is(String mode) {
        return modes.get(index).equalsIgnoreCase(mode);
    }
}