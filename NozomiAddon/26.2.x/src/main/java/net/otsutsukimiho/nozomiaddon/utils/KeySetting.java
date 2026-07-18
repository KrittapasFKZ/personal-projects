package net.otsutsukimiho.nozomiaddon.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
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
        if (Minecraft.getInstance().getWindow() == null) return false;

        long handle = Minecraft.getInstance().getWindow().handle();

        return GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS;
    }

    public String getKeyName() {
        if (code == -1) return "NONE";

        try {
            return InputConstants.Type.KEYSYM.getOrCreate(code).getDisplayName().getString().toUpperCase();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}