package net.otsutsukimiho.nozomiaddon.utils;

public class CheckMarkSetting extends Settings {
    private boolean enabled;

    public CheckMarkSetting(String name, boolean defaultValue) {
        super(name);
        this.enabled = defaultValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }
}