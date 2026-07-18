package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;
            ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseX, mouseY) -> {
                if (screen instanceof AbstractContainerScreen<?> handled) {
                    var handler = handled.getMenu();
                    Component title = handled.getTitle();
                    String titleStr = title.getString();
                    if (titleStr.contains("Pets")) {
                        isPetMenu = true;
                        var stack = handler.getCarried();
                        if (stack != null && isPetMenu) {
                            var lore = stack.getComponents().get(DataComponents.LORE);
                            if (lore != null && !lore.lines().isEmpty()) {
                                Component firstLine = lore.lines().getLast();
                                if (firstLine != null && firstLine.getString().contains("Right-click to convert to an item!")) {
                                    if (Minecraft.getInstance().player != null) {
                                        Minecraft.getInstance().player.closeContainer();
                                        Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(),1f,1f);
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