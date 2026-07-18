package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (client.player == null) return;

        starredBoxes.removeIf(box -> box.getStand().isRemoved());

        AABB worldBox = client.player.getBoundingBox().inflate(80);

        List<ArmorStand> stands =
                client.level.getEntitiesOfClass(
                        ArmorStand.class,
                        worldBox,
                        stand -> stand.hasCustomName()
                );

        for (ArmorStand stand : stands) {
            Component name = stand.getCustomName();
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
        private final ArmorStand stand;

        public StarredBox(ArmorStand stand) {
            this.stand = stand;
        }

        public void update() {
            if (stand.isRemoved()) return;

            Component customName = stand.getCustomName();
            if (customName == null) return;

            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            Vec3 pos = stand.getPosition(tickDelta);

            double x = pos.x();
            double y = pos.y();
            double z = pos.z();

            String name = customName.getString();
            double heightOffset = (name.contains("Fel") || name.contains("Withermancer")) ? 3.0 : 2.0;

            int color = StarredMobHighlight.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            AABB box = new AABB(x - 0.5, y - heightOffset, z - 0.5, x + 0.5, y, z + 0.5);
            Renderer.addBox(box.inflate(-0.2,0,-0.2), r, g, b);

        }

        public ArmorStand getStand() {
            return stand;
        }
    }

}