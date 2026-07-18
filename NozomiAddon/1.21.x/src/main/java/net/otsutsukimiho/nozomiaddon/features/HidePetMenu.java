package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HidePetMenu implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private volatile boolean enabled = false;
    private boolean isPetMenu = false;

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.BONE);
    }

    public void initClient() {
        ScreenEvents.AFTER_INIT.register((MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseX, mouseY) -> {
                if (screen instanceof HandledScreen<?> handled) {
                    var handler = handled.getScreenHandler();
                    Text title = handled.getTitle();
                    String titleStr = title.getString();
                    if (titleStr.contains("Pets")) {
                        isPetMenu = true;
                        var stack = handler.getCursorStack();
                        if (stack != null && isPetMenu) {
                            var lore = stack.getComponents().get(DataComponentTypes.LORE);
                            if (lore != null && !lore.lines().isEmpty()) {
                                Text firstLine = lore.lines().getLast();
                                if (firstLine != null && firstLine.getString().contains("Right-click to convert to an item!")) {
                                    if (MinecraftClient.getInstance().player != null) {
                                        MinecraftClient.getInstance().player.closeHandledScreen();
                                        MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(),1f,1f);
                                        isPetMenu = false;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            });
        });
    }
}