package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;

import java.util.List;

public class HideLoadouts implements FeatureManager.Feature {
    private volatile boolean enabled = false;
    private boolean isWardrobe = false;
    private static long blockReopenUntil = 0;

    public static BooleanSetting enableKeyBind = new BooleanSetting("EnableKeyBind", false);
    public static BooleanSetting blockReopen = new BooleanSetting("No Re-open", true);
    public static BooleanSetting checkCanEquip = new BooleanSetting("checkCanEquip", true);
    public static KeySetting loadout1 = new KeySetting("Loadout #1", 49);
    public static KeySetting loadout2 = new KeySetting("Loadout #2", 50);
    public static KeySetting loadout3 = new KeySetting("Loadout #3", 51);
    public static KeySetting loadout4 = new KeySetting("Loadout #4", 52);
    public static KeySetting loadout5 = new KeySetting("Loadout #5", 53);
    public static KeySetting loadout6 = new KeySetting("Loadout #6", 54);
    public static KeySetting loadout7 = new KeySetting("Loadout #7", 55);
    public static KeySetting loadout8 = new KeySetting("Loadout #8", 56);
    public static KeySetting loadout9 = new KeySetting("Loadout #9", 57);

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public List<Settings> getSettings() {
        return List.of(enableKeyBind, blockReopen, checkCanEquip, loadout1, loadout2, loadout3, loadout4, loadout5, loadout6, loadout7, loadout8, loadout9);
    }

    @Override
    public ItemStack getIcon() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x7F3F92));
        return stack;
    }

    @Override
    public void initClient() {
        EventBus.register(HideWardrobe.class, PacketEvent.Receive.class, event -> {
            if (!enabled || !blockReopen.isEnabled()) return;
            if (System.currentTimeMillis() < blockReopenUntil) {
                if (event.packet instanceof ClientboundOpenScreenPacket packet) {
                    if (packet.getTitle().getString().contains("Loadouts")) {
                        event.cancel();
                    }
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((Minecraft client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;

            ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseX, mouseY) -> {
                if (screen instanceof AbstractContainerScreen<?> handled) {
                    var handler = handled.getMenu();
                    Component title = handled.getTitle();
                    String titleStr = title.getString();
                    if (titleStr.contains("Loadouts")) {
                        isWardrobe = true;
                        var stack = handler.getCarried();
                        if (isWardrobe) {
                            var lore = stack.getComponents().get(DataComponents.LORE);
                            if (lore != null) {
                                for (Component line : lore.lines()) {
                                    String lineStr = line.getString().toLowerCase();
                                    if (lineStr.contains("left-click to equip")) {
                                        if (Minecraft.getInstance().player != null) {
                                            Minecraft.getInstance().player.closeContainer();
                                            Minecraft.getInstance().player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1f, 1f);
                                            isWardrobe = false;
                                            blockReopenUntil = System.currentTimeMillis() + 1000;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            });

            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, key) -> {
                if (!(scr instanceof AbstractContainerScreen<?> handled)) return true;

                String titleStr = handled.getTitle().getString();
                if (!titleStr.contains("Loadouts")) return true;

                if (!HideLoadouts.enableKeyBind.isEnabled()) return true;

                int code = key.input();
                int targetSlot = -1;

                if (code == loadout1.getCode()) targetSlot = 14;
                else if (code == loadout2.getCode()) targetSlot = 15;
                else if (code == loadout3.getCode()) targetSlot = 16;
                else if (code == loadout4.getCode()) targetSlot = 23;
                else if (code == loadout5.getCode()) targetSlot = 24;
                else if (code == loadout6.getCode()) targetSlot = 25;
                else if (code == loadout7.getCode()) targetSlot = 32;
                else if (code == loadout8.getCode()) targetSlot = 33;
                else if (code == loadout9.getCode()) targetSlot = 34;

                if (targetSlot == -1) return true;

                var player = client.player;
                var handler = handled.getMenu();

                if (player != null && client.gameMode != null) {
                    var slot = handler.getSlot(targetSlot);
                    if (!slot.hasItem()) return false;

                    var stack = slot.getItem();
                    var lore = stack.getComponents().get(DataComponents.LORE);

                    boolean isReady = false;

                    if (lore != null) {
                        for (Component line : lore.lines()) {
                            String lineStr = line.getString().toLowerCase();
                            if (lineStr.contains("left-click to equip")) {
                                isReady = true;
                                break;
                            }
                        }
                    }

                    if (checkCanEquip.isEnabled()) {
                        if (isReady) {
                            client.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, player);
                            if (client.player != null) {
                                client.player.closeContainer();
                                client.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1f, 1f);

                                blockReopenUntil = System.currentTimeMillis() + 1000;
                            }

                            isWardrobe = false;
                            return false;
                        }
                    } else {
                        client.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, player);
                        if (client.player != null) {
                            client.player.closeContainer();
                            client.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1f, 1f);

                            blockReopenUntil = System.currentTimeMillis() + 1000;
                        }

                        isWardrobe = false;
                        return false;
                    }

                }
                return true;
            });
        });
    }
}