package net.otsutsukimiho.nozomiaddon.config;

public class UiElementConfig {
    public int x;
    public int y;
    public int width;
    public int height;
    public int size;

    public UiElementConfig() {
        this(0, 0, 100, 10, 10);
    }

    public UiElementConfig(int x, int y, int width, int height, int size) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.size = size;
    }
}
