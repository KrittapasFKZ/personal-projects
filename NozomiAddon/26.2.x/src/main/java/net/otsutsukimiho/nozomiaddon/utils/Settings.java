package net.otsutsukimiho.nozomiaddon.utils;

import java.util.function.Supplier;

public class Settings {
    public String name;
    public Supplier<Boolean> visible;

    public Settings(String name) {
        this.name = name;
        this.visible = () -> true;
    }

    public boolean isVisible() {
        return visible.get();
    }
}