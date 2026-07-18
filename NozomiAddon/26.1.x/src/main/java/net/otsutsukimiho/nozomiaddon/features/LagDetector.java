package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.otsutsukimiho.nozomiaddon.gui.*;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LagDetector extends DraggableHudElement implements FeatureManager.Feature {
    public LagDetector() {
        super("LagDetector", 10, 10, 100, 10, 10);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static final Map<String, Supplier<String>> DUMMY_DATA = Map.of(
            "{loss}", () -> "670"
    );

    public static BooleanSetting onlyInDungeon = new BooleanSetting("OnlyInDungeon", false);
    public static NumberSetting threshold = new NumberSetting("Threshold (ms)", 750, 100, 5000, 50);
    public static TextSetting customMessage = new TextSetting("Custom Message", "&c{loss}ms", DUMMY_DATA);
    @Override
    public List<Settings> getSettings() {
        return List.of(onlyInDungeon, threshold, customMessage);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.CLOCK);
    }

    private volatile boolean enabled = false;
    private static long lastPacketTime = 0;

    public static void onPacketReceived() {
        if (onlyInDungeon.isEnabled() && !DUNGEON.inDungeon) return;
        lastPacketTime = System.currentTimeMillis();
    }

    @Override
    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            lastPacketTime = System.currentTimeMillis();
        });
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
        if (!enabled && !EditHudScreen.isEditMode()) return;

        long timeSinceLastPacket = System.currentTimeMillis() - lastPacketTime;
        boolean isLagging = timeSinceLastPacket > threshold.getValue();

        if (!EditHudScreen.isEditMode()) {
            if (onlyInDungeon.isEnabled() && !DUNGEON.inDungeon) return;
            if ((!isLagging || lastPacketTime == 0)) return;
        }

        float scale = this.size / 10.0f;

        var matrices = ctx.pose();
        matrices.pushMatrix();
        ctx.pose().translate(this.x, this.y);
        ctx.pose().scale(scale, scale);

        String rawMessage = customMessage.getValue();
        if (rawMessage == null) rawMessage = " ";
        String newMsg = rawMessage.replace("{loss}", isLagging ? String.valueOf(timeSinceLastPacket) : String.valueOf(670L)).trim();
        Component displayText = ColorUtils.parseColor(newMsg);

        int textWidth = Minecraft.getInstance().font.width(displayText);
        int centerX = 15 - textWidth / 2;

        ctx.text(Minecraft.getInstance().font, displayText, centerX, 0, 0xFFFFFFFF, true);

        ctx.pose().popMatrix();
        this.width = Math.round(30 * scale);
        this.height = Math.round(10 * scale);
    }

}