package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
            if (!enabled || client.getWindow() == null) return;
            if (client.currentScreen != null && !MacroManager.allowInGui) return;
            long windowHandle = client.getWindow().getHandle();

            for (MacroManager.Macro macro : MacroManager.macros) {
                if (!macro.enabled || macro.keycode == -1 || macro.command.isEmpty()) continue;

                boolean isDown = GLFW.glfwGetKey(windowHandle, macro.keycode) == GLFW.GLFW_PRESS;
                boolean wasDown = pressedStates.getOrDefault(macro.keycode, false);

                if (isDown && !wasDown) {
                    executeMacro(client, macro.command);
                }

                pressedStates.put(macro.keycode, isDown);
            }
        });
    }

    private void executeMacro(MinecraftClient client, String command) {
        if (client.player == null || client.player.networkHandler == null) return;
        if (command.startsWith("/")) {
            client.player.networkHandler.sendChatCommand(command.substring(1));
        } else {
            client.player.networkHandler.sendChatMessage(command);
        }
    }
}