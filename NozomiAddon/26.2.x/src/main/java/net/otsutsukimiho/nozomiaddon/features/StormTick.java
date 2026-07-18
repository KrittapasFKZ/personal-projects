package net.otsutsukimiho.nozomiaddon.features;

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

public class StormTick extends DraggableHudElement implements FeatureManager.Feature {
    public StormTick() {
        super("StormTick", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static NumberSetting hideAfter = new NumberSetting("hideAfter(s)", 35,10,40,1);
    @Override
    public List<Settings> getSettings() {
        return List.of(hideAfter);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTYwNTYyMzMzMzU2MSwKICAicHJvZmlsZUlkIiA6ICJjZGM5MzQ0NDAzODM0ZDdkYmRmOWUyMmVjZmM5MzBiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSYXdMb2JzdGVycyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85Y2E2YWM4Mzk2YmEyZmE2NGIwZjI3MTFkY2EyMDIzMmM3YTUyOTEyNmI5NmRiNmVmYWE4ZDdmMmUxODQwZDEiCiAgICB9CiAgfQp9");
    }

    private volatile boolean enabled = false;
    private static float tickElapsed = 0;
    private static boolean phaseStarted = false;

    public static void onPacketReceived() {
        if (!DUNGEON.inDungeon) return;
        if (phaseStarted) {
            tickElapsed += 1f;
            if ((tickElapsed / 20f) > StormTick.hideAfter.getValue()) phaseStarted = false;
        }
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            tickElapsed = 0;
            phaseStarted = false;
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled) return;
            if (!DUNGEON.inDungeon) return;
            String msg = message.getString();
            if (msg.matches("^\\[BOSS] Storm: Pathetic Maxor, just like expected.$") && !phaseStarted) {
                phaseStarted = true;
            }
            if (msg.contains("[BOSS] Storm: I should have known that I stood no chance.") && phaseStarted) {
                phaseStarted = false;
            }
        });
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (!phaseStarted) return;
        }

        float scale = this.size / 10.0f;
        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        Component displayText;
        if (EditHudScreen.isEditMode() && !phaseStarted) {
            displayText = Component.literal("§b30.15");
        } else {
            displayText = Component.literal(String.format("§b%.2f", (tickElapsed / 20.0)));
        }

        int textWidth = Minecraft.getInstance().font.width(displayText);
        int centerX = 15 - textWidth / 2;

        ctx.text(Minecraft.getInstance().font, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(30 * scale);
        this.height = Math.round(10 * scale);
    }

}