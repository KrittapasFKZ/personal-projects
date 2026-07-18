package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.awt.*;
import java.util.List;

public class TerminalsSolver implements FeatureManager.Feature {
    private boolean enabled = false;
    public static TerminalScreen currentOverlay = null;

    private static long lastArmorStandClick = 0;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) currentOverlay = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static FloatSetting uiSize = new FloatSetting("UI Size", 1f, 0.5f, 2f, 0.05f);
    public static ModeSetting clickTerm = new ModeSetting("ClickTerm", "Click", "Drag", "Click");
    public static ModeSetting panelStyle = new ModeSetting("Panel Style", "Square", "Circle", "Square", "Smooth Square");
    public static BooleanSetting melodyTerminal = new BooleanSetting("Melody Terminal", false);
    public static BooleanSetting autoMelody = new BooleanSetting("Auto Melody", false);
    public static BooleanSetting trySkipMelody = new BooleanSetting("Try Skip Melody", false);
    public static BooleanSetting melodyBlockWrongClick = new BooleanSetting("Melody Block Wrong Click", true);
    public static NumberSetting openCooldown = new NumberSetting("Open Cooldown (ms)", 1000, 0, 3000, 50);
    public static NumberSetting firstClickProtect = new NumberSetting("FirstClickProtection (ms)", 500, 100, 2000, 50);
    public static NumberSetting resyncDelay = new NumberSetting("Re-sync Delay (ms)", 600, 500, 800, 50);
    public static NumberSetting dragDelay = new NumberSetting("Drag Delay (ms)", 100, 50, 500, 10);
    public static NumberSetting melodyClickDelay = new NumberSetting("Melody Click Delay (ms)", 300, 5, 500, 5);
    public static NumberSetting clickDelay = new NumberSetting("MaxTickDelay", 10, 1, 20, 1);
    public static SoundSetting clickSound = new SoundSetting("ClickSound", "minecraft:ui.button.click", 1.0f, 1.0f);
    public static BooleanSetting hideTitle = new BooleanSetting("Hide Terminal Title", false);
    public static BooleanSetting showOrderNumber = new BooleanSetting("Show Order Number", false);
    public static BooleanSetting debug = new BooleanSetting("Debug Mode", false);
    public static BooleanSetting openOnce = new BooleanSetting("Open Once", false);
    public static FloatSetting bgOpacity = new FloatSetting("BG Opacity", 1f, 0f, 1f, 0.05f);
    public static ColorSetting bgColor = new ColorSetting("BG Color", new Color(25, 25, 25, 255), false);
    public static ColorSetting color1 = new ColorSetting("Panes", new Color(0, 255, 0, 255), false);
    public static ColorSetting colorNegative = new ColorSetting("Negative", new Color(255, 0, 0, 255), false);
    public static ColorSetting melodyNote = new ColorSetting("Melody Note", new Color(0, 255, 255, 255), false);
    public static ColorSetting color2 = new ColorSetting("Order2", new Color(0, 130, 130, 255), false);
    public static ColorSetting color3 = new ColorSetting("Order3", new Color(0, 40, 40, 255), false);

    @Override
    public List<Settings> getSettings() {
        return List.of(debug, uiSize, panelStyle, openOnce, melodyTerminal, autoMelody, trySkipMelody, melodyBlockWrongClick, openCooldown, firstClickProtect, resyncDelay, clickTerm, dragDelay, melodyClickDelay, clickDelay, hideTitle, showOrderNumber, clickSound, bgOpacity, bgColor, color1, colorNegative, melodyNote, color2, color3);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.COMMAND_BLOCK);
    }

    public void initClient() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!enabled || !DUNGEON.inDungeon || !TerminalsSolver.openOnce.isEnabled()) return InteractionResult.PASS;
            if (entity instanceof ArmorStand) {
                long now = System.currentTimeMillis();
                if (now - lastArmorStandClick < TerminalsSolver.openCooldown.getValue()) {
                    return InteractionResult.FAIL;
                }
                lastArmorStandClick = now;
            }
            return InteractionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!enabled || !DUNGEON.inDungeon || !TerminalsSolver.openOnce.isEnabled()) return InteractionResult.PASS;
            if (entity instanceof ArmorStand) {
                long now = System.currentTimeMillis();
                if (now - lastArmorStandClick < TerminalsSolver.openCooldown.getValue()) {
                    return InteractionResult.FAIL;
                }
                lastArmorStandClick = now;
            }
            return InteractionResult.PASS;
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!enabled) return;
            if (screen instanceof AbstractContainerScreen<?> handled) {
                String title = handled.getTitle().getString();
                TerminalType type = getTerminalType(title);

                if (type != TerminalType.None) {
                    if (currentOverlay != null) {
                        currentOverlay.updateHandler(handled);
                    } else {
                        currentOverlay = new TerminalScreen(type, handled);
                        TerminalScreen.setAllowClick(true);
                    }

                    ScreenMouseEvents.allowMouseClick(screen).register((s, click) -> {
                        if (currentOverlay != null) {
                            boolean handledByMod = currentOverlay.mouseClicked(click, false);
                            return !handledByMod;
                        }
                        return true;
                    });

                    ScreenMouseEvents.allowMouseDrag(screen).register((s, click, dragX, dragY) -> {
                        if (currentOverlay != null && TerminalsSolver.clickTerm.is("Drag")) {
                            boolean handledByMod = currentOverlay.mouseDragged(click, dragX, dragY);
                            return !handledByMod;
                        }
                        return true;
                    });
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.gui.screen() == null) {
                currentOverlay = null;
            }
            else if (currentOverlay != null && client.gui.screen() instanceof AbstractContainerScreen) {
                currentOverlay.tick();
            }
        });
    }

    public static TerminalType getTerminalType(String title) {
        if (title.startsWith("Click in order!")) return TerminalType.ClickInOrder;
        if (title.startsWith("Select all the")) return TerminalType.SelectAll;
        if (title.startsWith("Correct all the panes!")) return TerminalType.CorrectPanes;
        if (title.startsWith("What starts with:")) return TerminalType.StartsWith;
        if (title.startsWith("Change all to same color!")) return TerminalType.Rubix;
        if (title.startsWith("Click the button on time!") && melodyTerminal.isEnabled()) return TerminalType.Melody;
        return TerminalType.None;
    }

    public enum TerminalType {
        ClickInOrder, SelectAll, StartsWith, CorrectPanes, Rubix, Melody, None
    }
}