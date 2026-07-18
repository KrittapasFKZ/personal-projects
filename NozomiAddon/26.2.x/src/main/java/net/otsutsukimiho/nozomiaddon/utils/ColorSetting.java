package net.otsutsukimiho.nozomiaddon.utils;

import java.awt.Color;

public class ColorSetting extends Settings {
    private Color color;
    private boolean chroma;

    public ColorSetting(String name, Color defaultColor, boolean defaultChroma) {
        super(name);
        this.color = defaultColor;
        this.chroma = defaultChroma;
    }

    public Color getColor() { return color; }
    public int getRGB() { return color.getRGB(); }
    public boolean isChroma() { return chroma; }

    public void setColor(Color color) { this.color = color; }
    public void setChroma(boolean chroma) { this.chroma = chroma; }
}