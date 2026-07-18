package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class StarredMobHighlight implements FeatureManager.Feature {
    private boolean enabled = false;
    private static final Pattern MOB_PATTERN = Pattern.compile("^.*✯ .*\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?(?:[kMB])?❤$");
    private final List<StarredBox> starredBoxes = new ArrayList<>();

    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 0, 0, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(highlightColor);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTcyMDA1MDI3MjQwNiwKICAicHJvZmlsZUlkIiA6ICJlMjc5NjliODYyNWY0NDg1YjkyNmM5NTBhMDljMWMwMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLRVZJTktFTE9LRSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hMGU4MWVkMDdkZmIwMjQ0ZDU2ZjRkNWYyYjM3NTUzZWMwMjZmYjQ3OTZmMGZiMGM1N2E4ZWIyNjQ5ODNlMWUwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            starredBoxes.clear();
        });
    }

    public void find() {
        if (!enabled) return;
        if (!DUNGEON.inDungeon) return;
        if (DUNGEON.bossEntry != -1 || DUNGEON.runStarted == -1) {
            starredBoxes.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        starredBoxes.removeIf(box -> box.getStand().isRemoved());

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
            if (MOB_PATTERN.matcher(raw).matches()) {
                boolean exists = starredBoxes.stream()
                        .anyMatch(b -> b.getStand() == stand);

                if (!exists) {
                    starredBoxes.add(new StarredBox(stand));
                }
            }

        }

        for (StarredBox box : starredBoxes) {
            box.update();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        starredBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class StarredBox {
        private final ArmorStandEntity stand;

        public StarredBox(ArmorStandEntity stand) {
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
            double heightOffset = (name.contains("Fel") || name.contains("Withermancer")) ? 3.0 : 2.0;

            int color = StarredMobHighlight.highlightColor.getRGB();
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