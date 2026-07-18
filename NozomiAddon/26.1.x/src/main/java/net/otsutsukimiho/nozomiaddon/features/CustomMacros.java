package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.config.*;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMacros implements FeatureManager.Feature {
    private boolean enabled = false;
    private final Map<Integer, Boolean> pressedStates = new HashMap<>();

    public static BooleanSetting dummy = new BooleanSetting("dummy", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(dummy);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void initClient() {
        MacroManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (client.screen != null && !MacroManager.allowInGui) return;
            long windowHandle = client.getWindow().handle();

            for (MacroManager.Macro macro : MacroManager.macros) {
                if (!macro.enabled || macro.keycode == -1 || macro.command.isEmpty()) continue;

                boolean isDown;
                if (macro.keycode <= -100) {
                    int mouseButton = Math.abs(macro.keycode + 100);
                    isDown = GLFW.glfwGetMouseButton(windowHandle, mouseButton) == GLFW.GLFW_PRESS;
                } else {
                    isDown = GLFW.glfwGetKey(windowHandle, macro.keycode) == GLFW.GLFW_PRESS;
                }
                boolean wasDown = pressedStates.getOrDefault(macro.keycode, false);

                if (isDown && !wasDown) {
                    executeMacro(client, macro.command);
                }

                pressedStates.put(macro.keycode, isDown);
            }
        });
    }

    private void executeMacro(Minecraft client, String command) {
        if (client.player == null) return;
        if (command.startsWith("/")) {
            client.player.connection.sendCommand(command.substring(1));
        } else {
            client.player.connection.sendChat(command);
        }
    }
}