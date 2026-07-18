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

public class HideWardrobe implements FeatureManager.Feature {
    private volatile boolean enabled = false;
    private boolean isWardrobe = false;

    private long lastUnequipTime = 0;
    private int lastUnequipSlot = -1;
    private static long blockReopenUntil = 0;

    public static BooleanSetting enableKeyBind = new BooleanSetting("EnableKeyBind", false);
    public static BooleanSetting blockReopen = new BooleanSetting("No Re-open", true);
    public static BooleanSetting safeUnequip = new BooleanSetting("Safe Unequip (Double Tap)", true);
    public static NumberSetting blockDuration = new NumberSetting("Block Duration (ms)", 2000, 200, 3000, 100);
    public static KeySetting wardrobe1 = new KeySetting("Wardrobe #1", 49);
    public static KeySetting wardrobe2 = new KeySetting("Wardrobe #2", 50);
    public static KeySetting wardrobe3 = new KeySetting("Wardrobe #3", 51);
    public static KeySetting wardrobe4 = new KeySetting("Wardrobe #4", 52);
    public static KeySetting wardrobe5 = new KeySetting("Wardrobe #5", 53);
    public static KeySetting wardrobe6 = new KeySetting("Wardrobe #6", 54);
    public static KeySetting wardrobe7 = new KeySetting("Wardrobe #7", 55);
    public static KeySetting wardrobe8 = new KeySetting("Wardrobe #8", 56);
    public static KeySetting wardrobe9 = new KeySetting("Wardrobe #9", 57);

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
        return List.of(enableKeyBind, blockReopen, safeUnequip, blockDuration, wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9);
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
                    if (packet.getTitle().getString().contains("Armor Sets")) {
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
                    if (titleStr.contains("Armor Sets")) {
                        isWardrobe = true;
                        var stack = handler.getCarried();
                        if (isWardrobe) {
                            var lore = stack.getComponents().get(DataComponents.LORE);
                            if (lore != null) {
                                for (Component line : lore.lines()) {
                                    String lineStr = line.getString().toLowerCase();
                                    if (lineStr.contains("click to equip") || lineStr.contains("to unequip")) {
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
                if (!titleStr.contains("Armor Sets")) return true;

                if (!HideWardrobe.enableKeyBind.isEnabled()) return true;

                int code = key.input();
                int targetSlot = -1;

                if (code == wardrobe1.getCode()) targetSlot = 36;
                else if (code == wardrobe2.getCode()) targetSlot = 37;
                else if (code == wardrobe3.getCode()) targetSlot = 38;
                else if (code == wardrobe4.getCode()) targetSlot = 39;
                else if (code == wardrobe5.getCode()) targetSlot = 40;
                else if (code == wardrobe6.getCode()) targetSlot = 41;
                else if (code == wardrobe7.getCode()) targetSlot = 42;
                else if (code == wardrobe8.getCode()) targetSlot = 43;
                else if (code == wardrobe9.getCode()) targetSlot = 44;

                if (targetSlot == -1) return true;

                var player = client.player;
                var handler = handled.getMenu();

                if (player != null && client.gameMode != null) {
                    var slot = handler.getSlot(targetSlot);
                    if (!slot.hasItem()) return false;

                    var stack = slot.getItem();
                    var lore = stack.getComponents().get(DataComponents.LORE);

                    boolean isEquip = false;
                    boolean isUnequip = false;

                    if (lore != null) {
                        for (Component line : lore.lines()) {
                            String lineStr = line.getString().toLowerCase();
                            if (lineStr.contains("click to equip")) {
                                isEquip = true;
                                break;
                            } else if (lineStr.contains("click to unequip")) {
                                isUnequip = true;
                                break;
                            }
                        }
                    }

                    if (!isEquip && !isUnequip) return true;

                    if (isUnequip && safeUnequip.isEnabled()) {
                        long currentTime = System.currentTimeMillis();
                        if (targetSlot == lastUnequipSlot && (currentTime - lastUnequipTime) < blockDuration.getValue()) {
                            lastUnequipTime = 0;
                            lastUnequipSlot = -1;
                        } else {
                            lastUnequipTime = currentTime;
                            lastUnequipSlot = targetSlot;

                            client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §cPress again to unequip!"));
                            if (client.player != null) client.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1f, 1f);

                            return false;
                        }
                    }

                    client.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, player);
                    if (client.player != null) {
                        client.player.closeContainer();
                        client.player.playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 1f, 1f);
                        blockReopenUntil = System.currentTimeMillis() + 1000;
                    }

                    isWardrobe = false;
                    return false;
                }
                return true;
            });
        });
    }
}