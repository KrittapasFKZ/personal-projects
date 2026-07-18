package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

import net.minecraft.util.math.Vec3d;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FrozenCorpse implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean enabled = false;
    private final List<CorpseBox> corpseBoxes = new ArrayList<>();
    private static int ticks = 0;

    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 255, 85, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(highlightColor);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTcxMDQzNTc1NzQ0NCwKICAicHJvZmlsZUlkIiA6ICJlNzhjY2YyNjMxZTY0MjJkOGY1YzE3ZTliZGQ3N2RjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJEQU5JRUwxOTB5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliYjg2ODdlNzNjODRlMzEwZDFiYzAyMzFiODQyZGFkMWY5MzAwMWI2ZDliYTMzMjllM2M4YTY4NWI1MzU2MjMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
    }

    @Override public String getDescription() {
        return "§aHighlight Frozen Corpses while in Glacite Mineshaft";
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            corpseBoxes.clear();
            ticks = 0;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled) return;
            if (ticks >= 20 && !WorldUtils.getWorld().contains("Mineshaft")) {
                WorldUtils.checkMineshaft();
                ticks = 0;
            } else ticks++;
        });
    }

    public void find() {
        if (!enabled) {
            corpseBoxes.clear();
            return;
        }
        if (!WorldUtils.getWorld().contains("Mineshaft")) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        corpseBoxes.removeIf(box -> box.getStand().isRemoved());

        Box worldBox = client.player.getBoundingBox().expand(80);

        List<ArmorStandEntity> stands =
                client.world.getEntitiesByClass(
                        ArmorStandEntity.class,
                        worldBox,
                        stand -> !stand.isInvisible()
                );

        for (ArmorStandEntity stand : stands) {
            boolean exists = corpseBoxes.stream().anyMatch(b -> b.getStand() == stand);
            if (!exists) {
                corpseBoxes.add(new CorpseBox(stand));
            }
        }

        for (CorpseBox box : corpseBoxes) {
            box.update();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        corpseBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class CorpseBox {
        private final ArmorStandEntity stand;

        public CorpseBox(ArmorStandEntity stand) {
            this.stand = stand;
        }

        public void update() {
            if (stand.isRemoved()) return;
            if (stand.isInvisible()) return;

            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
            int color = FrozenCorpse.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            Vec3d lerpedPos = stand.getLerpedPos(tickDelta);
            Vec3d rawPos = stand.getEntityPos();
            Box box = stand.getBoundingBox().offset(lerpedPos.subtract(rawPos));
            Renderer.addBox(box.expand(0.2, 0.2, 0.2), r, g, b);
        }

        public ArmorStandEntity getStand() {
            return stand;
        }
    }

}