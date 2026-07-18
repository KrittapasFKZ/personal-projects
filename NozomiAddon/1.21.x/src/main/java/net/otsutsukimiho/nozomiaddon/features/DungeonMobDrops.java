package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import net.minecraft.util.math.Vec3d;
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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        armorStand.removeIf(box -> box.getStand().isRemoved());

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
            if (raw.contains("Ice Spray Wand") || (raw.contains("Skeleton Master Chestplate") && DungeonRunOverview.dungeonFloor.contains("7"))) {
                boolean exists = armorStand.stream().anyMatch(b -> b.getStand() == stand);
                if (!exists) {
                    armorStand.add(new DungeonMobDrops.MobDropBox(stand));
                    if (client.player != null) {
                        if (DungeonMobDrops.playSound.isEnabled()) customSound.playTestSound();

                        client.player.sendMessage(Text.literal("§e§m§l--------------------------------§r"), false);
                        client.player.sendMessage(Text.literal("§d§lNA §f§l» §d§lRARE DROP! ").append(name), false);
                        client.player.sendMessage(Text.literal("§d§m§l--------------------------------§r"), false);

                        if (DungeonMobDrops.showTitle.isEnabled()) {
                            client.inGameHud.setTitle(Text.literal("§e§l§kA§r §d§lRARE DROP §6§l§kA§r"));
                            client.inGameHud.setSubtitle(name);
                            client.inGameHud.setTitleTicks(0,50,10);
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
        private final ArmorStandEntity stand;

        public MobDropBox(ArmorStandEntity stand) {
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
            if (name.contains("Skeleton Master Chestplate")) {
                Box box = new Box(x - 0.5, y - 2.0, z - 0.5, x + 0.5, y, z + 0.5);
                Renderer.addBox(box.expand(-0.2,0,-0.2), 1f, 0.5f, 0f);
            } else if (name.contains("Ice Spray Wand")) {
                Box box = new Box(x - 0.5, y - 2.0, z - 0.5, x + 0.5, y, z + 0.5);
                Renderer.addBox(box.expand(-0.2,0,-0.2), 0f, 0.5f, 1f);
            }

        }

        public ArmorStandEntity getStand() {
            return stand;
        }
    }

}