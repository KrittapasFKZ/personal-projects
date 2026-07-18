package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) return;
            if (client.player == null) return;
            String msg = message.getString();
            if (msg.contains("Let's see how you can handle this") && !hideHud && BloodTick.hideAfterDialog.isEnabled()) {
                client.player.sendMessage(Text.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))),false);
                hideHud = true;
            }
        });
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.bloodStart) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) return;
            if (client.player == null) return;
            if (BloodTick.hideAfterSeconds.getValue() != 0 && !hideHud) {
                if ((DungeonRunOverview.bloodTick / 20.0) > BloodTick.hideAfterSeconds.getValue()) {
                    client.player.sendMessage(Text.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))),false);
                    hideHud = true;
                }
            }
            if (BloodTick.hideAfterDone.isEnabled() && !hideHud) {
                if (DUNGEON.watcherCleared != -1) {
                    client.player.sendMessage(Text.literal(String.format("§d§lNA §f§l» §cWatcher Yap for §a%.2fs", (DungeonRunOverview.bloodTick / 20.0))),false);
                    hideHud = true;
                }
            }
        });
    }

    @Override
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (!DungeonRunOverview.bloodStart) return;
            if (hideHud) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        Text displayText;
        if (EditHudScreen.isEditMode() && !DungeonRunOverview.bloodStart) {
            displayText = Text.literal("§c25.15");
        } else {
            displayText = Text.literal(String.format("§c%.2f", (DungeonRunOverview.bloodTick / 20.0)));
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 15 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.getMatrices().popMatrix();
        this.width = Math.round(30 * scale);
        this.height = Math.round(10 * scale);
    }

}