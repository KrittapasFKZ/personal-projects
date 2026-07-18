package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.otsutsukimiho.nozomiaddon.utils.*;
import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

import java.util.ArrayList;
import java.util.List;

public class DungeonMobDrops implements FeatureManager.Feature {
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        armorStand.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private boolean enabled = false;
    private final List<MobDropBox> armorStand = new ArrayList<>();

    public static BooleanSetting playSound = new BooleanSetting("playSound", true);
    public static BooleanSetting showTitle = new BooleanSetting("showTitle", true);
    public static SoundSetting customSound = new SoundSetting("AlertSound", "minecraft:entity.ender_dragon.growl", 1.0f, 2.0f);
    @Override
    public List<Settings> getSettings() {
        return List.of(showTitle, playSound, customSound);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("ewogICJ0aW1lc3RhbXAiIDogMTcyMDA0NTYxNjQzNiwKICAicHJvZmlsZUlkIiA6ICI1ODc5MjNlNDkxMzM0ZDMzYWE4ZjQ3ZWJkZTljOTc3MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGV2ZW5mb3VyMTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTdkMWY3YzFlOTBiZmEzMmE4ZTNhOWVhYjc4OTMzM2MzZmRjN2ZlN2FjMmM3ZmJhZTk5YTBkMTM4MTcxMTc3MSIKICAgIH0KICB9Cn0=");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            armorStand.clear();
        });
    }

    public void find() {
        if (!enabled) return;
        if (!DUNGEON.inDungeon) return;
        if (DUNGEON.bossEntry != -1) {
            armorStand.clear();
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (client.player == null) return;

        armorStand.removeIf(box -> box.getStand().isRemoved());

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
            if (raw.contains("Ice Spray Wand") || (raw.contains("Skeleton Master Chestplate") && DungeonRunOverview.dungeonFloor.contains("7"))) {
                boolean exists = armorStand.stream().anyMatch(b -> b.getStand() == stand);
                if (!exists) {
                    armorStand.add(new DungeonMobDrops.MobDropBox(stand));
                    if (client.player != null) {
                        if (DungeonMobDrops.playSound.isEnabled()) customSound.playTestSound();

                        client.gui.hud.getChat().addClientSystemMessage(Component.literal("§e§m§l--------------------------------§r"));
                        client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §d§lRARE DROP! ").append(name));
                        client.gui.hud.getChat().addClientSystemMessage(Component.literal("§d§m§l--------------------------------§r"));

                        if (DungeonMobDrops.showTitle.isEnabled()) {
                            client.gui.hud.setTitle(Component.literal("§e§l§kA§r §d§lRARE DROP §6§l§kA§r"));
                            client.gui.hud.setSubtitle(name);
                            client.gui.hud.setTimes(0,50,10);
                        }
                    }
                }
            }

        }

        for (DungeonMobDrops.MobDropBox box : armorStand) {
            box.update();
        }

    }

    private static class MobDropBox {
        private final ArmorStand stand;

        public MobDropBox(ArmorStand stand) {
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
            if (name.contains("Skeleton Master Chestplate")) {
                AABB box = new AABB(x - 0.5, y - 2.0, z - 0.5, x + 0.5, y, z + 0.5);
                Renderer.addBox(box.inflate(-0.2,0,-0.2), 1f, 0.5f, 0f);
            } else if (name.contains("Ice Spray Wand")) {
                AABB box = new AABB(x - 0.5, y - 2.0, z - 0.5, x + 0.5, y, z + 0.5);
                Renderer.addBox(box.inflate(-0.2,0,-0.2), 0f, 0.5f, 1f);
            }

        }

        public ArmorStand getStand() {
            return stand;
        }
    }

}