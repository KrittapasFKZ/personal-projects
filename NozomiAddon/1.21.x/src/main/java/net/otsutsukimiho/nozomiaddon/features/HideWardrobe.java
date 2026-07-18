package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import net.otsutsukimiho.nozomiaddon.utils.*;

import java.util.List;

public class HideWardrobe implements FeatureManager.Feature {
    private volatile boolean enabled = false;
    private boolean isWardrobe = false;

    private long lastUnequipTime = 0;
    private int lastUnequipSlot = -1;

    public static BooleanSetting enableKeyBind = new BooleanSetting("EnableKeyBind", false);
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
        return List.of(enableKeyBind, safeUnequip, blockDuration, wardrobe1, wardrobe2, wardrobe3, wardrobe4, wardrobe5, wardrobe6, wardrobe7, wardrobe8, wardrobe9);
    }

    @Override
    public ItemStack getIcon() {
        ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x7F3F92));
        return stack;
    }

    @Override
    public void initClient() {
        ScreenEvents.AFTER_INIT.register((MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) -> {
            if (!enabled) return;

            ScreenMouseEvents.afterMouseClick(screen).register((screen1, mouseX, mouseY) -> {
                if (screen instanceof HandledScreen<?> handled) {
                    var handler = handled.getScreenHandler();
                    Text title = handled.getTitle();
                    String titleStr = title.getString();
                    if (titleStr.contains("Wardrobe")) {
                        isWardrobe = true;
                        var stack = handler.getCursorStack();
                        if (stack != null && isWardrobe) {
                            var lore = stack.getComponents().get(DataComponentTypes.LORE);
                            if (lore != null) {
                                for (Text line : lore.lines()) {
                                    String lineStr = line.getString().toLowerCase();
                                    if (lineStr.contains("click to equip") || lineStr.contains("to unequip")) {
                                        if (MinecraftClient.getInstance().player != null) {
                                            MinecraftClient.getInstance().player.closeHandledScreen();
                                            MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 1f, 1f);
                                            isWardrobe = false;
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
                if (!(scr instanceof HandledScreen<?> handled)) return true;

                String titleStr = handled.getTitle().getString();
                if (!titleStr.contains("Wardrobe")) return true;

                if (!HideWardrobe.enableKeyBind.isEnabled()) return true;

                int code = key.getKeycode();
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
                var handler = handled.getScreenHandler();

                if (player != null && client.interactionManager != null) {
                    var slot = handler.getSlot(targetSlot);
                    if (slot == null || !slot.hasStack()) return false;

                    var stack = slot.getStack();
                    var lore = stack.getComponents().get(DataComponentTypes.LORE);

                    boolean isEquip = false;
                    boolean isUnequip = false;

                    if (lore != null) {
                        for (Text line : lore.lines()) {
                            String lineStr = line.getString().toLowerCase();
                            if (lineStr.contains("click to equip")) {
                                isEquip = true;
                                break;
                            } else if (lineStr.contains("to unequip")) {
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

                            if (client.player != null) {
                                client.player.sendMessage(Text.literal("§d§lNA §f§l» §cPress again to unequip!"), false);
                                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1f, 1f);
                            }

                            return false;
                        }
                    }

                    client.interactionManager.clickSlot(handler.syncId, targetSlot, 0, SlotActionType.PICKUP, player);
                    if (client.player != null) {
                        client.player.closeHandledScreen();
                        client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), 1f, 1f);
                    }

                    isWardrobe = false;
                    return false;
                }
                return true;
            });
        });
    }
}