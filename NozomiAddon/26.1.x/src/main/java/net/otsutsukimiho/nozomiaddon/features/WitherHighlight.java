package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WitherHighlight implements FeatureManager.Feature {
    private boolean enabled = false;
    private final List<WitherBox> witherBoxes = new ArrayList<>();
    private List<WitherBoss> withers = new ArrayList<>();
    private boolean inP5 = false;

    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 0, 0, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(highlightColor);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WITHER_SKELETON_SKULL);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            witherBoxes.clear();
            inP5 = false;
        });

        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            Minecraft client = Minecraft.getInstance();
            if (!enabled || client.level == null) return;
            if (!DUNGEON.inDungeon) return;

            String raw = message.getString();

            if (!inP5 && raw.contains("[BOSS] Necron: All this, for nothing...")) {
                inP5 = true;
            }
        });
    }

    public void find() {
        if (!enabled) return;
        if (!DUNGEON.inDungeon) return;
        if (inP5) {
            witherBoxes.clear();
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (client.player == null) return;

        witherBoxes.removeIf(box -> !box.getWither().isAlive());

        AABB worldBox = client.player.getBoundingBox().inflate(80);

        withers = client.level.getEntitiesOfClass(WitherBoss.class, worldBox, e -> e.getInvulnerableTicks() != 800);
        for (WitherBoss wither : withers) {
            boolean exists = witherBoxes.stream().anyMatch(b -> b.getWither() == wither);
            if (!exists) {
                witherBoxes.add(new WitherBox(wither));
            }
        }

        for (WitherBox box : witherBoxes) {
            box.update();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        witherBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private  class WitherBox {
        private final WitherBoss wither;

        public WitherBox(WitherBoss wither) {
            this.wither = wither;
        }

        public void update() {
            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            for (WitherBoss wither : withers) {
                int color = WitherHighlight.highlightColor.getRGB();
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                Vec3 lerpedPos = wither.getPosition(tickDelta);
                Vec3 rawPos = wither.position();
                AABB box = wither.getBoundingBox().move(lerpedPos.subtract(rawPos));
                Renderer.addBox(box.inflate(-0.05, -0.1, -0.05), r, g, b);
            }
        }

        public WitherBoss getWither() {
            return wither;
        }
    }
}