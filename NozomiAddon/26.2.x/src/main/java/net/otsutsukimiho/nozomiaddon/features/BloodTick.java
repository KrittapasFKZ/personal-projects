package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;

public class BloodTick extends DraggableHudElement implements FeatureManager.Feature {
    public BloodTick() {
        super("BloodTick", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static NumberSetting hideAfterSeconds = new NumberSetting("hideAfter(s) (0=disabled)", 0,0,100,1);
    public static BooleanSetting hideAfterDialog = new BooleanSetting("hideAfterBloodDialog", false);
    public static BooleanSetting hideAfterDone = new BooleanSetting("hideAfterBloodDone", false);
    @Override
    public List<Settings> getSettings() {
        return List.of(hideAfterSeconds, hideAfterDialog, hideAfterDone);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTcwODY4ODA2MjE4OCwKICAicHJvZmlsZUlkIiA6ICIzNzRhZGZlMjkyOWI0ZDBiODJmYmVjNTg2ZTI5ODk4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfR2xvenpfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIwZGU1ZTg5NzQ5NDAzNzU5MzRkMzJmNzFjOTFhZDJkNTcyOGQzOGU1MTY0N2RjYzhmMzkyMDZjMDk5YTU0YzIiCiAgICB9CiAgfQp9");
    }

    private volatile boolean enabled = false;
    private static boolean hideHud = false;

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            hideHud = false;
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.bloodStart) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            String msg = message.getString();
            if (msg.contains("Let's see how you can handle this") && !hideHud && BloodTick.hideAfterDialog.isEnabled()) {
                client.gui.hud.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))));
                hideHud = true;
            }
        });
        ClientTickEvents.END_LEVEL_TICK.register(world -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.bloodStart) return;
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            if (BloodTick.hideAfterSeconds.getValue() != 0 && !hideHud) {
                if ((DungeonRunOverview.bloodTick / 20.0) > BloodTick.hideAfterSeconds.getValue()) {
                    client.gui.hud.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))));
                    hideHud = true;
                }
            }
            if (BloodTick.hideAfterDone.isEnabled() && !hideHud) {
                if (DUNGEON.watcherCleared != -1) {
                    client.gui.hud.getChat().addClientSystemMessage(Component.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))));
                    hideHud = true;
                }
            }
        });
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.bloodStart) return;
            if (hideHud) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        Component displayText;
        if (EditHudScreen.isEditMode() && !DungeonRunOverview.bloodStart) {
            displayText = Component.literal("§c25.15");
        } else {
            displayText = Component.literal(String.format("§c%.2f", (DungeonRunOverview.bloodTick / 20.0)));
        }

        int textWidth = Minecraft.getInstance().font.width(displayText);
        int centerX = 15 - textWidth / 2;

        ctx.text(Minecraft.getInstance().font, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(30 * scale);
        this.height = Math.round(10 * scale);
    }

}