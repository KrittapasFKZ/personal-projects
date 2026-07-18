package net.otsutsukimiho.nozomiaddon.features;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

            if (event.packet instanceof ClientboundSoundPacket soundPacket) {
                if (soundPacket.getSound().value() == SoundEvents.BAT_DEATH) {
                    isBatDeathSound = true;
                }
            } else if (event.packet instanceof ClientboundSoundEntityPacket entitySoundPacket) {
                if (entitySoundPacket.getSound().value() == SoundEvents.BAT_DEATH) {
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

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        if (client.player == null) return;

        batBoxes.removeIf(box -> {
            Bat bat = box.getBat();
            if (bat.isInvisible()) return true;
            if (!bat.isAlive()) return true;
            if (bat.isRemoved()) return true;
            if (client.player.distanceTo(bat) > 32) return true;
            box.update();
            return false;
        });


        AABB detectionBox = client.player.getBoundingBox().inflate(32);
        List<Bat> nearbyBats = client.level.getEntitiesOfClass(Bat.class, detectionBox, e -> true);
        for (Bat bat : nearbyBats) {
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

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        customSound.playTestSound();
        client.gui.getChat().addClientSystemMessage(Component.literal("§d§lNA §f§l» §c§lBat §chas been killed!"));

        String rawMessage = BatHighlight.customMessage.getValue();
        if (rawMessage == null) rawMessage = " ";
        Component formattedText = ColorUtils.parseColor(rawMessage.trim());

        client.gui.setTitle(Component.empty());
        client.gui.setSubtitle(formattedText);
        client.gui.setTimes(0, 30, 0);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        batBoxes.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static class BatBox {
        private final Bat bat;

        public BatBox(Bat bat) {
            this.bat = bat;
        }

        public void update() {
            if (!bat.isAlive()) return;
            if (bat.isInvisible()) return;

            float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            int color = BatHighlight.highlightColor.getRGB();
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            Vec3 lerpedPos = bat.getPosition(tickDelta);
            Vec3 rawPos = bat.position();
            AABB box = bat.getBoundingBox().move(lerpedPos.subtract(rawPos));
            Renderer.addBox(box, r, g, b);
        }

        public Bat getBat() {
            return bat;
        }
    }

}