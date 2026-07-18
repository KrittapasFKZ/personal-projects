package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WitherHighlight implements FeatureManager.Feature {
    private boolean enabled = false;
    private final List<WitherBox> witherBoxes = new ArrayList<>();
    private List<WitherEntity> withers = new ArrayList<>();
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
            MinecraftClient client = MinecraftClient.getInstance();
            if (!enabled || client.world == null) return;
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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        witherBoxes.removeIf(box -> !box.getWither().isAlive());

        Box worldBox = client.player.getBoundingBox().expand(80);

        withers = client.world.getEntitiesByClass(WitherEntity.class, worldBox, e -> e.getInvulnerableTimer() != 800);
        for (WitherEntity wither : withers) {
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
        private final WitherEntity wither;

        public WitherBox(WitherEntity wither) {
            this.wither = wither;
        }

        public void update() {
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
            for (WitherEntity wither : withers) {
                int color = WitherHighlight.highlightColor.getRGB();
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                Vec3d lerpedPos = wither.getLerpedPos(tickDelta);
                Vec3d rawPos = wither.getEntityPos();
                Box box = wither.getBoundingBox().offset(lerpedPos.subtract(rawPos));
                Renderer.addBox(box.expand(-0.05, -0.1, -0.05), r, g, b);
            }
        }

        public WitherEntity getWither() {
            return wither;
        }
    }
}