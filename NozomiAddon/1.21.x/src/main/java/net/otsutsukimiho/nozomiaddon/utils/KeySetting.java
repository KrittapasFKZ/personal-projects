package net.otsutsukimiho.nozomiaddon.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeySetting extends Settings {
    private int code;

    public KeySetting(String name, int defaultCode) {
        super(name);
        this.code = defaultCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isPressed() {
        if (code == -1) return false;
        if (MinecraftClient.getInstance().getWindow() == null) return false;

        long handle = MinecraftClient.getInstance().getWindow().getHandle();

        return GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS;
    }

    public String getKeyName() {
        if (code == -1) return "NONE";

        try {
            return InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText().getString().toUpperCase();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}