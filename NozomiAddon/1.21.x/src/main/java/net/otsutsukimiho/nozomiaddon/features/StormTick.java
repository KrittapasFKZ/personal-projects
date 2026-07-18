package net.otsutsukimiho.nozomiaddon.features;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StormTick extends DraggableHudElement implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
    public void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        if (!EditHudScreen.isEditMode()) {
            if (!DUNGEON.inDungeon) return;
            if (!phaseStarted) return;
        }

        float scale = this.size / 10.0f;
        var matrices = ctx.getMatrices();
        matrices.pushMatrix();
        ctx.getMatrices().translate(this.x, this.y);
        ctx.getMatrices().scale(scale, scale);

        Text displayText;
        if (EditHudScreen.isEditMode() && !phaseStarted) {
            displayText = Text.literal("§b30.15");
        } else {
            displayText = Text.literal(String.format("§b%.2f", (tickElapsed / 20.0)));
        }

        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(displayText);
        int centerX = 15 - textWidth / 2;

        ctx.drawText(MinecraftClient.getInstance().textRenderer, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.getMatrices().popMatrix();
        this.width = Math.round(30 * scale);
        this.height = Math.round(10 * scale);
    }

}