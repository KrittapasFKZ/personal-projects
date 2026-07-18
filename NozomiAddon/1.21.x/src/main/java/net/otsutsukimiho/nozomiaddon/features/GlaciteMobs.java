package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import net.minecraft.util.math.Vec3d;
import net.otsutsukimiho.nozomiaddon.utils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GlaciteMobs implements FeatureManager.Feature {
    public static final String MOD_ID = "nozomiaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean enabled = false;
    private final List<GlaciteBox> glaciteBoxes = new ArrayList<>();
    private static final Pattern GLACITE_PATTERN = Pattern.compile("Glacite (Bowman|Caver|Mage|Mutt)|Littlefoot");

    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(85, 255, 255, 255), false);
    public static ColorSetting highlightColor2 = new ColorSetting("Littlefoot Color", new Color(255, 85, 85, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(highlightColor, highlightColor2);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTcxMjA0NzY4NjYxNywKICAicHJvZmlsZUlkIiA6ICI5OWY1MzhjMDhlN2E0NTg3YmU4MGJjNGVmNzU0ZmQyMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTb2xvV1MyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QyYTAwNTdlZGQ4ZTg0ODBmMGVhMmE3OGMxYWQ4OGU0MDFjNDg0NGY0NDU4YWViNGUzM2FjOTNhNThiOGMxMWEiCiAgICB9CiAgfQp9");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            glaciteBoxes.clear();
        });
    }

    public void find() {
        if (!enabled) return;
        if (!WorldUtils.getWorld().contains("Mineshaft")) {
            glaciteBoxes.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        glaciteBoxes.removeIf(box -> box.getStand().isRemoved());

        Box worldBox = client.player.getBoundingBox().expand(80);

        List<ArmorStandEntity> stands =
                client.world.getEntitiesByClass(
                        ArmorStandEntity.class,
                        worldBox,
                        stand -> stand.hasCustomName()
                );

        for (ArmorStandEntity stand : stands) {
            Text name = stand.getCustomName();
            if (name == null) continue;

            String raw = name.getString();
            if (GLACITE_PATTERN.matcher(raw).find()) {
                boolean exists = glaciteBoxes.stream().anyMatch(b -> b.getStand() == stand);
                if (!exists) {
                    glaciteBoxes.add(new GlaciteBox(stand));
                }
            }

        }

        for (GlaciteBox box : glaciteBoxes) {
            box.update();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        glaciteBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class GlaciteBox {
        private final ArmorStandEntity stand;

        public GlaciteBox(ArmorStandEntity stand) {
            this.stand = stand;
        }

        public void update() {
            if (stand.isRemoved()) return;

            Text customName = stand.getCustomName();
            if (customName == null) return;

            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
            Vec3d pos = stand.getLerpedPos(tickDelta);

            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            String name = customName.getString();
            double heightOffset = name.contains("Mutt") ? 1.0 : 2.0;

            int color = name.contains("Littlefoot") ? GlaciteMobs.highlightColor2.getRGB() : GlaciteMobs.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            Box box = new Box(x - 0.5, y - heightOffset, z - 0.5, x + 0.5, y, z + 0.5);
            Renderer.addBox(box.expand(-0.2,0,-0.2), r, g, b);

        }

        public ArmorStandEntity getStand() {
            return stand;
        }
    }

}