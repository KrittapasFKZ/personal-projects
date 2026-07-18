package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import net.minecraft.util.math.Vec3d;
import net.otsutsukimiho.nozomiaddon.utils.*;
import net.otsutsukimiho.nozomiaddon.utils.events.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.otsutsukimiho.nozomiaddon.NozomiAddonClient.DUNGEON;

public class BatHighlight implements FeatureManager.Feature {
    private boolean enabled = false;
    private long lastAnnounceTime = 0;
    private final List<BatBox> batBoxes = new ArrayList<>();

    public static BooleanSetting announceDead = new BooleanSetting("AnnounceDead", false);
    public static SoundSetting customSound = new SoundSetting("AlertSound", "minecraft:entity.experience_orb.pickup", 1.0f, 1.25f);
    public static TextSetting customMessage = new TextSetting("Custom Message", "&c&lBat Died!");
    public static ColorSetting highlightColor = new ColorSetting("Color", new Color(255, 0, 0, 255), false);
    @Override
    public List<Settings> getSettings() {
        return List.of(announceDead, customMessage, highlightColor, customSound);
    }

    @Override
    public ItemStack getIcon() {
        return HeadUtils.getSkull("eyJ0aW1lc3RhbXAiOjE1NzA3MzcyNjc0OTcsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGY4OWZjNTEzNzYzMWY2YjU0MzkzMWJmN2FmMWRkZTBhZWY4YzY5Y2JlNWI4YmFmZTIxNjZmM2IxYzdlMWE2OSIsIm1ldGFkYXRhIjp7Im1vZGVsIjoic2xpbSJ9fX19");
    }

    public void initClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, c) -> {
            batBoxes.clear();
        });
        EventBus.register(BatHighlight.class, PacketEvent.Receive.class, event -> {
            if (!enabled || !announceDead.isEnabled()) return;
            if (!DUNGEON.inDungeon) return;

            boolean isBatDeathSound = false;

            if (event.packet instanceof PlaySoundS2CPacket soundPacket) {
                if (soundPacket.getSound().value() == SoundEvents.ENTITY_BAT_DEATH) {
                    isBatDeathSound = true;
                }
            } else if (event.packet instanceof PlaySoundFromEntityS2CPacket entitySoundPacket) {
                if (entitySoundPacket.getSound().value() == SoundEvents.ENTITY_BAT_DEATH) {
                    isBatDeathSound = true;
                }
            }
            if (isBatDeathSound) {
                triggerAnnouncement();
            }
        });
    }

    public void find() {
        if (!enabled) return;
        if (!DUNGEON.inDungeon || DUNGEON.bossEntry != -1 || DUNGEON.runStarted == -1) {
            batBoxes.clear();
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        if (client.player == null) return;

        batBoxes.removeIf(box -> {
            BatEntity bat = box.getBat();
            if (bat.isInvisible()) return true;
            if (!bat.isAlive()) return true;
            if (bat.isRemoved()) return true;
            if (client.player.distanceTo(bat) > 32) return true;
            box.update();
            return false;
        });


        Box detectionBox = client.player.getBoundingBox().expand(32);
        List<BatEntity> nearbyBats = client.world.getEntitiesByClass(BatEntity.class, detectionBox, e -> true);
        for (BatEntity bat : nearbyBats) {
            if (bat.isInvisible()) continue;
            if (client.player.distanceTo(bat) > 32) continue;
            boolean isTracked = batBoxes.stream().anyMatch(b -> b.getBat() == bat);
            if (!isTracked && bat.isAlive()) {
                batBoxes.add(new BatBox(bat));
            }
        }
    }

    private void triggerAnnouncement() {
        long now = System.currentTimeMillis();
        if (now - lastAnnounceTime < 50) return;
        lastAnnounceTime = now;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        customSound.playTestSound();
        client.player.sendMessage(Text.literal("§d§lNA §f§l» §c§lBat §chas been killed!"), false);

        String rawMessage = BatHighlight.customMessage.getValue();
        if (rawMessage == null) rawMessage = " ";
        Text formattedText = ColorUtils.parseColor(rawMessage.trim());

        client.inGameHud.setTitle(Text.empty());
        client.inGameHud.setSubtitle(formattedText);
        client.inGameHud.setTitleTicks(0, 30, 0);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        batBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class BatBox {
        private final BatEntity bat;

        public BatBox(BatEntity bat) {
            this.bat = bat;
        }

        public void update() {
            if (!bat.isAlive()) return;
            if (bat.isInvisible()) return;

            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
            int color = BatHighlight.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            Vec3d lerpedPos = bat.getLerpedPos(tickDelta);
            Vec3d rawPos = bat.getEntityPos();
            Box box = bat.getBoundingBox().offset(lerpedPos.subtract(rawPos));
            Renderer.addBox(box, r, g, b);
        }

        public BatEntity getBat() {
            return bat;
        }
    }

}