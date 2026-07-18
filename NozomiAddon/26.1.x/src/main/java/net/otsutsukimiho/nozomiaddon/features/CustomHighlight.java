package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.otsutsukimiho.nozomiaddon.utils.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CustomHighlight implements FeatureManager.Feature {
    private boolean enabled = false;
    private final List<StarredBox> starredBoxes = new ArrayList<>();
    private static final List<String> ALL_MOB_NAMES = getEntityNames();

    private static List<String> getEntityNames() {
        List<String> names = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            String name = type.getDescription().getString();
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public static StringSetting regexString = new StringSetting("Name/Regex", "Glacite (Bowman|Caver|Mage|Mutt)");
    public static MultiSelectSetting mobsType = new MultiSelectSetting("Mob Type", ALL_MOB_NAMES);
    public static BooleanSetting onlyInArea = new BooleanSetting("Only In Area", false);
    public static BooleanSetting drawLine = new BooleanSetting("DrawLine", false);
    public static StringSetting areaString = new StringSetting("Area", "Catacombs");
    public static NumberSetting searchDistance = new NumberSetting("SearchDistance", 32, 1, 128, 1);
    public static FloatSetting highlightHeight = new FloatSetting("Height", 2f, 0.5f, 5f, 0.1f);
    public static FloatSetting highlightWidth = new FloatSetting("Width", 1f, 0.1f, 5f, 0.1f);
    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 0, 0, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(regexString, mobsType, onlyInArea, drawLine, areaString, searchDistance, highlightHeight, highlightWidth, highlightColor);
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.POLAR_BEAR_SPAWN_EGG);
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            starredBoxes.clear();
        });
    }

    public void find() {
        if (!enabled) {
            starredBoxes.clear();
            return;
        }
        if (CustomHighlight.onlyInArea.isEnabled()) {
            if (!WorldUtils.getWorld().contains(CustomHighlight.areaString.getValue())) {
                starredBoxes.clear();
                return;
            }
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (client.player == null) return;

        starredBoxes.removeIf(box -> box.getEntity().isRemoved());

        Pattern targetPattern = null;
        try {
            if (!CustomHighlight.regexString.getValue().isEmpty()) {
                targetPattern = Pattern.compile(CustomHighlight.regexString.getValue());
            }
        } catch (PatternSyntaxException e) {
            //
        }

        AABB worldBox = client.player.getBoundingBox().inflate(CustomHighlight.searchDistance.getValue());

        List<net.minecraft.world.entity.LivingEntity> entities = client.level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                worldBox,
                entity -> true
        );

        for (net.minecraft.world.entity.LivingEntity entity : entities) {
            if (entity == client.player) {
                continue;
            }

            boolean shouldHighlight = false;
            List<String> selectedMobs = mobsType.getSelectedOptions();
            String entityTypeName = entity.getType().getDescription().getString();

            boolean typeMatches = selectedMobs.isEmpty() || selectedMobs.contains(entityTypeName);

            if (typeMatches) {
                if (targetPattern != null) {
                    Component nameText = entity.hasCustomName() ? entity.getCustomName() : entity.getName();
                    if (nameText != null) {
                        String rawName = nameText.getString().replaceAll("(?i)[§&][0-9A-FK-OR]", "");
                        if (targetPattern.matcher(rawName).find()) {
                            shouldHighlight = true;
                        }
                    }
                } else {
                    if (!selectedMobs.isEmpty()) {
                        shouldHighlight = true;
                    }
                }
            }

            if (shouldHighlight) {
                boolean exists = starredBoxes.stream().anyMatch(b -> b.getEntity() == entity);
                if (!exists) {
                    starredBoxes.add(new StarredBox(entity));
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
        private final net.minecraft.world.entity.LivingEntity entity;

        public StarredBox(net.minecraft.world.entity.LivingEntity entity) {
            this.entity = entity;
        }

        public void update() {
            if (entity.isRemoved()) return;

            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            Vec3 pos = entity.getPosition(tickDelta);

            double x = pos.x();
            double y = pos.y();
            double z = pos.z();

            int color = CustomHighlight.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;

            double halfW = CustomHighlight.highlightWidth.getValue() / 2.0;
            double h = CustomHighlight.highlightHeight.getValue();

            AABB box;

            if (entity instanceof ArmorStand) {
                box = new AABB(x - halfW, y - h, z - halfW, x + halfW, y, z + halfW);
            } else {
                box = new AABB(x - halfW, y, z - halfW, x + halfW, y + h, z + halfW);
            }

            Renderer.addBox(box, r, g, b);
            if (drawLine.isEnabled()) Renderer.addTracer(box.getCenter(), r, g, b, 1f);
        }

        public net.minecraft.world.entity.LivingEntity getEntity() {
            return entity;
        }
    }

}